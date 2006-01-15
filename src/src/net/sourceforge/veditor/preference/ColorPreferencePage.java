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

import net.sourceforge.veditor.editor.HdlTextAttribute;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;

/**
 * Preference page
 */
public class ColorPreferencePage extends PreferencePageBase
{

	public ColorPreferencePage()
	{
	}

	protected void createFieldEditors()
	{
		addTextAttributeField("Default", "Default");
		addTextAttributeField("SingleLineComment", "Single line comment");
		addTextAttributeField("MultiLineComment", "Multi line comment");
		addTextAttributeField("DoxygenComment", "Doxygen comment");
		addTextAttributeField("KeyWord", "Reserved word");
		addTextAttributeField("String", "String");
	}

	private void addTextAttributeField(String name, String label)
	{
		Group parent = new Group(getFieldEditorParent(), 0);
		parent.setText(label);

		String colorName = "Color." + name;
		addField(new ColorFieldEditor(colorName, "", parent));
		addField(new BooleanFieldEditor("Bold." + name, "Bold", parent));
		addField(new BooleanFieldEditor("Italic." + name, "Italic", parent));
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

