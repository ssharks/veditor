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

import java.io.StringReader;

import net.sourceforge.veditor.parser.Module;
import net.sourceforge.veditor.parser.Segment;
import net.sourceforge.veditor.parser.VerilogParser;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Verilogのソースコードを構文解析する
 */
public class VerilogContentOutlineProvider implements ITreeContentProvider
{
	/*
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof Module)
		{
			Module mod = (Module)parentElement;
			return mod.getElements();
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element)
	{
		if (element instanceof Segment)
		{
			Segment mod = (Segment)element;
			return (Object)mod.getParent();
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element)
	{
		if (element instanceof Module)
		{
			Module mod = (Module)element;
			return mod.getElements() != null;
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement)
	{
		VerilogDocument doc = (VerilogDocument)inputElement;
		parse(doc);
		int size = parser.size();
		Object[] elements = new Object[size];

		for (int i = 0; i < size; i++)
			elements[i] = parser.getModule(i);
		return elements;
	}

	/*
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose()
	{
		parser.dispose();
		parser = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	private VerilogParser parser;

	private void parse(VerilogDocument doc)
	{
		String text = doc.get();

		parser = new VerilogParser(new StringReader(text));
		parser.parse(doc.getProject(), doc.getFile());

		parser.parseLineComment(new StringReader(text));
	}
}






