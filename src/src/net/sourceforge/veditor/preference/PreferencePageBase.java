//
//  Copyright 2004, 2006 KOBAYASHI Tadashi
//  $Id$
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package net.sourceforge.veditor.preference;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.editor.HdlTextAttribute;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page
 */
abstract public class PreferencePageBase extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage
{

	public PreferencePageBase()
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

	public boolean performOk()
	{
		super.performOk();
		HdlTextAttribute.init();
		return true;
	}

}

