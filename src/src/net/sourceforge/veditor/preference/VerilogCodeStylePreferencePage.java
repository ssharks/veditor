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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class VerilogCodeStylePreferencePage extends AbstractSimplePreferencePage
{

	protected void createFieldEditors()
	{
		Composite parent = getFieldEditorParent();

		String names[][] = new String[][] {
            { PreferenceStrings.NO_SPACE_IN_BRACKET  , "No space in bracket"},
            { PreferenceStrings.SPACE_BEFORE_OPERATOR_2, "add space before binary operator" },
            { PreferenceStrings.SPACE_AFTER_OPERATOR_2, "add space after binary operator" },
            { PreferenceStrings.SPACE_BEFORE_OPERATOR_1, "add space before unary operator" },
            { PreferenceStrings.SPACE_AFTER_OPERATOR_1, "add space after unary operator" },
            { PreferenceStrings.SPACE_BEFORE_COMMA, "add space before comma" },
            { PreferenceStrings.SPACE_AFTER_COMMA, "add space after comma" },
            { PreferenceStrings.SPACE_BEFORE_SEMICOLON, "add space before semiclon" },
            { PreferenceStrings.SPACE_BEFORE_OPEN_PAREN, "add space before opening parenthesis" },
            { PreferenceStrings.SPACE_AFTER_OPEN_PAREN, "add space after opening parenthesis" },
            { PreferenceStrings.SPACE_BEFORE_CLOSE_PAREN, "add space before closing parenthesis" },
            { PreferenceStrings.SPACE_BEFORE_OPEN_BRACKET, "add space before opening bracket" },
            { PreferenceStrings.SPACE_AFTER_OPEN_BRACKET, "add space after opening bracket" },
            { PreferenceStrings.SPACE_BEFORE_CLOSE_BRACKET, "add space before closing bracket" },
            { PreferenceStrings.SPACE_BEFORE_OPEN_BRACE, "add space before opening brace" },
            { PreferenceStrings.SPACE_AFTER_OPEN_BRACE, "add space after opening brace" },
            { PreferenceStrings.SPACE_BEFORE_CLOSE_BRACE, "add space before closing brace" },
            { PreferenceStrings.SPACE_BEFORE_CASE_COLON, "add space before colon in case" },
            { PreferenceStrings.SPACE_AFTER_CASE_COLON, "add space after colon in case" },
            { PreferenceStrings.SPACE_AFTER_IF, "add space after if" },
            { PreferenceStrings.SPACE_AFTER_FOR, "add space after for" },
            { PreferenceStrings.SPACE_AFTER_WHILE, "add space after while" },
            { PreferenceStrings.SPACE_AFTER_REPEAT, "add space after repeat" } };
		for (int i = 0; i < names.length; i++)
		{
			addField(new BooleanFieldEditor(names[i][0], names[i][1], parent));
		}
	}
}
