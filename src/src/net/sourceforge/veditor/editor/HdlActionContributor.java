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

package net.sourceforge.veditor.editor;

import java.util.ResourceBundle;

import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

public class HdlActionContributor extends TextEditorActionContributor
{
	private RetargetTextEditorAction contentAssistProposal;
	private RetargetTextEditorAction gotoMatchingBracket;
	private RetargetTextEditorAction openDeclaration;
	private RetargetTextEditorAction format;

	private static final String CONTENT_ASSIST_PROPOSAL = "ContentAssistProposal";
	private static final String GOTO_MATCHING_BRACKET = "GotoMatchingBracket";
	private static final String OPEN_DECLARATION = "OpenDeclaration";
	private static final String FORMAT = "Format";

	private ResourceBundle resource;

	public HdlActionContributor()
	{
		super();

		resource = EditorMessages.getResourceBundle();

		contentAssistProposal =
			createAction(
				CONTENT_ASSIST_PROPOSAL,
				ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		gotoMatchingBracket =
			createAction(
				GOTO_MATCHING_BRACKET,
				IJavaEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);
		openDeclaration =
			createAction(OPEN_DECLARATION, IJavaEditorActionDefinitionIds.OPEN_EDITOR);
		format = createAction(FORMAT, IJavaEditorActionDefinitionIds.FORMAT);
	}
	private RetargetTextEditorAction createAction(String name, String id)
	{
		RetargetTextEditorAction action = new RetargetTextEditorAction(resource, name + ".");
		action.setActionDefinitionId(id);
		return action;
	}

	public void init(IActionBars bars)
	{
		super.init(bars);

		IMenuManager menuManager = bars.getMenuManager();

		IMenuManager editMenu = menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null)
		{
			editMenu.add(new Separator());
			editMenu.add(contentAssistProposal);
			editMenu.add(format);
		}

		IMenuManager navigateMenu =
			menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null)
		{
			IMenuManager gotoMenu = navigateMenu.findMenuUsingPath(IWorkbenchActionConstants.GO_TO);
			gotoMenu.add(gotoMatchingBracket);

			navigateMenu.add(openDeclaration);
		}
	}

	private void setEditorAction(
		ITextEditor editor,
		final RetargetTextEditorAction action,
		String id)
	{
		action.setAction(getAction(editor, id));
	}

	private void doSetActiveEditor(IEditorPart part)
	{
		super.setActiveEditor(part);

		ITextEditor editor = null;
		if (part instanceof ITextEditor)
			editor = (ITextEditor)part;

		setEditorAction(editor, contentAssistProposal, CONTENT_ASSIST_PROPOSAL);
		setEditorAction(editor, gotoMatchingBracket, GOTO_MATCHING_BRACKET);
		setEditorAction(editor, openDeclaration, OPEN_DECLARATION);
		setEditorAction(editor, format, FORMAT);
	}

	public void setActiveEditor(IEditorPart part)
	{
		super.setActiveEditor(part);
		doSetActiveEditor(part);
	}

	public void dispose()
	{
		doSetActiveEditor(null);
		super.dispose();
	}
}




