/*******************************************************************************
 * Copyright (c) 2007 Ali Ghorashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali Ghorashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.editor.completionProposals;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.*;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Class to implement an instance completion proposal
 *
 */
public class VhdlInstanceCompletionProposal extends
		InstanceCompletionProposal {
	/**
	 * Outline element object
	 */
	private OutlineElement m_Element;

	/**
	 * Class constructor
	 * @param doc Document instance
	 * @param element Outline element associated with this proposal
	 * @param offset Offset line of the replace string from the beginning of the document??
	 * @param length length of the replace string
	 */
	public VhdlInstanceCompletionProposal(HdlDocument doc,
			OutlineElement element, int offset, int length) {

		super(doc, element.getName(), offset, length);
			
		if (element instanceof VhdlOutlineElement) {
			VhdlOutlineElement vhdlElement = (VhdlOutlineElement) element;
			name=vhdlElement.getShortName();				
		}
		
		m_Element = element;			
	}
	
	public Image getImage()
	{
		if (m_Element instanceof VhdlOutlineElement) {
			VhdlOutlineElement e = (VhdlOutlineElement) m_Element;
			return VerilogPlugin.getPlugin().getImage(e.GetImageName());
		}
		return null;
	}
	
	public static boolean canHandle(OutlineElement outlineElement) {
		if(outlineElement instanceof EntityDeclElement) return true;
		if(outlineElement instanceof ComponentDeclElement) return true;
		if(outlineElement instanceof VhdlSignalElement) return true;
		if(outlineElement instanceof VariableElement) return true;
		if(outlineElement instanceof ConstantElement) return true;
		if(outlineElement instanceof TypeDecl) return true;

		return false;
	}

	/**
	 * Called when the user selects a proposal
	 */
	public void apply(IDocument document) {
		if (m_Element == null) {
			Display.getCurrent().beep();
			return;
		}

		String replace = getReplaceString();

		try {
			document.replace(getOffset() - getLength(), getLength(),
					replace);
		} catch (BadLocationException e) {
		}
	}
	
	/**
	 * Gets the replace string for a component or entity declaration
	 * @return
	 */
	private String getComponentReplaceString(){
		String replaceString="",generics="",ports = "";
		
		Object obj[] = m_Element.getChildren();
		
		for (int i = 0; i < obj.length; i++){
			//do the generics
			if (obj[i] instanceof GenericElement) {
				GenericElement generic = (GenericElement) obj[i];
				generics += "\n " + generic.getName() + " => "+generic.getName()+",";
			}
			//do the ports
			if (obj[i] instanceof VhdlPortElement) {
				VhdlPortElement port = (VhdlPortElement) obj[i];
				ports += "\n " + port.getName() + " => "	
					+ port.getName()+",";
			}							
		}
		//assemble the replace string
		if (m_Element instanceof EntityDeclElement) {
			replaceString += "entity work.";
		}
		replaceString += m_Element.getName();
		//do we have any generics
		if(generics.length() > 0){
			replaceString+="\ngeneric map(";					
			//trim the last comma
			int lastComma=generics.lastIndexOf(',');
			replaceString+=generics.substring(0, lastComma)+generics.substring(lastComma+1);					
			replaceString+="\n)";
		}
		if(ports.length() > 0){
			replaceString+="\nport map(";					
			//trim the last comma
			int lastComma=ports.lastIndexOf(',');
			replaceString+=ports.substring(0, lastComma)+ports.substring(lastComma+1);
			replaceString+="\n)";
		}
		replaceString+=";";				
		//align the string
		replaceString = VerilogPlugin.alignOnChar(replaceString, '=', 1);				
		// add the indent string
		replaceString = replaceString.replace("\n ", "\n\t");
		String indentationstring = VerilogPlugin.getIndentationString();	
		replaceString = replaceString.replace("\t", indentationstring);
		return  replaceString.replace("\n", eol + getIndentString());
	}
		
	/**
	 * Gets the replacement string based on the outline element associated
	 * with this proposal
	 * @return
	 */
	public String getReplaceString() {
		if (m_Element instanceof EntityDeclElement || m_Element instanceof ComponentDeclElement) {
			return getComponentReplaceString();
		} else {
			return m_Element.getName();
		}
	}
}
