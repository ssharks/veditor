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

import net.sourceforge.veditor.parser.Module;

/**
 * parse source code for ContentOutline
 */
public class HdlContentOutlineProvider extends TreeProviderBase
{
	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof Module)
		{
			Module mod = (Module)parentElement;
			return mod.getElements();
		}
		return null;
	}
}






