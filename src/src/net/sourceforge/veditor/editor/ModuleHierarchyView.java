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
package net.sourceforge.veditor.editor;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

/**
 * tree view of module instantiation
 */
public class ModuleHierarchyView extends PageBookView
{
	private static String defaultMessage = "Module Hierarchy is not available";
	
	public ModuleHierarchyView()
	{
		super();
	}

	public void setFocus()
	{
	}

	/**
	 * Gets this projects hierarchy page
	 * @param project
	 * @return
	 */
	public HdlHierarchyPage getHierarchyPage(IProject project){
		HdlHierarchyPage page=null;
		
		try {
			page=(HdlHierarchyPage)project.getSessionProperty(VerilogPlugin.getHierarchyId());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return page;
	}
	
	protected IPage createDefaultPage(PageBook book)
	{
		//  These are referred from ContentOutline
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage(defaultMessage);
		return page;
	}

	protected PageRec doCreatePage(IWorkbenchPart part)
	{
		IProject project=null;
		HdlHierarchyPage page=null;
		
		if (part instanceof HdlEditor) {
			HdlEditor hdlEditor = (HdlEditor) part;
			
			if(hdlEditor.getHdlDocument()!=null){				
				project=hdlEditor.getHdlDocument().getProject();
				//does this project have a hierarchy page
				if(getHierarchyPage(project)==null){
					//no hierarchy page yet
					Object obj = part.getAdapter(HdlHierarchyPage.class);
					if (obj instanceof HdlHierarchyPage){
						page=(HdlHierarchyPage)obj;
						initPage(page);
						page.createControl(getPageBook());
						try {
							project.setSessionProperty(VerilogPlugin.getHierarchyId(), page);
						} catch (CoreException e) {						
						}
					}
				}else{
					page=getHierarchyPage(project);
					//if the page is disposed, remove it and create it again
					if(page.isDisposed()){
						try {
							project.setSessionProperty(VerilogPlugin.getHierarchyId(), null);
						} catch (CoreException e) {
						}
						return doCreatePage(part);
					}					
				}
				return new PageRec(part, page);
			}
		}			
		return null;		
	}

	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord)
	{
		HdlHierarchyPage page = (HdlHierarchyPage)pageRecord.page;
		page.dispose();
		pageRecord.dispose();
	}

	protected IWorkbenchPart getBootstrapPart()
	{
		IWorkbenchPage page = getSite().getPage();
		if (page != null)
			return page.getActiveEditor();
		else
			return null;
	}

	protected boolean isImportant(IWorkbenchPart part)
	{
		return (part instanceof IEditorPart);
	}
}

