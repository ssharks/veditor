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

import net.sourceforge.veditor.parser.Element;
import net.sourceforge.veditor.parser.Module;
import net.sourceforge.veditor.parser.ModuleList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.Page;

public class ModuleHierarchyPage extends Page implements ISelectionChangedListener,
		IDoubleClickListener
{
	private TreeViewer treeViewer;
	private HdlEditor editor;
	private Clipboard clipboard;
	private ISelection selection;

	public ModuleHierarchyPage(HdlEditor editor)
	{
		super();
		this.editor = editor;
		clipboard = new Clipboard(Display.getCurrent());
	}

	public void createControl(Composite parent)
	{
		treeViewer = new TreeViewer(parent);
		createContextMenu(treeViewer.getTree());

		treeViewer.setContentProvider(new ModuleHierarchyProvider());
		treeViewer.setLabelProvider(new LabelProvider());
		treeViewer.addSelectionChangedListener(this);
		treeViewer.addDoubleClickListener(this);

		IDocument doc = editor.getDocument();
		if (doc != null)
		{
			treeViewer.setInput(doc);
			treeViewer.expandToLevel(2);
		}
	}

	private void createContextMenu(Control control)
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menu)
			{
				menu.add(new CopyTextAction());
				menu.add(new CopyHierarchyAction());
			}
		});
		Menu menu = menuManager.createContextMenu(control);
		control.setMenu(menu);
	}

	public Control getControl()
	{
		if (treeViewer == null)
			return null;
		return treeViewer.getControl();
	}

	public void setFocus()
	{
		treeViewer.getControl().setFocus();
	}

	public void setInput(Object input)
	{
		//update();
	}

	public void update()
	{
		if (treeViewer != null)
		{
			Control control = treeViewer.getControl();
			if (control != null && !control.isDisposed())
			{
				control.setRedraw(false);
				treeViewer.setInput(editor.getDocument());
				treeViewer.expandToLevel(2);
				control.setRedraw(true);
			}
		}
	}

	public void selectionChanged(SelectionChangedEvent event)
	{
		selection = event.getSelection();
	}

	public void doubleClick(DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection elements = (IStructuredSelection)selection;
			if (elements.size() == 1)
			{
				Object element = elements.getFirstElement();
				if (element instanceof Element)
				{
					editor.openPage(((Element)element).getTypeName());
				}
				else if (element instanceof Module)
				{
					editor.openPage(element.toString());
				}
			}
		}
	}

	/**
	 * set text to clipboard<p>
	 * it is called from the Actions
	 */
	private void setClipboard(StringBuffer text)
	{
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		clipboard.setContents(new String[] { text.toString() },
				new Transfer[] { plainTextTransfer });
	}

	private class CopyTextAction extends Action
	{
		public CopyTextAction()
		{
			super();
			setText("copy text");
		}
		public void run()
		{
			StringBuffer text = new StringBuffer();

			if (selection instanceof IStructuredSelection)
			{
				Object[] ary = ((IStructuredSelection)selection).toArray();
				for (int i = 0; i < ary.length; i++)
				{
					text.append(ary[i].toString() + "\n");
				}
				setClipboard(text);
			}
		}
	}

	private class CopyHierarchyAction extends Action
	{
		public CopyHierarchyAction()
		{
			super();
			setText("copy hierarchy");
		}
		public void run()
		{
			StringBuffer text = new StringBuffer();

			if (selection instanceof IStructuredSelection)
			{
				Object[] ary = ((IStructuredSelection)selection).toArray();
				addText(text, 0, ary);

				setClipboard(text);
			}
		}

		/**
		 * add text with indent of hierarchy
		 * @param text	destination string
		 * @param level	hierarchical level
		 * @param ary	instance array
		 */
		private void addText(StringBuffer text, int level, Object[] ary)
		{
			if (ary == null)
				return;
			for (int i = 0; i < ary.length; i++)
			{
				for (int j = 0; j < level; j++)
					text.append("    ");
				text.append(ary[i].toString() + "\n");

				Module module = null;
				if (ary[i] instanceof Element)
				{
					Element element = (Element)ary[i];
					IProject proj = editor.getHdlDocument().getProject();
					module = ModuleList.find(proj).findModule(element.getTypeName());
				}
				else if (ary[i] instanceof Module)
				{
					module = (Module)ary[i];
				}
				if (module != null)
					addText(text, level + 1, module.getInstance());
			}
		}
	}
}


