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
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class VerilogContentOutlinePage extends ContentOutlinePage
{
	private IDocumentProvider documentProvider;
	private VerilogEditor editor;

	public VerilogContentOutlinePage(IDocumentProvider provider, VerilogEditor editor)
	{
		super();
		documentProvider = provider;
		this.editor = editor;
	}

	public void createControl(Composite parent)
	{
		super.createControl(parent);

		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new VerilogContentOutlineProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.addSelectionChangedListener(this);

		IDocument doc = editor.getDocument();
		if (doc != null)
			viewer.setInput(editor.getDocument());
	}

	public void selectionChanged(SelectionChangedEvent event)
	{
		super.selectionChanged(event);

		ISelection selection = event.getSelection();

		//  emptyÇÃèÍçáÇÕresetHighlightRangeÇµÇ»Ç¢
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
