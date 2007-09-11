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
				{"Style.noSpaceInBracket", "No space in bracket"},
				{ "Style.spaceBeforeOperator2", "add space before binary operator" },
				{ "Style.spaceAfterOperator2", "add space after binary operator" },
				{ "Style.spaceBeforeOperator1", "add space before unary operator" },
				{ "Style.spaceAfterOperator1", "add space after unary operator" },
				{ "Style.spaceBeforeComma", "add space before comma" },
				{ "Style.spaceAfterComma", "add space after comma" },
				{ "Style.spaceBeforeSemicolon", "add space before semiclon" },
				{ "Style.spaceBeforeOpenParen", "add space before opening parenthesis" },
				{ "Style.spaceAfterOpenParen", "add space after opening parenthesis" },
				{ "Style.spaceBeforeCloseParen", "add space before closing parenthesis" },
				{ "Style.spaceBeforeOpenBracket", "add space before opening bracket" },
				{ "Style.spaceAfterOpenBracket", "add space after opening bracket" },
				{ "Style.spaceBeforeCloseBracket", "add space before closing bracket" },
				{ "Style.spaceBeforeOpenBrace", "add space before opening brace" },
				{ "Style.spaceAfterOpenBrace", "add space after opening brace" },
				{ "Style.spaceBeforeCloseBrace", "add space before closing brace" },
				{ "Style.spaceBeforeCaseColon", "add space before colon in case" },
				{ "Style.spaceAfterCaseColon", "add space after colon in case" },
				{ "Style.spaceAfterIf", "add space after if" },
				{ "Style.spaceAfterFor", "add space after for" },
				{ "Style.spaceAfterWhile", "add space after while" },
				{ "Style.spaceAfterRepeat", "add space after repeat" } };
		for (int i = 0; i < names.length; i++)
		{
			addField(new BooleanFieldEditor(names[i][0], names[i][1], parent));
		}
	}
}
