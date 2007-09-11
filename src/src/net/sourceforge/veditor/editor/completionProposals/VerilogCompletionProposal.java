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
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogOutlineElement;

import org.eclipse.swt.graphics.Image;

/**
 * Simple completion proposal with an image
 * @author ali
 *
 */
public class VerilogCompletionProposal extends CompletionProposal{
	OutlineElement m_Element;
	public VerilogCompletionProposal(OutlineElement element, int offset, int length) {
		super(element.getName(), offset, length);		
		m_Element=element;
	}
	/**
	 * Gets the element's image
	 */
	public Image getImage()
	{
		if (m_Element instanceof VerilogOutlineElement) {
			VerilogOutlineElement e = (VerilogOutlineElement) m_Element;
			return VerilogPlugin.getPlugin().getImage(e.GetImageName());
		}
		return null;
	}
}