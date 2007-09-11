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
import java.util.Collections;
import java.util.List;

import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.editor.completionProposals.IComparableCompletionProposal;
import net.sourceforge.veditor.editor.completionProposals.VerilogCompletionProposal;
import net.sourceforge.veditor.editor.completionProposals.VerilogInstanceCompletionProposal;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogFunctionElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogModuleElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogParameterElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogPortElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogSignalElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogTaskElement;
import net.sourceforge.veditor.templates.VerilogInModuleContextType;
import net.sourceforge.veditor.templates.VerilogInStatementContextType;
import net.sourceforge.veditor.templates.VerilogOutModuleContextType;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.widgets.Display;

public class VerilogCompletionProcessor extends HdlCompletionProcessor
{
	public ICompletionProposal[] computeCompletionProposals(
			ITextViewer viewer,
			int documentOffset)
		{
			HdlDocument doc = (HdlDocument)viewer.getDocument();
			String match = getMatchingWord(doc.get(), documentOffset);
			List<IComparableCompletionProposal> matchList = null;
			OutlineElement currentElement=null;

			int context = IParser.OUT_OF_MODULE;
			try
			{	
				context = doc.getContext(documentOffset);
				currentElement=doc.getElementAt(documentOffset,false);
			}
			catch (BadLocationException e)
			{
			}
			catch (HdlParserException e){
				
			}

			switch(context)
			{
				case IParser.IN_MODULE:
					matchList = getInModuleProposals(doc, documentOffset, match);
					break;
				case IParser.IN_STATEMENT:
					matchList = getInStatmentProposals(doc, documentOffset, match, currentElement);
					break;
				case IParser.OUT_OF_MODULE:
					matchList = getOutOfModuleProposals(doc, documentOffset, match);				
					break;
				default:
					Display.getCurrent().beep();
					return null;
			}
			
			matchList.addAll(getTemplates(viewer, documentOffset, context));
			Collections.sort(matchList);
			ICompletionProposal[] result = new ICompletionProposal[matchList.size()];
			for (int i = 0; i < matchList.size(); i++)
			{
				result[i] = (ICompletionProposal)matchList.get(i);
			}
			return result;
		}
	public List<IComparableCompletionProposal> getInModuleProposals(HdlDocument doc, int offset,
			String replace)
	{
		int length = replace.length();
		List<IComparableCompletionProposal> matchList = new ArrayList<IComparableCompletionProposal>();

		//  reserved word
		String[] rwords = {"assign ", "reg ", "wire ", "integer ", "parameter "};
		for(int i = 0 ; i < rwords.length ; i++)
		{
			if (isMatch(replace, rwords[i]))
				matchList.add(getSimpleProposal(rwords[i], offset, length));
		}

		OutlineDatabase database = doc.getOutlineDatabase();
		OutlineElement[] topLevelElements=database.findTopLevelElements(replace);
		for(OutlineElement element: topLevelElements){
			if (element instanceof VerilogModuleElement) {				
				matchList.add(new VerilogInstanceCompletionProposal(doc,element, offset, length));
			}
		}
				
		return matchList;
	}

	public List<IComparableCompletionProposal> getInStatmentProposals(
		HdlDocument doc,
		int offset,
		String replace,
		OutlineElement element)
	{
		List<IComparableCompletionProposal>  matchList= new ArrayList<IComparableCompletionProposal>();

		//  variable
		return addVariableProposals(doc, offset, replace, element, matchList);
	}

	public List<IComparableCompletionProposal> getOutOfModuleProposals(HdlDocument doc, int offset, String replace)
	{
		List<IComparableCompletionProposal>  matchList= new ArrayList<IComparableCompletionProposal>();
		
		return matchList;
	}	
	
	/**
	 * Returns the relevance scale of the template given the prefix. 
	 * This value is used to sort the suggestions made during template completion 
	 * 
	 * @param template the template
	 * @param prefix the prefix
	 * @return the relevance of the <code>template</code> for the given <code>prefix</code>
	 */
	protected int getRelevance(Template template, String prefix) {
		//for now, all are equal
		return 0;
	}
	
	/**
	 * This function should return a context string for the given context.
	 * This value will be used to lookup the templates in the TemplateStore
	 * @param context
	 * @return Context string used to lookup the templates in the TemplateStore
	 */
	protected String getTemplateContextString(int context)
	{
		final String results;
		switch (context)
		{
			case IParser.OUT_OF_MODULE:
				results = VerilogOutModuleContextType.CONTEXT_TYPE;
				break;
			case IParser.IN_MODULE:
				results = VerilogInModuleContextType.CONTEXT_TYPE;
				break;
			case IParser.IN_STATEMENT:
			default:
				results = VerilogInStatementContextType.CONTEXT_TYPE;
				break;
		}
		return results;
	}
	
	protected List<IComparableCompletionProposal> addVariableProposals(
			HdlDocument doc,
			int offset, String replace, 
			OutlineElement element, 
			List<IComparableCompletionProposal> matchList)
	{		
		//scan backwards and add all the available elements
		while (element != null)
		{
			// FIX!!! should we do something more advanced when there
			// are no modules?
			if (element instanceof VerilogModuleElement) {
				VerilogModuleElement module = (VerilogModuleElement) element;
				for(OutlineElement child: module.getChildren()){					
					if (child instanceof VerilogPortElement && child.getName().startsWith(replace)) {
						//add ports
						VerilogPortElement port = (VerilogPortElement) child;
						matchList.add(new VerilogCompletionProposal(port, offset, replace.length()));
						
					}else if (child instanceof VerilogSignalElement && child.getName().startsWith(replace)) {
						//add signals
						VerilogSignalElement signal = (VerilogSignalElement) child;
						matchList.add(new VerilogCompletionProposal(signal, offset, replace.length()));	
					}else if (element instanceof VerilogParameterElement) {
						//add parameter
						VerilogParameterElement parameter = (VerilogParameterElement) element;
						matchList.add(new VerilogCompletionProposal(parameter, offset, replace.length()));
					}					
				}
				
			}else if (element instanceof VerilogTaskElement) {
				VerilogTaskElement task = (VerilogTaskElement) element;
				for(OutlineElement child: task.getChildren()){
					//add signals
					if (child instanceof VerilogSignalElement && child.getName().startsWith(replace)) {
						VerilogSignalElement signal = (VerilogSignalElement) child;
						matchList.add(new VerilogCompletionProposal(signal, offset, replace.length()));	
					}					
				}
				
				
			}else if (element instanceof VerilogFunctionElement) {
				VerilogFunctionElement func = (VerilogFunctionElement) element;
				for(OutlineElement child: func.getChildren()){
					//add signals
					if (child instanceof VerilogSignalElement && child.getName().startsWith(replace)) {
						VerilogSignalElement signal = (VerilogSignalElement) child;
						matchList.add(new VerilogCompletionProposal(signal, offset, replace.length()));		
					}					
				}
				
			}	
			
			element=element.getParent();
		}
		
		return matchList;
	}

}


