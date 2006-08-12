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

