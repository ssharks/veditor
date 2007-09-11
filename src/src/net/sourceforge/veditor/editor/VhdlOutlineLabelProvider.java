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
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlOutlineElement;

public class VhdlOutlineLabelProvider extends HdlLabelProvider {
	protected static final String OBJ_IMAGE= "$nl$/icons/obj.gif";	
	/**
	 * Converts a name to a string using the type string
	 * @param name element's name
	 * @param Type element's type
	 * @return
	 */
	protected String convertToString(OutlineElement element){
		if (element instanceof VhdlOutlineElement) {
			VhdlOutlineElement vhdlElement = (VhdlOutlineElement) element;
			return vhdlElement.getShortName();
		}
		//if not a vhdl element, something must be very wrong
		return "??";			
	}
	
	
	/**
	 * Returns an image name to be used for the given type
	 * @param type Type string
	 * @return Image name to be used for this tye
	 */
	protected String getImageNameForType(OutlineElement element){
		if (element instanceof VhdlOutlineElement) {
			VhdlOutlineElement vhdlElement = (VhdlOutlineElement) element;
			return vhdlElement.GetImageName();
		}
		//if not a vhdl element, something must be very wrong
		return OBJ_IMAGE;
	}

}
