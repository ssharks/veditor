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

import java.io.StringReader;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 *  エディタのメインクラス
 */
public class VerilogEditor extends TextEditor
{
	private ColorManager colorManager;
	private VerilogContentOutlinePage outlinePage;

	public VerilogEditor()
	{
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new VerilogSourceViewerConfiguration(colorManager));
		setDocumentProvider(new VerilogDocumentProvider());
		VerilogColorConstants.init();
	}

	public void updatePartControl(IEditorInput input)
	{
		super.updatePartControl(input);
		if ( outlinePage != null )
			outlinePage.setInput(input);
	}

	public IDocument getDocument()
	{
		return getDocumentProvider().getDocument(getEditorInput());
	}
	

	protected void createActions()
	{
		super.createActions();

		IAction action;
		action =
			new TextOperationAction(
				VerilogEditorMessages.getResourceBundle(),
				"ContentAssistProposal.",
				this,
				ISourceViewer.CONTENTASSIST_PROPOSALS);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action);

		action = new GotoMatchingBracketAction();
		action.setActionDefinitionId(IJavaEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);
		setAction("GotoMatchingBracket", action);

		action = new OpenDeclarationAction();
		action.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EDITOR);
		setAction("OpenDeclaration", action);
	}

//	これはコンテンツアシストのためのコードだと思われるが不要か？
//	public void editorContextMenuAboutToShow(MenuManager menu)
//	{
//		super.editorContextMenuAboutToShow(menu);
//		addAction(menu, "ContentAssistProposal");
//		addAction(menu, "ContentAssistTip");
//	}
	
	public void dispose()
	{
		colorManager.dispose();
		if (outlinePage != null)
			outlinePage.setInput(null);
		super.dispose();
	}
	
	public void doRevertToSaved()
	{
		super.doRevertToSaved();
		if (outlinePage != null)
			outlinePage.update();
	}
	
	public void doSave(IProgressMonitor monitor)
	{
		super.doSave(monitor);
		if (outlinePage != null)
			outlinePage.update();
	}
	
	public void doSaveAs()
	{
		super.doSaveAs();
		if (outlinePage != null)
			outlinePage.update();
	}

	public void doSetInput(IEditorInput input) throws CoreException
	{
		super.doSetInput(input);
		if (outlinePage != null)
			outlinePage.setInput(input);
	}
	
	public Object getAdapter(Class required)
	{
		if (IContentOutlinePage.class.equals(required))
		{
			if (outlinePage == null)
			{
				outlinePage =
					new VerilogContentOutlinePage(getDocumentProvider(), this);
				if (getEditorInput() != null)
					outlinePage.setInput(getEditorInput());
			}
			return outlinePage;
		}
		return super.getAdapter(required);
	}

	protected void initializeEditor()
	{
		super.initializeEditor();
		
//		これはコンテンツアシストのためのコードだと思われる
//		setEditorContextMenuId("#EditorContext");
//		setRulerContextMenuId("#RulerContext");
	}

	protected void editorContextMenuAboutToShow(IMenuManager menu)
	{
		super.editorContextMenuAboutToShow(menu);
		menu.add(new Separator());
		menu.add(getAction("OpenDeclaration"));
	}

	private void beep()
	{
		Display.getCurrent().beep();
	}

	public VerilogSegment[] parse()
	{
		String text = getSourceViewer().getTextWidget().getText();
		VerilogParser parser = new VerilogParser( new StringReader(text) );
		try
		{
			parser.parse();
			int size = parser.size();
			VerilogSegment[] elements = new VerilogSegment[size];
	
			for (int i = 0; i < size; i++)
				elements[i] = parser.getModule(i);
			return elements;
		}
		catch (ParseException e)
		{
			System.out.println( e );
			return null ;
		}
	}

	/**
	 * verilogの対応括弧ジャンプ、begin/endも対応する
	 */
	public class GotoMatchingBracketAction extends Action
	{
		public static final String ID =
			"verilog.VerilogEditor.GotoMatchingBracketAction";
		public GotoMatchingBracketAction()
		{
			setEnabled(true);
			setId("ID");
			ResourceBundle resource = VerilogEditorMessages.getResourceBundle();
			setText(resource.getString("GotoMatchingBracket.label"));
		}
		public void run()
		{
			StyledText widget = getSourceViewer().getTextWidget();
			String text = widget.getText();

			int pos = widget.getCaretOffset();
			String[] open = { "(", "{", "[", "begin" };
			String[] close = {  ")", "}", "]", "end" };
			
			int openIdx = searchWord(open, text, pos);
			int closeIdx = searchWord(close, text, pos);

			int refPos = -1;
			if (openIdx != -1)
			{
				refPos = searchCloseBracket(text, pos, open[openIdx], close[openIdx]);
			}
			else if (closeIdx != -1)
			{
				refPos = searchOpenBracket(text, pos, open[closeIdx], close[closeIdx]);
			}
			if (refPos >= 0)
			{
				widget.setSelection(refPos);
				return;
			}
			beep();
		}
		
		/**
		 * open/closeのキーワードを探す
		 * @param words	キーワード配列
		 * @param text		参照テキスト
		 * @param pos		参照位置（ここから前方に検索）
		 * @return			キーワード配列のインデックス
		 */
		private int searchWord(String[] words, String text, int pos)
		{
			for (int i = 0; i < words.length; i++)
			{
				int len = words[i].length();
				if (text.substring(pos - len, pos).equals(words[i]))
					return i;
			}
			return -1;
		}
		
		
		/**
		 * 閉じる括弧を探す（ネストに対応する）
		 * @param text		参照テキスト
		 * @param pos		検索開始位置
		 * @param open		開く括弧の文字
		 * @param close	開じる括弧の文字
		 * @return
		 */
		private int searchCloseBracket(String text, int pos, String open, String close)
		{
			int level = 1;
			int len = text.length();
			pos++;
			int openLen = open.length();
			int closeLen = close.length();
			while (pos < len)
			{
				String ref = text.substring(pos, pos + 1);
				if (testBracket(text, pos, open, openLen))
					level++;
				if (testBracket(text, pos, close, closeLen))
					level--;
				if (level == 0)
					return pos + closeLen;
				pos++;
			}
			return -1;
		}

		/**
		 * 開く括弧を探す（ネストに対応する）
		 * @param text		参照テキスト
		 * @param pos		検索開始位置
		 * @param open		開く括弧の文字
		 * @param close	開じる括弧の文字
		 * @return
		 */
		private int searchOpenBracket(String text, int pos, String open, String close)
		{
			int level = 1;
			int openLen = open.length();
			int closeLen = close.length();
			pos -= 1 + closeLen;
			while (pos >= 0)
			{
				if (testBracket(text, pos, open, openLen))
					level--;
				if (testBracket(text, pos, close, closeLen))
					level++;
				if (level == 0)
					return pos + openLen;
				pos--;
			}
			return -1;
		}
		
		/**
		 * 対応する括弧をテストする
		 * @param text
		 * @param pos
		 * @param bracket
		 * @param len
		 * @return
		 */
		private boolean testBracket(String text, int pos, String bracket, int len)
		{
			if (bracket.equals(text.substring(pos, pos + len)))
			{
				if (Character.isJavaIdentifierStart(bracket.charAt(0)))
				{
					return !Character.isJavaIdentifierPart(text.charAt(pos - 1))
						&& !Character.isJavaIdentifierPart(text.charAt(pos + len));
				}
				else
					return true;
			}
			else
				return false;
		}
	}

	/**
	 * プロジェクトのツリーの中からモジュールの定義ファイルを探す<p>
	 * モジュール名とファイル名は同じでなければならない
	 */
	public class OpenDeclarationAction extends Action
	{
		public static final String ID =
			"verilog.VerilogEditor.OpenDeclarationAction";
		public OpenDeclarationAction()
		{
			setEnabled(true);
			setId("ID");
			ResourceBundle resource = VerilogEditorMessages.getResourceBundle();
			setText(resource.getString("OpenDeclaration.label"));
		}
		public void run()
		{
			StyledText widget = getSourceViewer().getTextWidget();
			
			String modName = widget.getSelectionText();
			if ( modName.equals(""))
			{
				beep();
				return ;
			}

			IFile file = searchModule(VerilogDocumentProvider.getCurrentProject(), modName + ".v");
			if ( file == null )
			{
				beep();
				return ;
			}
			openEditor(modName, file);
		}

		private void openEditor(String modName, IFile file)
		{
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try
			{
				IEditorPart editorPart = page.openEditor(file);
				if ( editorPart instanceof VerilogEditor )
				{
					VerilogEditor editor = (VerilogEditor)editorPart ;
					VerilogSegment[] modules = editor.parse();
					if ( modules != null )
					{
						for( int i = 0 ; i < modules.length ; i++ )
						{
							VerilogSegment mod = modules[i] ;
							if ( modName.equals(mod.toString()) )
							{
								IDocument doc = editor.getDocument();
								int line = mod.getLine() - 1 ;
								int start = doc.getLineOffset(line);
								editor.getSourceViewer().getTextWidget().setSelection(start);
							}
						}
					}
				}
			}
			catch (BadLocationException e)
			{}
			catch (PartInitException e)
			{}
		}
		
		
		private IFile searchModule( IContainer parent, String fileName )
		{
			try
			{
				IResource[] members;
				members = parent.members();
				for (int i = 0; i < members.length; i++)
				{
					if (members[i] instanceof IContainer)
					{
						IFile file = searchModule((IContainer)members[i], fileName);
						if (file != null)
							return file;
					}
					if (members[i] instanceof IFile)
					{
						IFile file = (IFile)members[i] ;
						if ( fileName.equals( file.getName() ) )
							return file ;
					}
				}
			}
			catch (CoreException e)
			{}
			return null ;
		}
	}
}


