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
package net.sourceforge.veditor.editor;

import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogOutlineElement;

public class VerilogOutlineLabelProvider extends HdlLabelProvider {	
	protected static final String OBJ_IMAGE= "$nl$/icons/obj.gif";	
	
	/**
	 * Converts a name to a string using the type string
	 * @param name element's name
	 * @param Type element's type
	 * @return
	 */
	protected String convertToString(OutlineElement element){
		if (element instanceof VerilogOutlineElement) {
			VerilogOutlineElement verilogElement = (VerilogOutlineElement) element;
			return verilogElement.getLongName();
		}
		//something very wrong
		return element.toString();
	}
		
	/**
	 * Returns an image name to be used for the given type
	 * @param type Type string
	 * @return Image name to be used for this type
	 */
	protected String getImageNameForType(OutlineElement element){
		if (element instanceof VerilogOutlineElement) {
			VerilogOutlineElement verilogElement = (VerilogOutlineElement) element;
			return verilogElement.GetImageName();
		}
		//default
		return OBJ_IMAGE;
	}
}
