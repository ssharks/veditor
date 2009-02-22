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
		addBooleanField("ContentAssist.ModuleParameter",
				"Generate module parameter with instantiation(Verilog-2001)");
		addBooleanField("ScanProject.Enable", "Enable Scan Project");
		addBooleanField("Outline.Sort", "Sort in Outline/Hierarchy");
		addBooleanField("Outline.FilterSignals", "Filter Signals in Outline");
		addBooleanField("Outline.FilterPorts", "Filter Ports in Outline");
		addBooleanField("Compile.SaveBeforeCompile","Save File Before Compile");
		addStringField("Compile.command", "Compile command");
		addStringField("Synthesize.command", "Synthesize command");
		addStringField("Compile.Folder", "Compile folder");
	}

	public void init(IWorkbench workbench)
	{
	}

}

