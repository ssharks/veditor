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
import net.sourceforge.veditor.document.VhdlDocument;
import net.sourceforge.veditor.editor.completionProposals.HdlTemplateProposal;
import net.sourceforge.veditor.editor.completionProposals.IComparableCompletionProposal;
import net.sourceforge.veditor.editor.completionProposals.VhdlInstanceCompletionProposal;
import net.sourceforge.veditor.editor.completionProposals.VhdlSubprogramProposalProvider;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.OutlineContainer;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ArchitectureElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.EntityDeclElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.PackageDeclElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlOutlineElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlSignalElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlSubprogram;
import net.sourceforge.veditor.templates.VhdlGlobalContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.widgets.Display;

public class VhdlCompletionProcessor extends HdlCompletionProcessor {

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int documentOffset) {
		HdlDocument doc = (HdlDocument) viewer.getDocument();
		String match = getMatchingWord(doc.get(), documentOffset);
		List<IComparableCompletionProposal> matchList = null;
		OutlineElement currentElement=null;
		
		doc.getFile();
		int context =VhdlDocument.VHDL_GLOBAL_CONTEXT;
		try {	
			context = doc.getContext(documentOffset);
			currentElement = doc.getElementAt(documentOffset,false);
		} catch (BadLocationException e) {
		} catch (HdlParserException e) {
		}
		

		switch (context) {
		case VhdlDocument.VHDL_GLOBAL_CONTEXT:
			matchList = getGlobalPropsals(doc, documentOffset, match);
			break;
		default:
			Display.getCurrent().beep();
			return null;
		}

		matchList.addAll(getTemplates(viewer, documentOffset, context));
		addSignalPropsals(doc, documentOffset, match,currentElement,matchList);
		addSubprogramProposals(doc, documentOffset, match,currentElement,matchList);
		Collections.sort(matchList);
		ICompletionProposal[] result = new ICompletionProposal[matchList.size()];
		for (int i = 0; i < matchList.size(); i++) {
			result[i] = (ICompletionProposal) matchList.get(i);
		}
		return result;
	}

	/**
	 * Returns a list of global proposals based on the given replace string
	 * @param doc Document where the proposal should be made
	 * @param offset offset of the proposal in the document 
	 * @param replace replacement string
	 * @return List of proposals matching the given string
	 */
	public List<IComparableCompletionProposal> getGlobalPropsals(HdlDocument doc,
			int offset, String replace) {
		
		OutlineDatabase database = doc.getOutlineDatabase();
		List<IComparableCompletionProposal> matchList = new ArrayList<IComparableCompletionProposal>();
		int length = replace.length();
		
		if (database != null) {
			OutlineElement[] elements = database.findTopLevelElements(replace);
			for (int i = 0; i < elements.length; i++) {
				if(elements[i] instanceof VhdlOutlineElement){
					matchList.add(new VhdlInstanceCompletionProposal(doc,
							elements[i], offset, length));				
				}
			}
			//look into packages
			elements = database.findTopLevelElements("");
			for (int i = 0; i < elements.length; i++) {
				if(elements[i] instanceof PackageDeclElement){
					OutlineElement[] subPackageElements=elements[i].getChildren();
					for(int j=0; j< subPackageElements.length; j++){
						if(subPackageElements[j] instanceof VhdlOutlineElement &&
								subPackageElements[j].getName().startsWith(replace)){
							matchList.add(new VhdlInstanceCompletionProposal(doc,
									subPackageElements[j], offset, length));
						}
					}
				}
			}
		}

		return matchList;
	}
	
	private void addEnityIntface(HdlDocument doc,String entityName, int offset, int length,
			String replace,IFile file, List<IComparableCompletionProposal> matchList){
		
		//if no entity was found, bail
		if (entityName == null){
			return;
		}
		OutlineDatabase database = doc.getOutlineDatabase();
		OutlineContainer outline = database.getOutlineContainer(file);
		Object[] children= outline.getTopLevelElements();
		
		for (int i=0;i<children.length;i++){
			if (children[i] instanceof EntityDeclElement) {
				EntityDeclElement entityDecl = (EntityDeclElement) children[i];			
				//if we find an entity declaration, add the ports and generics
				if (entityDecl.getName().equalsIgnoreCase(entityName)) {				
					//get the entity's children
					OutlineElement[] enitityChildren=entityDecl.getChildren();
					for(int entChildIdx=0;entChildIdx<enitityChildren.length;entChildIdx++){
						if(	enitityChildren[entChildIdx].getName().toUpperCase().startsWith(replace.toUpperCase())){
							matchList.add(new 
									VhdlInstanceCompletionProposal(doc,
											enitityChildren[entChildIdx],
											offset, 
											length));
						}
					}				
				}
			}
		}
	}
	
	/**
	 * Adds a list of subprogram calls to the proposal list
	 * @param doc
	 * @param offset
	 * @param replace
	 * @param element
	 * @param matchList
	 */
	private void addSubprogramProposals(HdlDocument doc,int offset, String replace, OutlineElement  element,
			List<IComparableCompletionProposal> matchList){
		VhdlOutlineElement parent=null;
		int length = replace.length();
		
						
		if (element instanceof VhdlOutlineElement) {
			parent = (VhdlOutlineElement) element;
			
		}
		//work your way up
		while(parent != null){
			OutlineElement[] children =parent.getChildren();
		
			for(int i=0;i < children.length;i++){
				if (children[i] instanceof VhdlSubprogram) {
					VhdlSubprogram subProgram = (VhdlSubprogram) children[i];
					
					if(subProgram.getName().startsWith(replace)){
						VhdlSubprogramProposalProvider proposalProvider=
							new VhdlSubprogramProposalProvider(doc,subProgram,offset,length);
						HdlTemplateProposal proposal=proposalProvider.createProposal();
						matchList.add(proposal);
					}
				}				
			}
			if (parent.getParent() instanceof VhdlOutlineElement) {
				parent = (VhdlOutlineElement) parent.getParent();
				
			}
			else{
				parent=null;
			}
		}	
	}
	/**
	 * Adds a list of variables to the completion proposal;
	 * @param doc
	 * @param offset
	 * @param replace
	 * @param element
	 * @param matchList
	 */
	private void addSignalPropsals(HdlDocument doc,int offset, String replace, OutlineElement  element,
			List<IComparableCompletionProposal> matchList){
		VhdlOutlineElement parent=null;
		String architectureEntityName=null;
		int length = replace.length();
		
		if (element instanceof VhdlOutlineElement) {
			parent = (VhdlOutlineElement) element;
			
		}
		//work your way up
		while(parent != null){
			OutlineElement[] children =parent.getChildren();
			
			//if we encounter an architecture, remember its entity
			if (parent instanceof ArchitectureElement){
				architectureEntityName=((ArchitectureElement)parent).GetEntityName();
			}
			
			for(int i=0;i < children.length;i++){
				if (children[i] instanceof VhdlSignalElement) {
					VhdlSignalElement signalElement = (VhdlSignalElement) children[i];
					
					if(signalElement.getName().startsWith(replace)){
						matchList.add(new VhdlInstanceCompletionProposal(doc,signalElement, offset, length));
					}
				}				
			}
			if (parent.getParent() instanceof VhdlOutlineElement) {
				parent = (VhdlOutlineElement) parent.getParent();
				
			}
			else{
				parent=null;
			}
		}
		if(architectureEntityName!=null){
			//add the entity interfaces
			addEnityIntface(doc,architectureEntityName,offset,length, replace, element.getFile(),matchList);
		}
		
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
	protected String getTemplateContextString(int context) {
		final String results;
		switch (context) {
		case IParser.OUT_OF_MODULE:
			results = VhdlGlobalContext.CONTEXT_TYPE;
			break;
		default:
			results = VhdlGlobalContext.CONTEXT_TYPE;
			break;
		}
		return results;
	}
	
}
