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

import java.util.ResourceBundle;

import net.sourceforge.veditor.editor.VerilogEditor;
import net.sourceforge.veditor.editor.VerilogEditorMessages;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.source.ISourceViewer;


public abstract class AbstractAction extends Action
{
	private VerilogEditor editor;

	public AbstractAction(VerilogEditor editor, String name)
	{
		this.editor = editor;
		setEnabled(true);
		setId("net.sourceforge.veditor.veditor." + name);
		ResourceBundle resource = VerilogEditorMessages.getResourceBundle();
		setText(resource.getString(name + ".label"));
	}

	public abstract void run();

	protected VerilogEditor getEditor()
	{
		return editor;
	}
	protected ISourceViewer getViewer()
	{
		return editor.getViewer();
	}
	protected void beep()
	{
		editor.beep();
	}
}
