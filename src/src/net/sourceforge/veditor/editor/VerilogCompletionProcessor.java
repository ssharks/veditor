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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.*;

/**
 * コンテンツアシスト<p>
 * とりあえずごく簡単なパターンのみ
 */
public class VerilogCompletionProcessor implements IContentAssistProcessor
{
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
	{
		ICompletionProposal[] result = new ICompletionProposal[4];
		IDocument doc = viewer.getDocument();

		result[0] = createBeginEnd(doc, documentOffset);
		result[1] = createAlways(doc, documentOffset);
		result[2] = createFunction(doc, documentOffset);
		result[3] = createTask(doc, documentOffset);

		return result;
	}

	/*** begin/endのコンテンツアシストを作る */
	private ICompletionProposal createBeginEnd(IDocument doc, int offset)
	{
		// カレント行のインデント
		String indent;
		indent = getIndent(doc, offset);
		String str = "begin\n" + indent + "\t\n" + indent + "end";
		int cursor = 7 + indent.length();
		return getCompletionProposal(str, offset, cursor, "begin/end");
	}

	private ICompletionProposal createAlways(IDocument doc, int offset)
	{
		String first = "always @(posedge clk) begin\n\t";
		String second = "\nend\n";
		return getCompletionProposal(first + second, offset, first.length(), "always");
	}

	private ICompletionProposal createFunction(IDocument doc, int offset)
	{
		String first = "function ";
		String second = ";\nbegin\n\t\nend\nendfunction\n";
		return getCompletionProposal(first + second, offset, first.length(), "function");
	}

	private ICompletionProposal createTask(IDocument doc, int offset)
	{
		String first = "task ";
		String second = ";\nbegin\n\t\nend\nendtask\n";
		return getCompletionProposal(first + second, offset, first.length(), "task");
	}

	/** インデントの文字列を得る */
	private String getIndent(IDocument doc, int documentOffset)
	{
		try
		{
			int line = doc.getLineOfOffset(documentOffset);
			int pos = doc.getLineOffset(line);
			StringBuffer buf = new StringBuffer();
			for (;;)
			{
				char c = doc.getChar(pos++);
				if (!Character.isSpaceChar(c) && c != '\t')
					break;
				buf.append(c);
			}
			return buf.toString();
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private ICompletionProposal getCompletionProposal(
		String replace,
		int offset,
		int cursor,
		String display)
	{
		return new CompletionProposal(replace, offset, 0, cursor, null, display, null, display);
	}

	public char[] getCompletionProposalAutoActivationCharacters()
	{
		// return new char[] { '.', '(' };
		return null;
	}

	public String getErrorMessage()
	{
		return null;
	}

	//
	//  ContentInformationはサポートしない
	//

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset)
	{
		return null;
	}

	public char[] getContextInformationAutoActivationCharacters()
	{
		return null;
	}

	public IContextInformationValidator getContextInformationValidator()
	{
		return null;
	}
}
