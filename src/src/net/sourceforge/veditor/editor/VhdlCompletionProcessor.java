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

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.document.VhdlDocument;
import net.sourceforge.veditor.editor.completionProposals.HdlTemplateProposal;
import net.sourceforge.veditor.editor.completionProposals.IComparableCompletionProposal;
import net.sourceforge.veditor.editor.completionProposals.VhdlInstanceCompletionProposal;
import net.sourceforge.veditor.editor.completionProposals.VhdlRecordCompletionProposal;
import net.sourceforge.veditor.editor.completionProposals.VhdlSubprogramProposalProvider;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.OutlineContainer;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ArchitectureElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.EntityDeclElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.PackageDeclElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.RecordElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlOutlineElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlSignalElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlSubprogram;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.GenericElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlPortElement;
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
		String matchwithdot = getMatchingWordWithdot(doc.get(), documentOffset);
		
		matchwithdot = matchwithdot+" ";//add space
		String matchword[]=matchwithdot.split("[.]");
		
		if(matchword.length==1) {
			switch (context) {
		 		case VhdlDocument.VHDL_GLOBAL_CONTEXT:
		 			matchList = getGlobalPropsals(doc, documentOffset, match);
		 			break;
		 		default:
		 			Display.getCurrent().beep(); return null;
		 	}
		 
		 	matchList.addAll(getTemplates(viewer, documentOffset, context));
			addSignalPropsals(doc, documentOffset, match, currentElement,matchList);
			addSubprogramProposals(doc, documentOffset, match,currentElement, matchList);
		} else {  // record member auto completion
			String recordname = null;
			recordname = getSignalType(doc, documentOffset, matchword[0], currentElement);
			for(int i=1;i<matchword.length-1;i++){
				OutlineElement[] tempRecord=searchRecordDefinition(doc, documentOffset, recordname,currentElement).getChildren();
				recordname=getMemberType(doc,documentOffset,matchword[i],tempRecord);
			}
			
			RecordElement finalRecord=(RecordElement)searchRecordDefinition(doc, documentOffset, recordname,currentElement);
			
			
			matchList = new ArrayList<IComparableCompletionProposal>();
			
			if (finalRecord != null) {
				OutlineElement[] memberElements = finalRecord.getChildren();
				String matchlc1 = matchword[matchword.length-1];
				                            String matchlc=matchlc1.trim().toLowerCase();   
				for (int h = 0; h < memberElements.length; h++) {
					String recordmember = memberElements[h].getName().toLowerCase();
					if (recordmember.startsWith(matchlc)) {
						 int cc=matchlc.length();
						 String replace = memberElements[h].getName();
						
						 matchList.add(new VhdlRecordCompletionProposal(replace, documentOffset, cc, replace.length(), replace));
					}
				}
			}
		}

		Collections.sort(matchList);
		ICompletionProposal[] result = new ICompletionProposal[matchList.size()];
		for (int i = 0; i < matchList.size(); i++) {
			result[i] = (ICompletionProposal) matchList.get(i);
		}
		return result;
	}
	


	// returns typename of member
	private String getMemberType(HdlDocument doc, int documentOffset,
			String membername, OutlineElement[] record) {

		String typeName = null;
		OutlineElement[] children = record;
		for (int i = 0; i < children.length; i++) {
			if (children[i].getName().equalsIgnoreCase(membername)) {
				if (!(children[i] instanceof VhdlOutlineElement))
					continue;
				typeName = ((VhdlOutlineElement) children[i]).getTypePart1();
			}
		}
		return typeName;
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
								subPackageElements[j].getName().toLowerCase().startsWith(replace.toLowerCase())){
							matchList.add(new VhdlInstanceCompletionProposal(doc,
									subPackageElements[j], offset, length));
						}
					}
				}
			}
			
			for (int i = 0; i < elements.length; i++) {
				if(elements[i] instanceof VhdlOutlineElement) {
					String name = elements[i].getName();
					if(name.startsWith("tb_")) continue;
					if(isMatch(replace, "tb_"+name)) {
						matchList.add(createTestBench(doc,(VhdlOutlineElement)elements[i],offset,length));
					}
				}
			}
		}

		return matchList;
	}
	
	public OutlineElement searchRecordDefinition(HdlDocument doc,
			int offset, String recordname, OutlineElement  element) {
		
		// first search in this file:
		VhdlOutlineElement parent=null;
		if (element instanceof VhdlOutlineElement) {
			parent = (VhdlOutlineElement) element;
		}
		//work your way up
		while(parent != null){
			OutlineElement[] children =parent.getChildren();
		
			for(int i=0;i < children.length;i++){
				if (children[i] instanceof RecordElement && children[i].getName()
						.equalsIgnoreCase(recordname)) {
					return children[i];
				}
			}
			if (parent.getParent() instanceof VhdlOutlineElement) {
				parent = (VhdlOutlineElement) parent.getParent();
			} else {
				parent=null;
			}
		}
		
		// not found in this file, search it in packages of other files
		OutlineDatabase database = doc.getOutlineDatabase();
		
		if (database != null) {
			OutlineElement[] elements = database.findTopLevelElements("");
			for (int i = 0; i < elements.length; i++) {
				if(elements[i] instanceof PackageDeclElement ){
					OutlineElement[] subPackageElements=elements[i].getChildren();
					for(int j=0; j< subPackageElements.length; j++){
						if (subPackageElements[j] instanceof RecordElement
								&& subPackageElements[j].getName()
										.equalsIgnoreCase(recordname)) {
							return subPackageElements[j];

						}
					}
				}
			
			}
		}

		return null;
	}

	private IComparableCompletionProposal createTestBench(HdlDocument doc, VhdlOutlineElement mod, int offset, int length) {
		String modname = mod.toString();
		
		String first = ""
		+ "library ieee;\n"
		+ "use ieee.std_logic_1164.all;\n"
		+ "use ieee.std_logic_arith.all;\n"
		+ "library std;\n"
		+ "use std.textio.all;\n"	
		+ "library work;\n"
		+ "use work."+modname+"PCK.all;\n\n"
		+ "entity tb_"+modname+" is\n"
		+ "end tb_"+modname+";\n\n"
		+ "architecture behav of tb_"+modname+" is\n";

		//Object[] ports = mod.getPorts();
		
		OutlineElement[] ports = mod.getChildren();
		String second = "";
		
		for (int i = 0; i < ports.length; i++)
		{
			if (!(ports[i] instanceof GenericElement)) continue;
			String port = ports[i].getName();
			String type = ports[i].getType(); // = port#in#std_logic			
			String[] typesplit = type.split("#");
			if(typesplit.length!=2) continue;
			if(typesplit[0].compareToIgnoreCase("generic")!=0) continue;
			second = second + ( "\tconstant " + port + " : " + typesplit[1] + " := ;\n" );
		}		
		
		for (int i = 0; i < ports.length; i++)
		{
			if (!(ports[i] instanceof VhdlPortElement)) continue;
			String port = ports[i].getName();
			String type = ports[i].getType(); // = port#in#std_logic			
			String[] typesplit = type.split("#");
			if(typesplit.length!=3) continue;
			if(typesplit[0].compareToIgnoreCase("port")!=0) continue;
			second = second + ( "\tsignal " + port + " : " + typesplit[2] + ";\n" );
		}

		String third = ""
		+ "begin\n"
		+ "\n\n\n"
		+ "\tprocess\n"
		+ "\tbegin\n"
		+ "\t\tif (clk='0') then\n"
		+ "\t\t\tclk <= '1';\n"
		+ "\t\telse\n"
		+ "\t\t\tclk <= '0';\n"
		+ "\t\tend if;\n"
		+ "\t\twait for 5 ns; --100Mhz\n"
		+ "\tend process;\n"		
		+ "\n\n\n"
		
		+ "\tprocess\n"
		+ "\tbegin\n"
		+ "\t\t--create a synchronous reset:\n"
		+ "\t\treset <='1';\n"
		+ "\t\twait for 1 us;\n"
		+ "\t\twait until rising_edge(clk);\n"
		+ "\t\treset <='0';\n"
		+ "\n\n\n"
		+ "\t\twait for 20 us;\n"
		+ "\t\tassert false report \"Simulation done\" severity failure;\n"
		+ "\t\twait;\n"
		+ "\tend process;\n"			
		+ "\n\n\n"
	
		+ "\tprocess\n"
		+ "\t\tvariable myline : line;\n"
		+ "\tbegin\n"
		+ "\t\twait for 10 us;\n"
		+ "\t\twrite(myline, now, right , 0, us);\n"
		+ "\t\twriteline(std.textio.output, myline);\n"
		+ "\t\tassert now < 100 us report \"Error: Time overflow\" severity failure;\n"
		+ "\tend process;\n"
	
		+ "\n\n\n"
		+ "\ti_dut: ";
		
		VhdlInstanceCompletionProposal prop = new VhdlInstanceCompletionProposal(doc, mod, offset, length);
		String forth = prop.getReplaceString();
		forth = forth.replace("\n", "\n\t");
		String fifth = "\n\n\nend behav;\n";
		
		String indentationstring = VerilogPlugin.getIndentationString();	
		first = first.replace("\t", indentationstring);
		second = second.replace("\t", indentationstring);
		third = third.replace("\t", indentationstring);
		forth = forth.replace("\t", indentationstring);
		fifth = fifth.replace("\t", indentationstring);
		
		return getCompletionProposal(first + second + third + forth + fifth, offset, length, first
				.length() + second.length() + third.length() , "tb_"+modname);
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

	
	// returns typename of the signal.
	
	private String getSignalType(HdlDocument doc,int offset, String signalname, OutlineElement  element){
		VhdlOutlineElement parent=null;
		
		String architectureEntityName=null;
		
		if (element instanceof VhdlOutlineElement) {
			parent = (VhdlOutlineElement) element;
			
			while (parent != null) {
				OutlineElement[] children = parent.getChildren();	
				//if we encounter an architecture, remember its entity
				if (parent instanceof ArchitectureElement){
					architectureEntityName=((ArchitectureElement)parent).GetEntityName();
				}
				for (int i = 0; i < children.length; i++) {
					if (children[i].getName().equalsIgnoreCase(signalname)) {
						if (! (children[i] instanceof VhdlOutlineElement)) continue;
						return ((VhdlOutlineElement)children[i]).getTypePart1();
					}
				}
				if (parent.getParent() instanceof VhdlOutlineElement) {
					parent = (VhdlOutlineElement) parent.getParent();
				} else {
					parent = null;
				}
			}
		}
		
		// now search in the port of the entity of this architecture:
		if(architectureEntityName!=null){
			OutlineDatabase database = doc.getOutlineDatabase();
			OutlineContainer outline = database.getOutlineContainer(doc.getFile());
			OutlineElement[] children= outline.getTopLevelElements();
			
			for (OutlineElement child:children){
				if (! (child instanceof EntityDeclElement)) continue;
				EntityDeclElement entityDecl = (EntityDeclElement) child;			
				//if we find an entity declaration, search ports for signalname
				if (!entityDecl.getName().equalsIgnoreCase(architectureEntityName)) continue;			
				//get the entity's children
				OutlineElement[] entityChildren=entityDecl.getChildren();
				for(OutlineElement entitychild:entityChildren) {
					if(!entitychild.getName().equalsIgnoreCase(signalname)) continue;
					if(!(entitychild instanceof VhdlOutlineElement)) continue;				
					return ((VhdlOutlineElement)entitychild).getTypePart2();
				}
			}
		}		
		
		return "";
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
