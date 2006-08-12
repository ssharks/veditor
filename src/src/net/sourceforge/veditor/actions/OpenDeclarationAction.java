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
package net.sourceforge.veditor.actions;

import org.eclipse.swt.custom.StyledText;


/**
 * find module declaration from project tree<p>
 * file name and module name must be same
 */
public class OpenDeclarationAction extends AbstractAction
{
	public OpenDeclarationAction()
	{
		super("OpenDeclaration");
	}
	public void run()
	{
		StyledText widget = getViewer().getTextWidget();

		String modName = widget.getSelectionText();
		if (modName.equals(""))
		{
			beep();
			return;
		}
		getEditor().openPage(modName);
	}
}



