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
		        PreferenceStrings.INDENT_TYPE, "Indent character", 2,
					new String[][]{
							{"Tab",   PreferenceStrings.INDENT_TAB },
							{"Space", PreferenceStrings.INDENT_SPACE}
					},
					parent);
		addField(indentEditor);
		
		indentSizeEditor = new RadioGroupFieldEditor(
		        PreferenceStrings.INDENT_SIZE, "Indent size", 4,
				new String[][]{
						{"2", PreferenceStrings.INDENT_SIZE_2},
						{"3", PreferenceStrings.INDENT_SIZE_3},
						{"4", PreferenceStrings.INDENT_SIZE_4},
						{"8", PreferenceStrings.INDENT_SIZE_8}
				},
				parent);
		addField(indentSizeEditor);
		
		String indent = getPreferenceStore().getString(PreferenceStrings.INDENT_TYPE);
		indentSizeEditor.setEnabled(indent.equals(PreferenceStrings.INDENT_SPACE), parent);
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
			boolean sizeValid = newValue.equals(PreferenceStrings.INDENT_SPACE);
			indentSizeEditor.setEnabled(sizeValid, getFieldEditorParent());
		}
    }
}









