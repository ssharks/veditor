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

package net.sourceforge.veditor.wizard;

public class NewVhdlWizard extends NewHdlWizard
{
	public void addPages()
	{
		super.addPages(".vhd");
	}

	String getInitialContents(String moduleName)
	{
		String contents[] = {
				"entity " + moduleName + " is",
				"\tport(",
				"\t);",
				"end " + moduleName + ";" };

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < contents.length; i++)
		{
			buf.append(contents[i]);
			buf.append("\n");
		}
		return buf.toString();
	}

}

