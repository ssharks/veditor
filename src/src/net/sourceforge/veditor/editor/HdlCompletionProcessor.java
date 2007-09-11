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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.editor.completionProposals.CompletionProposal;
import net.sourceforge.veditor.editor.completionProposals.HdlTemplateProposal;
import net.sourceforge.veditor.editor.completionProposals.IComparableCompletionProposal;
import net.sourceforge.veditor.editor.completionProposals.TemplateCompletionProposal;
import net.sourceforge.veditor.templates.TemplateWithIndent;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.swt.graphics.Image;

/**
 * content assist<p/>
 * simple templates and module instantiation
 */
abstract public class HdlCompletionProcessor implements IContentAssistProcessor
{
	protected static final String TEMPLATE_IMAGE= "$nl$/icons/template.gif";
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	abstract public ICompletionProposal[] computeCompletionProposals(
		ITextViewer viewer,
		int documentOffset);
	
	/** This should return the relevance of the template which will be used to sort
	 * the suggestions
	 * @param template
	 * @param prefix
	 * @return scalar relevance of the template in the given context
	 */
	abstract protected int getRelevance(Template template, String prefix);
	
	/**
	 * This function should return a context string for the given context.
	 * This value will be used to lookup the templates in the TemplateStore
	 * @param context
	 * @return Context string used to lookup the templates in the TemplateStore
	 */
	abstract protected String getTemplateContextString(int context);
	
	
	
	protected static void addProposals(List<IComparableCompletionProposal> matchList, int offset,
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
		

	protected static boolean isMatch(String replace, String name)
	{
		int length = replace.length();
		if (name.length() < length)
			return false;

		String ref = name.substring(0, length);
		return ref.toLowerCase().equals(replace.toLowerCase());
	}

	protected static String getMatchingWord(String text, int offset)
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

	protected static IComparableCompletionProposal getSimpleProposal(String word,
			int offset, int length)
	{
		return getCompletionProposal(word, offset, length, word.length(), word);
	}

	protected static IComparableCompletionProposal getCompletionProposal(String replace,
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
	
		
	/**
	 * Creates a list of template proposals
	 * @param viewer
	 * @param offset
	 * @return
	 */
	@SuppressWarnings("deprecation")
	protected List<HdlTemplateProposal> getTemplates(ITextViewer viewer, int offset,int context){		
		List<HdlTemplateProposal> results=new ArrayList<HdlTemplateProposal>();
		
		ITextSelection selection= (ITextSelection) viewer.getSelectionProvider().getSelection();

		// adjust offset to end of normalized selection
		if (selection.getOffset() == offset)
			offset= selection.getOffset() + selection.getLength();
		
		//walk backwards to find the beginning of the word we are replacing
		int beginning= offset;
		if (viewer.getDocument() instanceof HdlDocument) {
			HdlDocument doc = (HdlDocument) viewer.getDocument();
				
			while (beginning > 0) {
				try{
					char ch= doc.getChar(beginning - 1);
					if (!Character.isJavaIdentifierPart(ch))
						break;
					beginning--;			
				}
				catch (BadLocationException e)
				{
					//empty list
					return results;
				}
			}
			
			Region region= new Region(beginning, offset-beginning);
			String prefix=null;
			try{
				prefix=doc.get(beginning, offset-beginning);
			}
			catch (BadLocationException e)
			{
				//empty list
				return results;
			}
			//get a list of templates
			String contextString=getTemplateContextString(context);
			TemplateContextType contextType=VerilogPlugin.getPlugin().getContextTypeRegistry().getContextType(contextString);
			Template[] templates= VerilogPlugin.getPlugin().getTemplateStore().getTemplates(contextType.getId());
			DocumentTemplateContext documnetTemplateContext= new DocumentTemplateContext(contextType, doc, region.getOffset(), region.getLength());
			//find a matching template
			for (int i= 0; i < templates.length; i++) {
				Template template= templates[i];
				try {
					contextType.validate(template.getPattern());
				} catch (TemplateException e) {
					continue;
				}			
				
				if (template.matches(prefix, contextType.getId()) && template.getName().startsWith(prefix)){
					String indent = doc.getIndentString(offset);
					template = new TemplateWithIndent(template, indent);
					HdlTemplateProposal hdlTemplateProposal=new HdlTemplateProposal(template,
																documnetTemplateContext, 
																region, 
																getTemplateImage(template),
																getRelevance(template, prefix));
					results.add(hdlTemplateProposal);
				}
			}	
		}
		return results;
	}
	
	/**
	 * Returns an image used for template suggestions
	 * 
	 * @param template the template, ignored in this implementation
	 * @return the default template image
	 */
	protected Image getTemplateImage(Template template) {		
		VerilogPlugin plugin=VerilogPlugin.getPlugin();
		
		return plugin.getImage(TEMPLATE_IMAGE);				
	}
}







