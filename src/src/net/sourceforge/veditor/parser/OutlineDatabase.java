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

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;

import net.sourceforge.veditor.VerilogPlugin;
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
	 * @return List of elements matching the given name. NULL if not found
	 */
	public OutlineElement[] findTopLevelElements(String name) {
		ArrayList<OutlineElement> list = new ArrayList<OutlineElement>();
		Iterator<OutlineContainer> iter = m_HierarchyDatabase.values()
				.iterator();

		while (iter.hasNext()) {
			OutlineContainer outline = iter.next();

			Object[] obj = outline.getTopLevelElements();
			for (int i = 0; i < obj.length; i++) {
				if (obj[i] instanceof OutlineElement) {
					OutlineElement element = (OutlineElement) obj[i];
					if (element.getName().startsWith(name)) {
						list.add(element);
					}
				}
			}
		}
		return list.toArray(new OutlineElement[0]);
	}
	
	/**
	 * Class used to inform the listeners of change
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
	class scanProjectJob extends Job {
		private Vector<IFile> m_Files;
		
		/**
		 * Constructor
		 * @param files to scan
		 */
		public scanProjectJob(Vector<IFile> files) {
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
	public void scanProject(){
		Vector<IFile> files=getProjectFiles(m_Project);
		
		if(m_ScanState != ScanState.IN_PROGRESS){
			scanProjectJob job = new scanProjectJob(files);
			m_ScanState=ScanState.IN_PROGRESS;
			//not a critical job
			job.setPriority(Job.DECORATE);
		    job.schedule();	    
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
	 * Scans a single file and adds its contents to the database
	 * @param file file to be scanned
	 */
	private void scanFile(IFile file) {
		
		try {
			InputStreamReader reader = new InputStreamReader(file.getContents());
			IParser parser=null;
			if (file.getName().endsWith(".v")){
				parser = ParserFactory.createVerilogParser(reader, m_Project,
						file);				
			} else 	if (file.getName().endsWith(".vhd")){
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
						scanProject();
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
