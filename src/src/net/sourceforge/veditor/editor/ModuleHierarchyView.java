//
//  Copyright 2004, KOBAYASHI Tadashi
//  $Id$
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package net.sourceforge.veditor.editor;

import org.eclipse.jface.viewers.TreeViewer;
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
	private TreeViewer viewer;
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
		//  These are refered from ContentOutline
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

