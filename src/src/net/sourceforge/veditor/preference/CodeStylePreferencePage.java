/*******************************************************************************
 * Copyright (c) 2004, 2007 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    KOBAYASHI Tadashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.preference;

import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class CodeStylePreferencePage extends AbstractSimplePreferencePage
{
	RadioGroupFieldEditor indentEditor;
	RadioGroupFieldEditor indentSizeEditor;

	protected void createFieldEditors()
	{
		Composite parent = getFieldEditorParent();

		indentEditor = new IndentFieldEditor(
					"Style.indent", "Indent character", 2,
					new String[][]{
							{"Tab", "Tab"},
							{"Space", "Space"}
					},
					parent);
		addField(indentEditor);
		
		indentSizeEditor = new RadioGroupFieldEditor(
				"Style.indentSize", "Inent size", 3,
				new String[][]{
						{"2", "2"},
						{"4", "4"},
						{"8", "8"}
				},
				parent);
		addField(indentSizeEditor);
		
		addBooleanField("Style.noSpaceInBracket", "No space in bracket");

		String indent = getPreferenceStore().getString("Style.indent");
		indentSizeEditor.setEnabled(indent.equals("Space"), parent);
	}
	
	class IndentFieldEditor extends RadioGroupFieldEditor
	{
		public IndentFieldEditor(String name, String labelText, int numColumns,
				String[][] labelAndValues, Composite parent)
		{
			super(name, labelText, numColumns, labelAndValues, parent);
		}

		protected void fireValueChanged(String property, Object oldValue,
				Object newValue)
		{
			super.fireValueChanged(property, oldValue, newValue);
			boolean sizeValid = newValue.equals("Space");
			indentSizeEditor.setEnabled(sizeValid, getFieldEditorParent());
		}
    }
}









