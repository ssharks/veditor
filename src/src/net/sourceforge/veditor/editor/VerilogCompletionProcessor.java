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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.veditor.parser.Module;
import net.sourceforge.veditor.parser.ModuleList;
import net.sourceforge.veditor.parser.VerilogParser;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
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

		int context = VerilogParser.OUT_OF_MODULE;
		String moduleName = "";
		try
		{
			VerilogParser parser =
				new VerilogParser(new StringReader(doc.get(0, documentOffset - length)));
			context = parser.getContext();
			moduleName = parser.getCurrentModuleName();
		}
		catch (BadLocationException e)
		{
		}

		List matchList = null;

		switch(context)
		{
			case VerilogParser.IN_MODULE:
				matchList = getInModuleProprosals(doc, documentOffset, match);
				break;
			case VerilogParser.IN_STATEMENT:
				matchList = getInStatmentProposals(doc, documentOffset, match, moduleName);
				break;
			case VerilogParser.OUT_OF_MODULE:
			default:
				Display.getCurrent().beep();
				return null;
		}

		Collections.sort(matchList);
		ICompletionProposal[] result = new ICompletionProposal[matchList.size()];
		for (int i = 0; i < matchList.size(); i++)
		{
			result[i] = (ICompletionProposal)matchList.get(i);
		}
		return result;
	}

	private List getInModuleProprosals(VerilogDocument doc, int offset, String replace)
	{
		int length = replace.length();
		List matchList = new ArrayList();

		//  template
		if (isMatch(replace, "always"))
			matchList.add(createAlways(doc, offset, length));
		if (isMatch(replace, "initial"))
			matchList.add(createInitial(doc, offset, length));
		if (isMatch(replace, "function"))
			matchList.add(createFunction(doc, offset, length));
		if (isMatch(replace, "task"))
			matchList.add(createTask(doc, offset, length));

		//  module instantiation
		ModuleList mlist = ModuleList.find(doc.getProject());
		String[] mnames = mlist.getModuleNames();
		for (int i = 0; i < mnames.length; i++)
		{
			if (isMatch(replace, mnames[i]))
			{
				matchList.add(
					new InstanceCompletionProposal(
						doc.getProject(),
						mnames[i],
						offset,
						length));
			}
		}
		return matchList;
	}

	private List getInStatmentProposals(
		VerilogDocument doc,
		int offset,
		String replace,
		String mname)
	{
		List matchList = new ArrayList();

		//  template
		if (isMatch(replace, "begin"))
			matchList.add(createBeginEnd(doc, offset, replace.length()));

		//  variable
		ModuleList mlist = ModuleList.find(doc.getProject());
		Module module = mlist.findModule(mname);
		if (module != null)
		{
			addInStatementProposals(matchList, offset, replace, module.getPorts());
			addInStatementProposals(matchList, offset, replace, module.getVariables());
		}
		return matchList;
	}

	private void addInStatementProposals(
		List matchList,
		int offset,
		String replace,
		Object[] vars)
	{
		for (int i = 0; i < vars.length; i++)
		{
			String port = vars[i].toString();
			if (isMatch(replace, port))
			{
				matchList.add(new CompletionProposal(port, offset, replace.length()));
			}
		}
	}

	private boolean isMatch(String replace, String name)
	{
		int length = replace.length();
		return name.length() >= length && name.substring(0, length).equals(replace);
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
	private ICompletionProposal createBeginEnd(IDocument doc, int offset, int length)
	{
		String indent = getIndent(doc, offset);
		String str = "begin\n" + indent + "\t\n" + indent + "end";
		int cursor = 7 + indent.length();
		return getCompletionProposal(str, offset, length, cursor, "begin/end");
	}
	private ICompletionProposal createAlways(IDocument doc, int offset, int length)
	{
		String first = "always @(posedge clk) begin\n\t";
		String second = "\nend\n";
		return getCompletionProposal(first + second, offset, length, first.length(), "always");
	}
	private ICompletionProposal createInitial(IDocument doc, int offset, int length)
	{
		String first = "initial begin\n\t";
		String second = "\nend\n";
		return getCompletionProposal(first + second, offset, length, first.length(), "initial");
	}
	private ICompletionProposal createFunction(IDocument doc, int offset, int length)
	{
		String first = "function ";
		String second = ";\nbegin\n\t\nend\nendfunction\n";
		return getCompletionProposal(first + second, offset, length, first.length(), "function");
	}
	private ICompletionProposal createTask(IDocument doc, int offset, int length)
	{
		String first = "task ";
		String second = ";\nbegin\n\t\nend\nendtask\n";
		return getCompletionProposal(first + second, offset, length, first.length(), "task");
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
		int length,
		int cursor,
		String display)
	{
		return new TemplateCompletionProposal(replace, offset, length, cursor, display);
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

	private class CompletionProposal implements ICompletionProposal, Comparable
	{
		private String replace;
		private int offset, length;

		public CompletionProposal(String replace, int offset, int length)
		{
			this.replace = replace;
			this.offset = offset;
			this.length = length;
		}
		public void apply(IDocument document)
		{
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
		public Image getImage()
		{
			return null;
		}
		public IContextInformation getContextInformation()
		{
			return null;
		}
		public String toString()
		{
			return getDisplayString();
		}
		public int compareTo(Object arg)
		{
			return toString().compareTo(arg.toString());
		}
		public String getDisplayString()
		{
			return replace;
		}
		public int getLength()
		{
			return length;
		}
		public int getOffset()
		{
			return offset;
		}
	}

	private class TemplateCompletionProposal extends CompletionProposal
	{
		private int cursor;
		private String display;

		public TemplateCompletionProposal(
			String replace,
			int offset,
			int length,
			int cursor,
			String display)
		{
			super(replace, offset, length);
			this.cursor = cursor;
			this.display = display;
		}
		public String getDisplayString()
		{
			return display;
		}
		public Point getSelection(IDocument document)
		{
			return new Point(getOffset() - getLength() + cursor, 0);
		}
	}

	private class InstanceCompletionProposal extends CompletionProposal
	{
		private IProject proj;
		private String name;

		public InstanceCompletionProposal(
			IProject proj,
			String name,
			int offset,
			int length)
		{
			super("", offset, length);
			this.proj = proj;
			this.name = name;
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
			Object[] ports = module.getPorts();
			int column = 0;
			for (int i = 0; i < ports.length; i++)
			{
				String port = ports[i].toString();
				String append = "." + port + "(" + port + ")";
				if (column + append.length() >= 80)
				{
					column = 0;
					replace.append("\n\t");
				}
				replace.append(append);
				column += append.length();

				if (i < ports.length - 1)
					replace.append(", ");
			}
			replace.append("\n);");

			try
			{
				document.replace(
					getOffset() - getLength(),
					getLength(),
					replace.toString());
			}
			catch (BadLocationException e)
			{
			}
		}
		public String getDisplayString()
		{
			return name;
		}
	}
}







