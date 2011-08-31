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
package net.sourceforge.veditor;

import net.sourceforge.veditor.preference.TopPreferencePage;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class VerilogPreferenceInitializer extends AbstractPreferenceInitializer
{
	public VerilogPreferenceInitializer()
	{
		super();
	}

	/**
	 * Error parser
	 * <PRE>
	 *  version number for future extension 
	 *  (
	 *    compiler name
	 *    error matching pattern
	 *    warning matching pattern
	 *    info matching pattern
	 *  )*
	 * </PRE>
	 */
	
	public void initializeDefaultPreferences()
	{
		Preferences preferences = VerilogPlugin.getPlugin()
				.getPluginPreferences();

		setDefaultAttr(preferences, "DoxygenComment", "64,64,128");
		setDefaultAttr(preferences, "SingleLineComment", "00,128,128");
		setDefaultAttr(preferences, "MultiLineComment", "00,128,128");
		setDefaultAttr(preferences, "String", "00,00,128");
		setDefaultAttr(preferences, "Default", "00,00,00");
		setDefaultAttr(preferences, "KeyWord", "127,00,85", true);
		setDefaultAttr(preferences, "Directive", "127,00,85", true);
		setDefaultAttr(preferences, "Types", "64,64,255");
		setDefaultAttr(preferences, "AutoTasks", "127,159,191",true,true);

		preferences.setDefault("ContentAssist.ModuleParameter", false);
		preferences.setDefault("ScanProject.Enable", true);
		preferences.setDefault(TopPreferencePage.MAX_PARSE_LINES, "50000");
		preferences.setDefault(TopPreferencePage.MAX_PARSE_TIME, "2000");
		preferences.setDefault("Outline.Sort", false);
		preferences.setDefault("Outline.FilterSignals", false);
		preferences.setDefault("Outline.FilterPorts", false);
		
		preferences.setDefault("Compile.SaveBeforeCompile",true);
		preferences.setDefault("Compile.command", "vcom %p%f");
		preferences.setDefault("Synthesize.command", "vcom -check_synthesis %p%f");
		preferences.setDefault("Compile.Folder", "simulation");
		
		preferences.setDefault("ErrorParser", "1\n");
		
		preferences.setDefault("Style.indent","Tab");
		preferences.setDefault("Style.indentSize", "4");
		preferences.setDefault("Style.noSpaceInBracket", true);
		preferences.setDefault("Style.Vhdl.PadOperators", true);
		preferences.setDefault("Style.Vhdl.IndentLibrary", false);
		setDefaultStyleSpace(preferences);
	}
	
	private void setDefaultAttr(Preferences preferences, String name,
			String color)
	{
		setDefaultAttr(preferences, name, color, false);
	}

	private void setDefaultAttr(Preferences preferences, String name,
			String color, boolean bold)
	{
		preferences.setDefault("Color." + name, color);
		preferences.setDefault("Bold." + name, bold);
		preferences.setDefault("Italic." + name, false);
	}
	
	private void setDefaultAttr(Preferences preferences, String name,
            String color, boolean bold,boolean italic)
    {
        preferences.setDefault("Color." + name, color);
        preferences.setDefault("Bold." + name, bold);
        preferences.setDefault("Italic." + name, italic);
    }
	
	private void setDefaultStyleSpace(Preferences preferences)
	{
		Object values[][] = new Object[][] {
				{ "Style.spaceBeforeOperator2", true },
				{ "Style.spaceAfterOperator2", true },
				{ "Style.spaceBeforeOperator1", true },
				{ "Style.spaceAfterOperator1", false },
				{ "Style.spaceBeforeComma", false },
				{ "Style.spaceAfterComma", true },
				{ "Style.spaceBeforeSemicolon", false },
				{ "Style.spaceBeforeOpenParen", false },
				{ "Style.spaceAfterOpenParen", false },
				{ "Style.spaceBeforeCloseParen", false },
				{ "Style.spaceBeforeOpenBracket", false },
				{ "Style.spaceAfterOpenBracket", false },
				{ "Style.spaceBeforeCloseBracket", false },
				{ "Style.spaceBeforeOpenBrace", false },
				{ "Style.spaceAfterOpenBrace", false },
				{ "Style.spaceBeforeCloseBrace", false },
				{ "Style.spaceBeforeCaseColon", false },
				{ "Style.spaceAfterCaseColon", true },
				{ "Style.spaceAfterIf", true },
				{ "Style.spaceAfterFor", true },
				{ "Style.spaceAfterWhile", true },
				{ "Style.spaceAfterRepeat", true } };
		for (int i = 0; i < values.length; i++)
		{
			boolean flag = ((Boolean)values[i][1]).booleanValue();
			preferences.setDefault(values[i][0].toString(), flag);
		}
	}
}


