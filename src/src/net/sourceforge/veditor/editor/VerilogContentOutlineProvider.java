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

import org.eclipse.jface.text.IDocument;
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
		if ( parentElement instanceof VerilogSegment )
		{
			VerilogSegment mod = (VerilogSegment)parentElement ;
			int size = mod.size();
			Object[] elements = new Object[size] ;
		
			for( int i = 0 ; i < size ; i++ )
				elements[i] = mod.getInstance( i );
			return elements; 
		}
		return null ;
	}

	/*
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element)
	{
		if ( element instanceof VerilogSegment )
		{
			VerilogSegment mod = (VerilogSegment)element;
			return (Object)mod.getParent();
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element)
	{
		if ( element instanceof VerilogSegment )
		{
			VerilogSegment mod = (VerilogSegment)element ;
			return mod.size() >= 1 ;
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement)
	{
		IDocument doc = (IDocument)inputElement;
		parse(doc.get());
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
	}

	/*
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	private VerilogParser parser ;

	public void parse(String text)
	{
		parser = new VerilogParser( new StringReader(text) );
		try
		{
			parser.parse();
		}
		catch (ParseException e)
		{
			System.out.println( e );
		}
	}
}






