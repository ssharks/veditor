/*******************************************************************************
 * Copyright (c) 2004, 2012 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    KOBAYASHI Tadashi - initial API and implementation
 *******************************************************************************/

package net.sourceforge.veditor.parser.verilog;

public class Identifier extends Token {
	private static final long serialVersionUID = 1L;

	private int width = 0;

	public Identifier(Token ident) {
		beginLine = ident.beginLine;
		beginColumn = ident.beginColumn;
		endLine = ident.endLine;
		endColumn = ident.endColumn;
		image = ident.image;
	}
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

}
