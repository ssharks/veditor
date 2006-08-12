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

import java.io.StringReader;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.parser.ParserManager;
import net.sourceforge.veditor.parser.Segment;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


public abstract class TreeProviderBase implements ITreeContentProvider
{
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	private ParserManager manager;

	public void dispose()
	{
		if (manager != null)
		{
			manager.dispose();
			manager = null;
		}
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
		HdlDocument doc = (HdlDocument)inputElement;
		parse(doc);
		int size = manager.size();
		Object[] elements = new Object[size];

		for (int i = 0; i < size; i++)
			elements[i] = manager.getModule(i);
		return elements;
	}

	private void parse(HdlDocument doc)
	{
		String text = doc.get();

		manager = doc.createParserManager(new StringReader(text));
		if (manager.parse(doc.getProject()) == false)
			return;
			
		boolean comment = VerilogPlugin.getPreferenceBoolean("Outline.Comment");
		if (comment)
			manager.parseLineComment(new StringReader(text));
	}
}




