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

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

public final class HdlTextAttribute
{
	public static HdlTextAttribute SINGLE_LINE_COMMENT = new HdlTextAttribute();
	public static HdlTextAttribute MULTI_LINE_COMMENT = new HdlTextAttribute();
	public static HdlTextAttribute STRING = new HdlTextAttribute();
	public static HdlTextAttribute DEFAULT = new HdlTextAttribute();
	public static HdlTextAttribute KEY_WORD = new HdlTextAttribute();
	public static HdlTextAttribute DOXYGEN_COMMENT = new HdlTextAttribute();

	private RGB color;
	private int style;

	private HdlTextAttribute()
	{
		color = null;
		style = SWT.NORMAL;
	}
	
	public TextAttribute getTextAttribute(ColorManager colorManager)
	{
		return new TextAttribute(colorManager.getColor(color), null, style);
	}
	
	public static void init()
	{
		readColor(SINGLE_LINE_COMMENT, "SingleLineComment");
		readColor(MULTI_LINE_COMMENT, "MultiLineComment");
		readColor(DOXYGEN_COMMENT, "DoxygenComment");
		readColor(STRING, "String");
		readColor(DEFAULT, "Default");
		readColor(KEY_WORD, "KeyWord");
	}

	private static void readColor(HdlTextAttribute target, String key)
	{
		String color = VerilogPlugin.getPreferenceString("Color." + key);
		boolean bold = VerilogPlugin.getPreferenceBoolean("Bold." + key);
		boolean italic = VerilogPlugin.getPreferenceBoolean("Italic." + key);
		try
		{
//			int rgb = Integer.parseInt(color, 16);
//			int red = (rgb >> 16) & 0xff;
//			int green = (rgb >> 8) & 0xff;
//			int blue = (rgb >> 0) & 0xff;
			RGB rgb = StringConverter.asRGB(color);
			target.color = new RGB(rgb.red, rgb.green, rgb.blue);

			target.style = SWT.NORMAL;
			if (bold)
				target.style |= SWT.BOLD;
			if (italic)
				target.style |= SWT.ITALIC;
		}
		catch (NumberFormatException ex)
		{
		}
	}
}

