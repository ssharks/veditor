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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager
{
	private Map colorMap = new HashMap(10);

	public void dispose()
	{
		Iterator e = colorMap.values().iterator();
		while (e.hasNext())
		{
			((Color)e.next()).dispose();
		}
	}
	public Color getColor(RGB rgb)
	{
		Color color = (Color)colorMap.get(rgb);
		if (color == null)
		{
			color = new Color(Display.getCurrent(), rgb);
			colorMap.put(rgb, color);
		}
		return color;
	}
}
