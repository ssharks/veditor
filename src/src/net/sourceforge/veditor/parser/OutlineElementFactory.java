/*******************************************************************************
 * Copyright (c) 2006 Ali Ghorashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali Ghorashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.parser;

import org.eclipse.core.resources.IFile;

/**
 * This class defines the interface for class factory that generates outline elements
 * based on the given type
 *
 */
abstract public class OutlineElementFactory {
	//do not directly instantiate
	protected OutlineElementFactory() {}
	
	abstract public OutlineElement CreateElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file);
}
