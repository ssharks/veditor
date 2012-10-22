/*******************************************************************************
 * Copyright (c) 2004, 2006 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    KOBAYASHI Tadashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogInstanceElement;
import net.sourceforge.veditor.parser.verilog.VerilogParserReader;
import net.sourceforge.veditor.semanticwarnings.SemanticWarnings;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

/**
 * module database for each project
 */
public class OutlineDatabase {
	private IProject m_Project;
	private HashMap<IFile, OutlineContainer> m_HierarchyDatabase;
	public enum ScanState { NOT_STARTED, IN_PROGRESS, DONE, CANCELLED}
	private ScanState m_ScanState;
	private resourceChangeListern m_ResourceChangeListener;
	private Vector<OutlineDatabaseEvent> m_Listeners;

	
	public static OutlineDatabase getProjectsDatabase(IProject project){
		try {
			return (OutlineDatabase)project.getSessionProperty(VerilogPlugin.getOutlineDatabaseId());
		} catch (CoreException e) {
			return null;
		}
	}
	/**
	 * constructor
	 * @param project
	 */
	public OutlineDatabase(IProject project) {
		m_Project           = project;
		m_HierarchyDatabase = new HashMap<IFile, OutlineContainer>();
		m_ScanState=ScanState.NOT_STARTED;
		m_ResourceChangeListener = new resourceChangeListern();
		m_Listeners = new Vector<OutlineDatabaseEvent>();
		VerilogPlugin.getWorkspace().addResourceChangeListener(
				m_ResourceChangeListener, IResourceChangeEvent.POST_CHANGE);
	}

	public OutlineContainer getOutlineContainer(IFile file) {
		OutlineContainer results = m_HierarchyDatabase.get(file);
		//if the outline already exists
		if (m_HierarchyDatabase.get(file) == null) {
			results = new OutlineContainer();
			m_HierarchyDatabase.put(file, results);			
		}
		return results;
	}
	
	/**
	 * Finds top level elements starting with the given name
	 * @param name Element name
	 * @param exactly if true, find only exactly equal
	 * @return List of elements matching the given name. list.length = 0 if not found
	 */
	synchronized public OutlineElement[] findTopLevelElements(String name, boolean exactly) {
		ArrayList<OutlineElement> list = new ArrayList<OutlineElement>();
		Iterator<OutlineContainer> iter = m_HierarchyDatabase.values()
				.iterator();

		while (iter.hasNext()) {
			OutlineContainer outline = iter.next();

			Object[] obj = outline.getTopLevelElements();
			for (int i = 0; i < obj.length; i++) {
				if (obj[i] instanceof OutlineElement) {
					OutlineElement element = (OutlineElement) obj[i];
					if (exactly) {
						if (element.getName().equals(name)) {
							list.add(element);
						}
					} else {
						if (element.getName().startsWith(name)) {
							list.add(element);
						}
					}
				}
			}
		}
		return list.toArray(new OutlineElement[0]);
	}

	public OutlineElement[] findTopLevelElements(String name){
		return findTopLevelElements(name, false);
	}

	/**
	 * Class used to inform the listeners of change
	 * 
	 * @author gho18481
	 * 
	 */
	public abstract static class OutlineDatabaseEvent{
		public abstract void handel();
		
		protected void run() {
			Display.getDefault().asyncExec(new Runnable() {
	               public void run() {
	            	   handel();
	               }
	            }
			);
		}
	}
	
	/**
	 * Adds a listener to the list of listeners
	 * @param eventListner
	 */
	public void addChangeListner(OutlineDatabaseEvent eventListener){
		synchronized(m_Listeners){
			m_Listeners.add(eventListener);
		}
	}
	
	
	/**
	 * Adds a listener to the list of listeners
	 * @param eventListner
	 */
	public void removeChangeListner(OutlineDatabaseEvent eventListener){
		synchronized(m_Listeners){
			m_Listeners.remove(eventListener);
		}
	}
	
	/**
	 * Fires a change event
	 */
	protected void fireChangeEvent(){
		synchronized(m_Listeners){
			for(int i=0;i<m_Listeners.size();i++){			
				m_Listeners.get(i).run();
			}
		}
	}
	
	/**
	 * Class used to launch the project scanning task
	 * @author gho18481
	 *
	 */
	private class ScanProjectJob extends Job {
		private Vector<IFile> m_Files;
		
		/**
		 * Constructor
		 * @param files to scan
		 */
		public ScanProjectJob(Vector<IFile> files) {
			super("Scanning HDL Files");
			m_Files=files;
		}

		public IStatus run(IProgressMonitor monitor) {
			final int ticks = m_Files.size();
			monitor.beginTask("Scanning HDL Files", ticks);
			try {
				for (int i = 0; i < ticks; i++) {
					if (monitor.isCanceled()){
						m_ScanState=ScanState.CANCELLED;
						return Status.CANCEL_STATUS;
					}
					monitor.subTask("Scanning " + m_Files.get(i).getName());
					scanFile(m_Files.get(i));
					monitor.worked(1);
					fireChangeEvent();
				}
			} finally {
				monitor.done();
			}
			SemanticWarnings.clearWarningsGenerated();
			m_ScanState=ScanState.DONE;
			return Status.OK_STATUS;
		}
	}

	/**
	 * Class used to launch the project scanning task
	 */
	private class ScanTreeJob extends Job {
		private IFile file;
		
		/**
		 * Constructor
		 * @param files to scan
		 */
		public ScanTreeJob(IFile file) {
			super("Scanning HDL Tree");
			this.file = file;
		}

		public IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Scanning HDL Tree", 1);
			try {
				scanChildrenFiles(file);
				monitor.worked(1);
				fireChangeEvent();
			} finally {
				monitor.done();
			}
			m_ScanState=ScanState.DONE;
			return Status.OK_STATUS;
		}
	}
	
	/**
	 * @return The current scan state
	 */
	public ScanState getScanState(){
		return m_ScanState;
	}
	/**
	 * Public access to scan function
	 */
	public void scanProject() {
		if (VerilogPlugin.getPreferenceBoolean("ScanProject.Enable")) {

			Vector<IFile> files = getProjectFiles(m_Project);
			VerilogPlugin.deleteMarkers(m_Project);
			
			if (m_ScanState != ScanState.IN_PROGRESS) {
				ScanProjectJob job = new ScanProjectJob(files);
				m_ScanState = ScanState.IN_PROGRESS;
				// not a critical job
				job.setPriority(Job.DECORATE);
				job.schedule();
			}
		}
	}
	/**
	 * Searches the project for a list of files to be scanned
	 */
	private Vector<IFile> getProjectFiles(IContainer root){
		IResource[] members;
		Vector<IFile> results=new Vector<IFile>();
		
		try {
			members = root.members();
			for (int i = 0; i < members.length; i++) {
				if (members[i] instanceof IContainer){
					results.addAll(getProjectFiles((IContainer)members[i]));
				} else if (members[i] instanceof IFile) {
					IFile file = (IFile) members[i];					
					if(file.getName().endsWith(".v") || file.getName().endsWith(".vhd")){
						results.add(file);
					}
				}
			}
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}
	
	/**
	 * Public access to scan function
	 */
	public void scanTree(IFile file){
		if (VerilogPlugin.getPreferenceBoolean("ScanProject.Enable") == false) {
			if (m_ScanState != ScanState.IN_PROGRESS) {
				ScanTreeJob job = new ScanTreeJob(file);
				m_ScanState = ScanState.IN_PROGRESS;
				// not a critical job
				job.setPriority(Job.DECORATE);
				job.schedule();
			}
		}
	}

	/**
	 * Scan children recursively
	 */
	private void scanChildrenFiles(IFile file){
		OutlineContainer container = getOutlineContainer(file);
		OutlineElement[] elements = container.getTopLevelElements();
		for(int i = 0; i < elements.length; i++){
			OutlineElement[] children = elements[i].getChildren();
			for(int j = 0; j < children.length; j++)
			{
				if (children[j] instanceof VerilogInstanceElement)
				{
					VerilogInstanceElement instance = (VerilogInstanceElement)children[j];
					String[] types = instance.getType().split("#");
					OutlineElement[] element = findTopLevelElements(types[1], true);
					if (element.length == 0)
					{
						IFile found = findFile(m_Project, types[1] + ".v");
						if (found != null)
						{
							scanFile(found);
							scanChildrenFiles(found);
						}
					}
				}
				// TODO: VHDL files must be scanned
			}
		}
	}
	
	/**
	 * search definition from instance
	 */
	public OutlineElement findDefinition(OutlineElement instance) {
		if (instance instanceof VerilogInstanceElement) {
			String[] types = instance.getType().split("#");
			OutlineElement[] element = findTopLevelElements(types[1], true);
			if (element.length == 0)
				return null;
			else
				return element[0];
		}
		// TODO: VHDL module must be found
		return null;
	}
	
	/**
	 * Searches file for name in the project
	 * 
	 * @return
	 */
	private IFile findFile(IContainer root, String fileName) {
		try {
			IResource[] members;
			members = root.members();
			for (int i = 0; i < members.length; i++) {
				if (members[i] instanceof IContainer) {
					IFile file = findFile((IContainer) members[i], fileName);
					if (file != null)
						return file;
				}
				if (members[i] instanceof IFile) {
					IFile file = (IFile) members[i];
					if (fileName.equals(file.getName()))
						return file;
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}
	
	/**
	 * Scans a single file and adds its contents to the database
	 * 
	 * @param file
	 *            file to be scanned
	 */
	private void scanFile(IFile file) {
		try {
			IParser parser=null;
			if (file.getName().endsWith(".v")){
				// VerilogParserReader handles verilog compiler directive
				ParserReader reader = new VerilogParserReader(file.getContents(), file);
				parser = ParserFactory.createVerilogParser(reader, m_Project,
						file);
			} else 	if (file.getName().endsWith(".vhd")){
				ParserReader reader = new ParserReader(file.getContents());
				parser = ParserFactory.createVhdlParser(reader, m_Project, file);
			}
			//do we have parser
			if(parser!= null){
				parser.parse();				
			}
		} catch (CoreException e) {
		} catch (HdlParserException e){
			
		}
	}
	
	
	 /**
	  * Class used to determine the changes
	  *
	  */
	 class DeltaPrinter implements IResourceDeltaVisitor {
		public boolean visit(IResourceDelta delta) {
			IResource res = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				if (res instanceof IFile) {
					IFile file=(IFile) res;
					if(file.getName().endsWith(".v") || file.getName().endsWith(".vhd")){
						// just scan added file
						scanFile(file);
					}
				}
				break;
			case IResourceDelta.REMOVED:
				if (res instanceof IFile) {
					IFile file = (IFile) res;
					//remove this file from the outline database
					m_HierarchyDatabase.remove(file);
				}
				break;
			case IResourceDelta.CHANGED:
				break;
			}
			return true; // visit the children
		}
	}
	 
	/**
	 * Class used to keep track of workspace resources
	 */
	class resourceChangeListern implements IResourceChangeListener {
		/**
		 * Called when a resource is changed
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			switch (event.getType()) {
			case IResourceChangeEvent.PRE_CLOSE:
				break;
			case IResourceChangeEvent.PRE_DELETE:
				break;
			case IResourceChangeEvent.POST_CHANGE:
				try {
					event.getDelta().accept(new DeltaPrinter());
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case IResourceChangeEvent.PRE_BUILD:
				break;
			case IResourceChangeEvent.POST_BUILD:
				break;
			}
		}
	}
}
