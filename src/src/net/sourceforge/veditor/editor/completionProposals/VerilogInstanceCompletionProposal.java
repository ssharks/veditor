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

import java.util.ArrayList;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogModuleElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogOutlineElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogParameterElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogPortElement;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

public class VerilogInstanceCompletionProposal extends
InstanceCompletionProposal 
{
	protected OutlineElement m_Element;
	public VerilogInstanceCompletionProposal(
		HdlDocument doc,
		OutlineElement element,
		int offset,
		int length)
	{
		super(doc, element.getName(), offset, length);
		m_Element=element;
	}
	
	public void apply(IDocument document)
	{			
		String replace = getReplaceString();

		try
		{
			document.replace(getOffset() - getLength(), getLength(),
					replace);
		}
		catch (BadLocationException e)
		{
		}
	}
	
	public Image getImage()
	{
		if (m_Element instanceof VerilogOutlineElement) {
			VerilogOutlineElement e = (VerilogOutlineElement) m_Element;
			return VerilogPlugin.getPlugin().getImage(e.GetImageName());
		}
		return null;
	}
	
	protected String getReplaceString()
	{
		//default behavior
		StringBuffer replace=new StringBuffer(m_Element.getName());
		
		if(m_Element instanceof VerilogModuleElement){
			VerilogModuleElement module = (VerilogModuleElement) m_Element;
			String name = module.getName();
			String indent = "\n" + getIndentString();
			boolean isParams = VerilogPlugin
					.getPreferenceBoolean("ContentAssist.ModuleParameter");
			
			ArrayList<VerilogParameterElement> parameters=new ArrayList<VerilogParameterElement>();
			ArrayList<VerilogPortElement> ports=new ArrayList<VerilogPortElement>();
			for(OutlineElement child : module.getChildren()){
				if (child instanceof VerilogParameterElement) {
					VerilogParameterElement p = (VerilogParameterElement) child;

					// localparam is not included parameter list
					if (!p.isLocal())
						parameters.add(p);						
				}
				if (child instanceof VerilogPortElement) {
					VerilogPortElement p = (VerilogPortElement) child;
					ports.add(p);
				}
			}
							
			isParams = isParams && (parameters.size() > 0);

			replace = new StringBuffer(name + " ");
			
			if (isParams)
			{
				replace.append("#(");
				for (int i = 0; i < parameters.size(); i++)
				{
					replace.append(indent + "\t");			
					replace.append("." + parameters.get(i).getName() + "(" + parameters.get(i).GetValue() + ")");
					if (i < parameters.size() - 1)
						replace.append(",");
				}
				replace.append(indent + ")" + indent);
			}
			
			replace.append(name + "(");
			for (int i = 0; i < ports.size(); i++)
			{
				replace.append(indent + "\t");

				
				replace.append("." + ports.get(i).getName() + "(" + ports.get(i).getName() + ")");

				if (i < ports.size() - 1)
					replace.append(",");
			}
			replace.append(indent + ");");
		}
		
		return replace.toString();
	}
}