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

import java.util.ArrayList;
import java.util.Iterator;

import net.sourceforge.veditor.parser.Module;
import net.sourceforge.veditor.parser.ModuleList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * content assist<p/>
 * simple templates and module instantiation
 */
public class VerilogCompletionProcessor implements IContentAssistProcessor
{
	public ICompletionProposal[] computeCompletionProposals(
		ITextViewer viewer,
		int documentOffset)
	{
		VerilogDocument doc = (VerilogDocument)viewer.getDocument();

		String match = getMatchingWord(doc.get(), documentOffset);
		int length = match.length();  // replace length

		//  simple template
		if (length == 0)
		{
			ICompletionProposal[] result = new ICompletionProposal[4];
			result[0] = createBeginEnd(doc, documentOffset);
			result[1] = createAlways(doc, documentOffset);
			result[2] = createFunction(doc, documentOffset);
			result[3] = createTask(doc, documentOffset);
			return result;
		}

		//  module instantiation
		ModuleList mlist = ModuleList.find(doc.getProject());
		String[] mnames = mlist.getModuleNames();
		ArrayList matchList = new ArrayList();
		for (int i = 0; i < mnames.length; i++)
		{
			if (mnames[i].length() >= length
				&& mnames[i].substring(0, length).equals(match))
			{
				matchList.add(mnames[i]);
			}
		}

		ICompletionProposal[] result = new ICompletionProposal[matchList.size()];
		for (int i = 0; i < matchList.size(); i++)
		{
			result[i] =
				new InstanceCompletionProposal(
					doc.getProject(),
					(String)matchList.get(i),
					documentOffset,
					length);
		}
		return result;
	}

	private String getMatchingWord(String text, int offset)
	{
		int start = offset;
		while (start > 0)
		{
			start--;
			char c = text.charAt(start);
			if (!Character.isJavaIdentifierPart(c))
				return text.substring(start + 1, offset);
		}
		return text.substring(0, offset);
	}


	//  code templates
	private ICompletionProposal createBeginEnd(IDocument doc, int offset)
	{
		String indent = getIndent(doc, offset);
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

	/**
	 * get indent string
	 */
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

	//  ContentInformation is not supported
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

	private class InstanceCompletionProposal implements ICompletionProposal
	{
		private IProject proj;
		private String name;
		private int offset, length;

		public InstanceCompletionProposal(
			IProject proj,
			String name,
			int offset,
			int length)
		{
			this.proj = proj;
			this.name = name;
			this.offset = offset;
			this.length = length;
		}

		public void apply(IDocument document)
		{
			ModuleList mlist = ModuleList.find(proj);
			Module module = mlist.findModule(name);
			if (module == null)
			{
				Display.getCurrent().beep();
				return;
			}

			StringBuffer replace = new StringBuffer(name + " " + name + "(\n\t");
			Iterator i = module.getPortIterator();
			int column = 0;
			while (i.hasNext())
			{
				String port = i.next().toString();
				String append = "." + port + "(" + port + ")";
				if (column + append.length() >= 80)
				{
					column = 0;
					replace.append("\n\t");
				}
				replace.append(append);
				column += append.length();

				if (i.hasNext())
					replace.append(", ");
			}
			replace.append("\n);");

			try
			{
				document.replace(offset - length, length, replace.toString());
			}
			catch (BadLocationException e)
			{
			}
		}

		public Point getSelection(IDocument document)
		{
			return null;
		}

		public String getAdditionalProposalInfo()
		{
			return null;
		}

		public String getDisplayString()
		{
			return name;
		}

		public Image getImage()
		{
			return null;
		}

		public IContextInformation getContextInformation()
		{
			return null;
		}
	}
}







