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

import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.ParserFactory;
import net.sourceforge.veditor.parser.Segment;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


public abstract class TreeProviderBase implements ITreeContentProvider
{
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	public void dispose()
	{
		parser.dispose();
		parser = null;
	}

	public Object getParent(Object element)
	{
		if (element instanceof Segment)
		{
			Segment mod = (Segment)element;
			return (Object)mod.getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element)
	{
		return getChildren(element) != null;
	}

	public Object[] getElements(Object inputElement)
	{
		// parse source code and get instance list
		VerilogDocument doc = (VerilogDocument)inputElement;
		IParser parser = parse(doc);
		int size = parser.size();
		Object[] elements = new Object[size];

		for (int i = 0; i < size; i++)
			elements[i] = parser.getModule(i);
		return elements;
	}

	protected IParser parse(VerilogDocument doc)
	{
		String text = doc.get();

		parser = ParserFactory.create(new StringReader(text), doc.getFile());
		parser.parse(doc.getProject(), doc.getFile());
		parser.parseLineComment(new StringReader(text));
		return parser;
	}
	private IParser parser;
}
