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

import org.eclipse.swt.graphics.RGB;
import net.sourceforge.veditor.VerilogPlugin;

public final class VerilogColorConstants
{
	public static final RGB SINGLE_LINE_COMMENT = new RGB(0, 128, 128);
	public static final RGB MULTI_LINE_COMMENT = new RGB(0, 128, 128);
	public static final RGB STRING = new RGB(0, 0, 128);
	public static final RGB DEFAULT = new RGB(0, 0, 0);
	public static final RGB KEY_WORD = new RGB(128, 0, 128);
	public static final RGB DOXYGEN_COMMENT = new RGB(64, 64, 128);

	private VerilogColorConstants()
	{
	}

	public static void init()
	{
		readColor(SINGLE_LINE_COMMENT, "Color.SingleLineComment");
		readColor(MULTI_LINE_COMMENT, "Color.MultiLineComment");
		readColor(DOXYGEN_COMMENT, "Color.DoxygenComment");
		readColor(STRING, "Color.String");
		readColor(DEFAULT, "Color.Default");
		readColor(KEY_WORD, "Color.KeyWord");
	}

	private static void readColor(RGB target, String key)
	{
		String value = VerilogPlugin.getPreferenceString(key);
		try
		{
			int rgb = Integer.parseInt(value, 16);
			target.red = (rgb >> 16) & 0xff;
			target.green = (rgb >> 8) & 0xff;
			target.blue = (rgb >> 0) & 0xff;
		}
		catch (NumberFormatException ex)
		{
		}
	}
}
