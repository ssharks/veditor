//
//  Copyright 2004, KOBAYASHI Tadashi
//  $Id$
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package net.sourceforge.veditor.actions;

import net.sourceforge.veditor.editor.VerilogEditor;

import org.eclipse.swt.custom.StyledText;


/**
 * find module declaration from project tree<p>
 * file name and module name must be same
 */
public class OpenDeclarationAction extends AbstractAction
{
	public OpenDeclarationAction(VerilogEditor editor)
	{
		super(editor, "OpenDeclaration");
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



