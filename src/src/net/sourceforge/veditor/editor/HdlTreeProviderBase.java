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

import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.OutlineContainer;
import net.sourceforge.veditor.parser.OutlineElement;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


public abstract class HdlTreeProviderBase implements ITreeContentProvider
{
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}


	public void dispose()
	{
	}

	public Object getParent(Object element)
	{
		if (element instanceof OutlineElement)
		{
			OutlineElement e = (OutlineElement)element;
			return (Object)e.getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element)
	{
		if (element instanceof OutlineElement)
		{
			OutlineElement e = (OutlineElement)element;
			return e.HasChildren();
		}
		
		return false;
	}

	public Object[] getElements(Object inputElement)
	{		
		if (inputElement instanceof HdlDocument) {
			HdlDocument doc = (HdlDocument)inputElement;
			OutlineContainer outlineContainer=null;
			
			try {
				outlineContainer=doc.getOutlineContainer();
				if(outlineContainer != null){
					return outlineContainer.getTopLevelElements();
				}
			} catch (HdlParserException e) {
		
			}
		}
		return new Object[0];
		
	}

}




