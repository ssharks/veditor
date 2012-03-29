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
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{

        
	public PreferenceInitializer()
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
	    //IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(VerilogPlugin.ID);
		IPreferenceStore preferences = VerilogPlugin.getStore();
	
		setDefaultAttr(preferences, PreferenceStrings.DOXGEN_COMMENT, "64,64,128");
		setDefaultAttr(preferences, PreferenceStrings.SINGLE_LINE_COMMENT, "00,128,128");
		setDefaultAttr(preferences, PreferenceStrings.MULTI_LINE_COMMENT, "00,128,128");
		setDefaultAttr(preferences, PreferenceStrings.STRING, "00,00,128");
		setDefaultAttr(preferences, PreferenceStrings.DEFAULT, "00,00,00");
		setDefaultAttr(preferences, PreferenceStrings.KEYWORD, "127,00,85", true);
		setDefaultAttr(preferences, PreferenceStrings.DIRECTIVE, "127,00,85", true);
		setDefaultAttr(preferences, PreferenceStrings.TYPES, "64,64,255");
		setDefaultAttr(preferences, PreferenceStrings.AUTO_TASKS, "127,159,191",true,true);

		preferences.setValue(PreferenceStrings.MODULE_PARAMETERS, false);
		preferences.setValue(PreferenceStrings.ENABLE_SCAN_PROJECT, true);
		preferences.setValue(PreferenceStrings.MAX_PARSE_LINES, "50000");
		preferences.setValue(PreferenceStrings.MAX_PARSE_TIME, "2000");
		preferences.setValue(PreferenceStrings.SORT_OUTLINE, false);
		preferences.setValue(PreferenceStrings.FILTER_SINGALS_IN_OUTLINE, false);
		preferences.setValue(PreferenceStrings.FILTER_PORTS_IN_OUTLINE, false);
		
		preferences.setValue(PreferenceStrings.SAVE_BEFORE_COMPILE,true);
		preferences.setValue(PreferenceStrings.COMPILE_COMMAND, "vcom %p%f");
		preferences.setValue(PreferenceStrings.SYNTH_COMMAND, "vcom -check_synthesis %p%f");
		preferences.setValue(PreferenceStrings.COMPILE_FOLDER, "simulation");
		
		preferences.setValue(PreferenceStrings.ERROR_PARSER, "1\n");
		
		preferences.setValue(PreferenceStrings.INDENT_TYPE,PreferenceStrings.INDENT_SPACE);
		preferences.setValue(PreferenceStrings.INDENT_SIZE, PreferenceStrings.INDENT_SIZE_4);
		preferences.setValue(PreferenceStrings.NO_SPACE_IN_BRACKET, true);
		preferences.setValue(PreferenceStrings.PAD_OPERATORS, true);
		preferences.setValue(PreferenceStrings.INDENT_LIBRARY, false);
		setDefaultStyleSpace(preferences);
	}
	
	private void setDefaultAttr(IPreferenceStore preferences, String name,
			String color)
	{
		setDefaultAttr(preferences, name, color, false);
	}

	private void setDefaultAttr(IPreferenceStore preferences, String name,
			String color, boolean bold)
	{
		preferences.setValue("Color." + name, color);
		preferences.setValue("Bold." + name, bold);
		preferences.setValue("Italic." + name, false);
	}
	
	private void setDefaultAttr(IPreferenceStore preferences, String name,
            String color, boolean bold,boolean italic)
    {
        preferences.setValue("Color." + name, color);
        preferences.setValue("Bold." + name, bold);
        preferences.setValue("Italic." + name, italic);
    }
	
	private void setDefaultStyleSpace(IPreferenceStore preferences)
	{
		Object values[][] = new Object[][] {
				{ PreferenceStrings.SPACE_BEFORE_OPERATOR_2, true },
				{ PreferenceStrings.SPACE_AFTER_OPERATOR_2, true },
				{ PreferenceStrings.SPACE_BEFORE_OPERATOR_1, true },
				{ PreferenceStrings.SPACE_AFTER_OPERATOR_1, false },
				{ PreferenceStrings.SPACE_BEFORE_COMMA, false },
				{ PreferenceStrings.SPACE_AFTER_COMMA, true },
				{ PreferenceStrings.SPACE_BEFORE_SEMICOLON, false },
				{ PreferenceStrings.SPACE_BEFORE_OPEN_PAREN, false },
				{ PreferenceStrings.SPACE_AFTER_OPEN_PAREN, false },
				{ PreferenceStrings.SPACE_BEFORE_CLOSE_PAREN, false },
				{ PreferenceStrings.SPACE_BEFORE_OPEN_BRACKET, false },
				{ PreferenceStrings.SPACE_AFTER_OPEN_BRACKET, false },
				{ PreferenceStrings.SPACE_BEFORE_CLOSE_BRACKET, false },
				{ PreferenceStrings.SPACE_BEFORE_OPEN_BRACE, false },
				{ PreferenceStrings.SPACE_AFTER_OPEN_BRACE, false },
				{ PreferenceStrings.SPACE_BEFORE_CLOSE_BRACE, false },
				{ PreferenceStrings.SPACE_BEFORE_CASE_COLON, false },
				{ PreferenceStrings.SPACE_AFTER_CASE_COLON, true },
				{ PreferenceStrings.SPACE_AFTER_IF, true },
				{ PreferenceStrings.SPACE_AFTER_FOR, true },
				{ PreferenceStrings.SPACE_AFTER_WHILE, true },
				{ PreferenceStrings.SPACE_AFTER_REPEAT, true } };
		for (int i = 0; i < values.length; i++)
		{
			boolean flag = ((Boolean)values[i][1]).booleanValue();
			preferences.setValue(values[i][0].toString(), flag);
		}
	}
}


