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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Simple preference page, which uses FieldEditor
 */
abstract public class AbstractSimplePreferencePage extends
		FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	public AbstractSimplePreferencePage()
	{
		super(GRID);
		setPreferenceStore(VerilogPlugin.getPlugin().getPreferenceStore());
	}

	abstract protected void createFieldEditors();

	protected void addStringField(String name, String label)
	{
		addField(new StringFieldEditor(name, label, getFieldEditorParent()));
	}

	protected void addBooleanField(String name, String label)
	{
		addField(new BooleanFieldEditor(name, label, getFieldEditorParent()));
	}

	public void init(IWorkbench workbench)
	{
	}
}

