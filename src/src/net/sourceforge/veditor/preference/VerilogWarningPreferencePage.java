/*******************************************************************************
 * Copyright (c) 2004, 2012 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    KOBAYASHI Tadashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.preference;


public class VerilogWarningPreferencePage extends AbstractSimplePreferencePage implements PreferenceStrings
{   
	protected void createFieldEditors()
	{
		String names[][] = new String[][] {
            {WARNING_UNRESOLVED, "Unresolved signal"},
            {WARNING_NO_USED_ASIGNED, "No used or assignd signal, function or task"},
            {WARNING_BIT_WIDTH, "Bit width mismatch"},
            {WARNING_INT_CONSTANT, "Bit width mismatch with integer constant"},
            {WARNING_BLOCKING_ASSIGNMENT, "Blocking and non-blocking assignment in a block"},
            {WARNING_BLOCKING_ASSIGNMENT_IN_ALWAYS, "Blocking assignment in always block"},
            {WARNING_UNRESOLVED_MODULE, "Unresolved module"},
            {WARNING_MODULE_CONNECTION, "Checking module port connection"}
		};

		for (int i = 0; i < names.length; i++) {
			addBooleanField(names[i][0], names[i][1]);
		}
	}
}

