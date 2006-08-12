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
		Object obj = part.getAdapter(ModuleHierarchyPage.class);
		if (obj instanceof ModuleHierarchyPage)
		{
			ModuleHierarchyPage page = (ModuleHierarchyPage)obj;
			initPage(page);
			page.createControl(getPageBook());
			return new PageRec(part, page);
		}
		return null;
	}

	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord)
	{
		ModuleHierarchyPage page = (ModuleHierarchyPage)pageRecord.page;
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

