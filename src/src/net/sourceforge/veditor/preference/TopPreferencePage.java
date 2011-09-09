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

import org.eclipse.ui.IWorkbench;

/**
 * Top Preference page
 */
public class TopPreferencePage extends AbstractSimplePreferencePage
{
	public TopPreferencePage()
	{
	}

	protected void createFieldEditors()
	{
		addBooleanField(PreferenceStrings.CONTENT_ASSIST_MODULE_PARAM,
				"Generate module parameter with instantiation(Verilog-2001)");
		addBooleanField(PreferenceStrings.SCAN_ENABLE, "Enable Scan Project");
		addStringField(PreferenceStrings.MAX_PARSE_TIME,"Max amount time spent scanning files (mS)");
		addStringField(PreferenceStrings.MAX_PARSE_LINES,"Maximum number of lines in a file to scan");
		addBooleanField(PreferenceStrings.SORT_OUTLINE, "Sort in Outline/Hierarchy");
		addBooleanField(PreferenceStrings.FILTER_SINGALS_IN_OUTLINE, "Filter Signals in Outline");
		addBooleanField(PreferenceStrings.FILTER_PORTS_IN_OUTLINE, "Filter Ports in Outline");
		addBooleanField(PreferenceStrings.SAVE_BEFORE_COMPILE,"Save File Before Compile");
		addStringField(PreferenceStrings.COMPILE_COMMAND, "Compile command");
		addStringField(PreferenceStrings.SYNTH_COMMAND, "Synthesize command");
		addStringField(PreferenceStrings.COMPILE_FOLDER, "Compile folder");
		
	}

	public void init(IWorkbench workbench)
	{
	}

}

