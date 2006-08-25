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

package net.sourceforge.veditor.editor;

import java.util.Collections;
import java.util.List;

import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.Module;
import net.sourceforge.veditor.parser.ModuleList;
import net.sourceforge.veditor.parser.ParserManager;

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
abstract public class HdlCompletionProcessor implements IContentAssistProcessor
{
	public ICompletionProposal[] computeCompletionProposals(
		ITextViewer viewer,
		int documentOffset)
	{
		HdlDocument doc = (HdlDocument)viewer.getDocument();
		String match = getMatchingWord(doc.get(), documentOffset);
		int length = match.length();  // replace length

		int context = IParser.OUT_OF_MODULE;
		String moduleName = "";
		try
		{
			ParserManager manager = doc.createParserManager(doc.get(0,
					documentOffset - length));
			context = manager.parseContext();
			moduleName = manager.getCurrentModuleName();
		}
		catch (BadLocationException e)
		{
		}

		List matchList = null;

		switch(context)
		{
			case IParser.IN_MODULE:
				matchList = getInModuleProposals(doc, documentOffset, match);
				break;
			case IParser.IN_STATEMENT:
				matchList = getInStatmentProposals(doc, documentOffset, match, moduleName);
				break;
			case IParser.OUT_OF_MODULE:
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
	
	abstract public List getInModuleProposals(HdlDocument doc, int offset,
			String replace);

	abstract public List getInStatmentProposals(HdlDocument doc, int offset,
			String replace, String mname);

	protected static void addProposals(List matchList, int offset,
			String replace, Object[] vars)
	{
		for (int i = 0; i < vars.length; i++)
		{
			String word = vars[i].toString();
			if (isMatch(replace, word))
			{
				matchList.add(new CompletionProposal(word, offset, replace
						.length()));
			}
		}
	}
	
	protected List addVariableProposals(HdlDocument doc, int offset, String replace, String mname, List matchList)
	{
		ModuleList mlist = ModuleList.find(doc.getProject());
		
		if (mlist != null)
		{
			// FIX!!! should we do something more advanced when there
			// are no modules?

			Module module = mlist.findModule(mname);
			if (module != null)
			{
				addProposals(matchList, offset, replace, module.getPorts());
				addProposals(matchList, offset, replace, module.getSignals());
				addProposals(matchList, offset, replace, module.getParameters());
			}
		}
		return matchList;
	}

	protected static boolean isMatch(String replace, String name)
	{
		int length = replace.length();
		if (name.length() < length)
			return false;

		String ref = name.substring(0, length);
		return ref.toLowerCase().equals(replace.toLowerCase());
	}

	private static String getMatchingWord(String text, int offset)
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

	/**
	 * get indent string
	 */
	protected static String getIndent(IDocument doc, int documentOffset)
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

	protected static ICompletionProposal getSimpleProposal(String word,
			int offset, int length)
	{
		return getCompletionProposal(word, offset, length, word.length(), word);
	}

	protected static ICompletionProposal getCompletionProposal(String replace,
			int offset, int length, int cursor, String display)
	{
		return new TemplateCompletionProposal(replace, offset, length, cursor,
				display);
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
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int documentOffset)
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

	protected static class CompletionProposal implements ICompletionProposal,
			Comparable
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

	private static class TemplateCompletionProposal extends CompletionProposal
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

	abstract protected class InstanceCompletionProposal extends
			CompletionProposal
	{
		private IProject proj;
		private String name;
		private String indentString;

		public InstanceCompletionProposal(
			HdlDocument doc,
			String name,
			int offset,
			int length)
		{
			super("", offset, length);
			this.proj = doc.getProject();
			this.name = name;
			this.indentString = getIndent(doc, offset);
		}

		abstract public String getReplaceString(Module module);

		public void apply(IDocument document)
		{
			ModuleList mlist = ModuleList.find(proj);
			Module module = mlist.findModule(name);
			if (module == null)
			{
				Display.getCurrent().beep();
				return;
			}
			
			String replace = getReplaceString(module);

			try
			{
				document.replace(getOffset() - getLength(), getLength(),
						replace);
			}
			catch (BadLocationException e)
			{
			}
		}
		public String getDisplayString()
		{
			return name;
		}

		public String getIndentString()
		{
			return indentString;
		}
	}
}







