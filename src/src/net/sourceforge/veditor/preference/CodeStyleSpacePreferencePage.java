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

public class CodeStyleSpacePreferencePage extends AbstractSimplePreferencePage
{

	protected void createFieldEditors()
	{
		Composite parent = getFieldEditorParent();

		String names[][] = new String[][] {
				{ "Style.spaceBeforeOperator2", "before binary operator" },
				{ "Style.spaceAfterOperator2", "after binary operator" },
				{ "Style.spaceBeforeOperator1", "before unary operator" },
				{ "Style.spaceAfterOperator1", "after unary operator" },
				{ "Style.spaceBeforeComma", "before comma" },
				{ "Style.spaceAfterComma", "after comma" },
				{ "Style.spaceBeforeSemicolon", "before semiclon" },
				{ "Style.spaceBeforeOpenParen", "before opening parenthesis" },
				{ "Style.spaceAfterOpenParen", "after opening parenthesis" },
				{ "Style.spaceBeforeCloseParen", "before closing parenthesis" },
				{ "Style.spaceBeforeOpenBracket", "before opening bracket" },
				{ "Style.spaceAfterOpenBracket", "after opening bracket" },
				{ "Style.spaceBeforeCloseBracket", "before closing bracket" },
				{ "Style.spaceBeforeOpenBrace", "before opening brace" },
				{ "Style.spaceAfterOpenBrace", "after opening brace" },
				{ "Style.spaceBeforeCloseBrace", "before closing brace" },
				{ "Style.spaceBeforeCaseColon", "before colon in case" },
				{ "Style.spaceAfterCaseColon", "after colon in case" },
				{ "Style.spaceAfterIf", "after if" },
				{ "Style.spaceAfterFor", "after for" },
				{ "Style.spaceAfterWhile", "after while" },
				{ "Style.spaceAfterRepeat", "after repeat" } };
		for (int i = 0; i < names.length; i++)
		{
			addField(new BooleanFieldEditor(names[i][0], names[i][1], parent));
		}
	}
}
