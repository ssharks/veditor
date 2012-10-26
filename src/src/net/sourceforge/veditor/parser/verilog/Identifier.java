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
	private int dimension = 0;

	public Identifier(Token ident) {
		copy(ident);
	}

	public void copy(Token src) {
		beginLine = src.beginLine;
		beginColumn = src.beginColumn;
		endLine = src.endLine;
		endColumn = src.endColumn;
		image = src.image;
	}

	public void copy(Identifier src) {
		copy((Token) src);
		width = src.width;
		dimension = src.dimension;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}
}
