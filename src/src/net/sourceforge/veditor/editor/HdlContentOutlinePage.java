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

import net.sourceforge.veditor.parser.Segment;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class HdlContentOutlinePage extends ContentOutlinePage
{
	private HdlEditor editor;

	public HdlContentOutlinePage(HdlEditor editor)
	{
		super();
		this.editor = editor;
	}

	public void createControl(Composite parent)
	{
		super.createControl(parent);

		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new HdlContentOutlineProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.addSelectionChangedListener(this);

		IDocument doc = editor.getDocument();
		if (doc != null)
			viewer.setInput(doc);
	}

	public void selectionChanged(SelectionChangedEvent event)
	{
		super.selectionChanged(event);

		ISelection selection = event.getSelection();

		if (!selection.isEmpty())
		{
			Segment mod =
				(Segment) ((IStructuredSelection)selection).getFirstElement();
			IDocument doc = editor.getDocument();

			int line = mod.getLine() - 1;
			try
			{
				int start = doc.getLineOffset(line);
				int length = doc.getLineOffset(line + mod.getLength()) - start;
				editor.setHighlightRange(start, length, true);
			}
			catch (IllegalArgumentException x)
			{
				editor.resetHighlightRange();
			}
			catch (BadLocationException e)
			{
				editor.resetHighlightRange();
			}
		}
	}

	public void setInput(Object input)
	{
		update();
	}

	public void update()
	{
		TreeViewer viewer = getTreeViewer();

		if (viewer != null)
		{
			Control control = viewer.getControl();
			if (control != null && !control.isDisposed())
			{
				control.setRedraw(false);
				viewer.setInput(editor.getDocument());
				viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}
}
