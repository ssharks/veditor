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
package net.sourceforge.veditor.document;

import java.util.Vector;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.editor.HdlPartitionScanner;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.OutlineContainer;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

abstract public class HdlDocument extends Document
{
	/**
	 * project which has this verilog source file
	 */
	private IProject m_Project;
	private IFile m_File;	
	private boolean m_NeedToRefresh;

	public HdlDocument(IProject project, IFile file)
	{
		super();
		m_Project = project;
		m_File = file;
		m_NeedToRefresh=true;
		addDocumentListener(new HdlDocumentListner());		
	}

	public IProject getProject()
	{
		return m_Project;
	}

	public IFile getFile()
	{
		return m_File;
	}

	/**
	 * Gets the outline database object for this project	 
	 * @return
	 */
	public OutlineDatabase getOutlineDatabase(){	
			OutlineDatabase database = null;
			IProject project = getProject();
			try {
				database = (OutlineDatabase) project
						.getSessionProperty(VerilogPlugin.getOutlineDatabaseId());
				if(database == null){
					database=CreateOutlineDatabase(project);
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return database;
		}
	
	/**
	 * Same as  getOutlineContainer(true);
	 * @return
	 * @throws HdlParserException 
	 */
	public OutlineContainer getOutlineContainer() throws HdlParserException{
		return getOutlineContainer(true);
	}
	/**
	 * @param bRefreshIfNeeded if true the outline will be if the data is stale
	 * @return The outline container for this document
	 * @throws HdlParserException 
	 */
	public OutlineContainer getOutlineContainer(boolean bRefreshIfNeeded) throws HdlParserException{
		OutlineDatabase database=getOutlineDatabase();
		if (database == null){
			return null;
		}
		OutlineContainer results=database.getOutlineContainer(getFile());
		if(results==null || bRefreshIfNeeded){
			refreshOutline();
		}
		
		return database.getOutlineContainer(getFile());
	}
	
	/**
	 * Refreshes the outline database if necessary
	 * @return true if a refresh was required
	 * @throws HdlParserException 
	 */
	public boolean refreshOutline() throws HdlParserException{
		if(m_NeedToRefresh){
			m_NeedToRefresh=false;
			getOutlineContainer(false).clear();
			IParser parser = createParser(get());
			VerilogPlugin.clearProblemMarker(getFile());
			try{
				parser.parse();				
			}
			catch (HdlParserException e){							
				throw e;
			}
			return true;
		}		
		return false;
	}
	
	/**
	 * Used to listen for document changes
	 *
	 */
	private class HdlDocumentListner implements IDocumentListener{

		public void documentAboutToBeChanged(DocumentEvent event) {
			// TODO Auto-generated method stub
			
		}

		public void documentChanged(DocumentEvent event) {
			//skip over the first modification because it is usually fired when a save occurs
			if(m_NeedToRefresh==false && event.getModificationStamp() > 1){
				m_NeedToRefresh=true;				
			}
		}
		
	}
	
		
	/**
	 * Returns the element near the given document offset
	 * @param document
	 * @param doRefresh If set to true, the document will be 
	 * parsed (if dirty) before an attempt is made to find the element
	 * @return
	 * @throws HdlParserException 
	 */
	public OutlineElement getElementAt(int documentOffset,boolean doRefresh)  throws BadLocationException, HdlParserException{
		int line=getLineOfOffset(documentOffset);
		int col=documentOffset-getLineOffset(line);
		
		return getOutlineContainer(doRefresh).getLineContext(line, col);
	}
	
	
	/**
	 * Creates an outline database and adds it to the project
	 * if one does not exist. This function will do useful work
	 * once per project	 
	 * @param project Project to owning the data base	
	 *   
	 */
	private OutlineDatabase CreateOutlineDatabase(IProject project){
		// do we already have an outline database?
		OutlineDatabase database=null;
		try {
			database = (OutlineDatabase)project.getSessionProperty(VerilogPlugin.getOutlineDatabaseId());			
		} catch (CoreException e) {			
			e.printStackTrace();
		}
		//if not created yet, make one
		if(database == null){
			database=new OutlineDatabase(project);
			try {
				project.setSessionProperty(VerilogPlugin.getOutlineDatabaseId(), database);
				database.scanProject();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return database;
	}
	
	/**
	 * Gets the indent string of the line that contains the offset
	 * @param documentOffset offset from the beginning of the document
	 * @return Indent string
	 */
	public  String getIndentString(int documentOffset)
	{
		try
		{
			int line = getLineOfOffset(documentOffset);
			int pos = getLineOffset(line);
			StringBuffer buf = new StringBuffer();
			for (;;)
			{
				char c = getChar(pos++);
				if (!Character.isSpaceChar(c) && c != '\t')
					break;
				buf.append(c);
			}
			return buf.toString();
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	abstract public HdlPartitionScanner createPartitionScanner();
	abstract protected IParser createParser(String text);
	public abstract Vector<OutlineElement> getDefinitionList(String name,int offset);	
	/**
	 * returns the context of the given offset
	 * @param documentOffset
	 * @return
	 */
	public abstract int getContext(int documentOffset) throws BadLocationException;

}



