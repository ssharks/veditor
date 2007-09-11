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

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;
import net.sourceforge.veditor.editor.completionProposals.IComparableCompletionProposal;

/**
 * This class extends the Template proposal class in order to add a compareTo function
 *
 */
public class HdlTemplateProposal extends TemplateProposal implements IComparableCompletionProposal
{
	protected final String templateName;
	
	public int compareTo(Object object) {
		int results=0;
		if (object instanceof HdlTemplateProposal) {
			HdlTemplateProposal operand = (HdlTemplateProposal) object;
			results=getRelevance()-operand.getRelevance();		
			
			//if the two templates are equal, then compare names
			if(results == 0)
			{
				results=templateName.compareTo(operand.templateName);
			}			
		}			
		
		return results;
	}
	public HdlTemplateProposal(Template template, TemplateContext context, IRegion region, Image image, int relevance){
		super(template,context,region,image,relevance);
		templateName=template.getName();			
	}
}
