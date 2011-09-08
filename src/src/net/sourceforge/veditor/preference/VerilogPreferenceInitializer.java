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
package net.sourceforge.veditor.preference;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class VerilogPreferenceInitializer extends AbstractPreferenceInitializer
{
    public static final String DOXGEN_COMMENT =  "DoxygenComment";
    public static final String SINGLE_LINE_COMMENT = "SingleLineComment";
    public static final String MULTI_LINE_COMMENT = "MultiLineComment";
    public static final String STRING = "String";
    public static final String DEFAULT = "Default";
    public static final String KEYWORD = "KeyWord";
    public static final String DIRECTIVE = "Directive";
    public static final String TYPES = "Types";
    public static final String AUTO_TASKS = "AutoTasks";
        
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
	    IEclipsePreferences preferences = new InstanceScope().getNode(VerilogPlugin.ID);
		//Preferences preferences = VerilogPlugin.getPlugin()
		//		.getPluginPreferences();

		setDefaultAttr(preferences, DOXGEN_COMMENT, "64,64,128");
		setDefaultAttr(preferences, SINGLE_LINE_COMMENT, "00,128,128");
		setDefaultAttr(preferences, MULTI_LINE_COMMENT, "00,128,128");
		setDefaultAttr(preferences, STRING, "00,00,128");
		setDefaultAttr(preferences, DEFAULT, "00,00,00");
		setDefaultAttr(preferences, KEYWORD, "127,00,85", true);
		setDefaultAttr(preferences, DIRECTIVE, "127,00,85", true);
		setDefaultAttr(preferences, TYPES, "64,64,255");
		setDefaultAttr(preferences, AUTO_TASKS, "127,159,191",true,true);

		preferences.putBoolean("ContentAssist.ModuleParameter", false);
		preferences.putBoolean("ScanProject.Enable", true);
		preferences.put(TopPreferencePage.MAX_PARSE_LINES, "50000");
		preferences.put(TopPreferencePage.MAX_PARSE_TIME, "2000");
		preferences.putBoolean("Outline.Sort", false);
		preferences.putBoolean("Outline.FilterSignals", false);
		preferences.putBoolean("Outline.FilterPorts", false);
		
		preferences.putBoolean("Compile.SaveBeforeCompile",true);
		preferences.put("Compile.command", "vcom %p%f");
		preferences.put("Synthesize.command", "vcom -check_synthesis %p%f");
		preferences.put("Compile.Folder", "simulation");
		
		preferences.put("ErrorParser", "1\n");
		
		preferences.put("Style.indent","Space");
		preferences.put("Style.indentSize", "4");
		preferences.putBoolean("Style.noSpaceInBracket", true);
		preferences.putBoolean("Style.Vhdl.PadOperators", true);
		preferences.putBoolean("Style.Vhdl.IndentLibrary", false);
		setDefaultStyleSpace(preferences);
	}
	
	private void setDefaultAttr(IEclipsePreferences preferences, String name,
			String color)
	{
		setDefaultAttr(preferences, name, color, false);
	}

	private void setDefaultAttr(IEclipsePreferences preferences, String name,
			String color, boolean bold)
	{
		preferences.put("Color." + name, color);
		preferences.putBoolean("Bold." + name, bold);
		preferences.putBoolean("Italic." + name, false);
	}
	
	private void setDefaultAttr(IEclipsePreferences preferences, String name,
            String color, boolean bold,boolean italic)
    {
        preferences.put("Color." + name, color);
        preferences.putBoolean("Bold." + name, bold);
        preferences.putBoolean("Italic." + name, italic);
    }
	
	private void setDefaultStyleSpace(IEclipsePreferences preferences)
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
			preferences.putBoolean(values[i][0].toString(), flag);
		}
	}
}


