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

import java.util.ResourceBundle;

import net.sourceforge.veditor.editor.HdlEditor;
import net.sourceforge.veditor.editor.EditorMessages;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.source.ISourceViewer;


public abstract class AbstractAction extends Action
{
	private HdlEditor editor;

	public AbstractAction(String name)
	{
		editor = HdlEditor.current();
		setEnabled(true);
		String id = "net.sourceforge.veditor.actions." + name;
		setId(id);
		setActionDefinitionId(id);
		ResourceBundle resource = EditorMessages.getResourceBundle();
		setText(resource.getString(name + ".label"));
	}

	public abstract void run();

	protected HdlEditor getEditor()
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
