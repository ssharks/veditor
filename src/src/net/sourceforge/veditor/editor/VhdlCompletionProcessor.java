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
import java.util.Calendar;
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
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.*;
import net.sourceforge.veditor.templates.VhdlGlobalContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.widgets.Display;

public class VhdlCompletionProcessor extends HdlCompletionProcessor {
	private static String TEST_BENCH_TEMPLATE_NAME="testbench";

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
		} else {  
			if(matchword.length==2 && matchword[0].equals("work")) { // packages auto completion
				String match2 = matchword[1].trim();
				OutlineDatabase database = doc.getOutlineDatabase();
				OutlineElement[] elements = database.findTopLevelElements(match2);
				
				matchList = new ArrayList<IComparableCompletionProposal>();
				
				for (int i = 0; i < elements.length; i++) {
					if(elements[i] instanceof PackageDeclElement){
						matchList.add(new VhdlInstanceCompletionProposal(doc, elements[i], documentOffset, match2.length()));
					}
				}
			} else { // record member auto completion
				String recordname = null;
				recordname = getSignalType(doc, documentOffset, matchword[0], currentElement);
				for(int i=1;i<matchword.length-1;i++){
					OutlineElement[] tempRecord=searchRecordDefinition(doc, documentOffset, recordname,currentElement).getChildren();
					recordname=getMemberType(doc,documentOffset,matchword[i],tempRecord);
				}
				
				TypeDecl finalRecord=(TypeDecl)searchRecordDefinition(doc, documentOffset, recordname,currentElement);
				
				
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
				if(VhdlInstanceCompletionProposal.canHandle(elements[i])) {
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
						if(VhdlInstanceCompletionProposal.canHandle(subPackageElements[j]) &&
								subPackageElements[j].getName().toLowerCase().startsWith(replace.toLowerCase())){
							matchList.add(new VhdlInstanceCompletionProposal(doc,
									subPackageElements[j], offset, length));
						}
					}
				}
			}
			
			for (int i = 0; i < elements.length; i++) {
				if(elements[i] instanceof EntityDeclElement || elements[i] instanceof ComponentDeclElement) {
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
				if (children[i] instanceof TypeDecl && children[i].getName()
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
						if (subPackageElements[j] instanceof TypeDecl
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
	  String results=
	          "library ieee;\n"
	        + "use ieee.std_logic_1164.all;\n"
	        + "use ieee.std_logic_arith.all;\n"
	        + "library std;\n"
	        + "use std.textio.all;\n"   
	        + "library work;\n"
	        + "use work.${module}.PCK.all;\n\n"
	        + "entity ${testbench} is\n"
	        + "end ${testbench};\n\n"
	        + "architecture behav of ${testbench} is\n"
	        + "-------------------------------\n"
	        + "-- Test bench for ${module}\n"
	        + "-------------------------------\n"
	        + "architecture Test of ${testbench} is\n"
	        + "    ${signals}\n"
	        + "    begin\n"
	        + "    uut:\n" 
	        + "        ${mod_def}\n"
	        +  "end architecture Test;\n";

	  //attempt to get new test bench template
      Template[] templates = VerilogPlugin.getPlugin().getTemplateStore().getTemplates(VhdlGlobalContext.CONTEXT_TYPE);
        for(Template template: templates){
            if(TEST_BENCH_TEMPLATE_NAME.equals(template.getName())){
                results=template.getPattern();
                break;
            }
        }
       String modname = mod.toString(); 
       String test_bench="tb_"+modname;
       OutlineElement[] ports = mod.getChildren();
       String signals="";
       for (int i = 0; i < ports.length; i++)
       {
           if (!(ports[i] instanceof GenericElement)) continue;
           String port = ports[i].getName();
           String type = ports[i].getType(); // = port#in#std_logic            
           String[] typesplit = type.split("#");
           if(typesplit.length!=2) continue;
           if(typesplit[0].compareToIgnoreCase("generic")!=0) continue;
           signals = signals + ( "\tconstant " + port + " : " + typesplit[1] + " := ;\n" );
       }       
       
       for (int i = 0; i < ports.length; i++)
       {
           if (!(ports[i] instanceof VhdlPortElement)) continue;
           String port = ports[i].getName();
           String type = ports[i].getType(); // = port#in#std_logic            
           String[] typesplit = type.split("#");
           if(typesplit.length!=3) continue;
           if(typesplit[0].compareToIgnoreCase("port")!=0) continue;
           signals = signals + ( "\tsignal " + port + " : " + typesplit[2] + ";\n" );
       }
       
       VhdlInstanceCompletionProposal prop = new VhdlInstanceCompletionProposal(doc, mod, offset, length);
       String module_def = prop.getReplaceString();
       module_def = module_def.replace("\n", "\n\t"); 

		String indentationstring = VerilogPlugin.getIndentationString();
		results=results.replace("${testbench}", test_bench);
		results=results.replace("${signals}", signals);
		results=results.replace("${module}", modname);
		results=results.replace("${mod_def}", module_def);
		results=results.replace("${user}", System.getProperty("user.name"));
		results=results.replace("${year}", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
		String month = Integer.toString(Calendar.getInstance().get(Calendar.MONTH)+1);
		if(month.length()<2) month = "0"+month;
		String day = Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		if(day.length()<2) day = "0"+day;
		results=results.replace("${month}", month);
		results=results.replace("${day}", day);

       results = results.replace("\t", indentationstring);
       
       return getCompletionProposal(results, offset, length, results.length() , test_bench+" (auto gen testbench) ");
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
					
					if(subProgram.getName().toLowerCase().startsWith(replace.toLowerCase())){
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
		//look into packages
		OutlineDatabase database = doc.getOutlineDatabase();
		
		if (database != null) {
			OutlineElement[] elements = database.findTopLevelElements("");
			for (int i = 0; i < elements.length; i++) {
				if(elements[i] instanceof PackageDeclElement){
					OutlineElement[] subPackageElements=elements[i].getChildren();
					for(int j=0; j< subPackageElements.length; j++){
						if(subPackageElements[j] instanceof VhdlSubprogram) {
							if(subPackageElements[j].getName().toLowerCase().startsWith(replace.toLowerCase())){
								VhdlSubprogramProposalProvider proposalProvider=
									new VhdlSubprogramProposalProvider(doc,(VhdlSubprogram)subPackageElements[j],offset,length);
								HdlTemplateProposal proposal=proposalProvider.createProposal();
								matchList.add(proposal);
							}
						}
					}
				}
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
				if (VhdlInstanceCompletionProposal.canHandle(children[i])) {
					if(children[i].getName().toLowerCase().startsWith(replace.toLowerCase())){
						matchList.add(new VhdlInstanceCompletionProposal(doc,children[i], offset, length));
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
