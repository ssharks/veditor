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

import net.sourceforge.veditor.parser.Element;
import net.sourceforge.veditor.parser.Module;


public class ModuleHierarchyProvider extends TreeProviderBase
{
	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof Module)
		{
			Module mod = (Module)parentElement;
			return mod.getInstance();
		}
		if (parentElement instanceof Element)
		{
			//  search module by name
			Element element = (Element)parentElement;
			Module parent = (Module)(element.getParent());
			Module mod = parent.findModule(element.getTypeName());
			if (mod == null)
				return null;
			else
				return mod.getInstance();
		}
		return null;
	}
}

