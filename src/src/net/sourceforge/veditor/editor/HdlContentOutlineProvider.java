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

import java.util.Vector;

import net.sourceforge.veditor.parser.OutlineElement;

/**
 * parse source code for ContentOutline
 */
public class HdlContentOutlineProvider extends HdlTreeProviderBase
{
	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof OutlineElement)
		{
			OutlineElement e = (OutlineElement)parentElement;
			Vector<Object> results=new Vector<Object>();
			OutlineElement[] children=e.getChildren();
			for(int i=0;i<children.length;i++){
				if (children[i].isVisible()){
					results.add(children[i]);
				}
			}
			
			return results.toArray();
		}
		return null;
	}
}






