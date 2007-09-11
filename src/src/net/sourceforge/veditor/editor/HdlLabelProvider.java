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

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.parser.OutlineElement;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

abstract public class HdlLabelProvider extends LabelProvider {
	/**
	 * Converts a name to a string using the type string
	 * @param name element's name
	 * @param Type element's type
	 * @return
	 */
	abstract protected String convertToString(OutlineElement outlineElement);
	
	/**
	 * Returns an image name to be used for the given type
	 * @param type Type string
	 * @return Image name to be used for this tye
	 */
	abstract protected String getImageNameForType(OutlineElement outlineElement);
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element){
		 if (element instanceof OutlineElement) {
			OutlineElement outlineElement = (OutlineElement) element;
			
			return convertToString(outlineElement);
		}
		 else{
			 return element.toString();
		 }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		VerilogPlugin plugin = VerilogPlugin.getPlugin();
		Image results = null;

		// if the element is an outline element
		if (element instanceof OutlineElement) {
			OutlineElement outlineElement = (OutlineElement) element;
			String imageName = getImageNameForType(outlineElement);
			results=plugin.getImage(imageName);			
		}		
		
		return results;
	}
}
