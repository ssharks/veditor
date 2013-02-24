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

import java.util.ArrayList;
import java.util.Iterator;

public class Expression {
	private static final int INVALID_WIDTH = -1;
	private static final int UNFIXED_WIDTH = 0; // used for non width indicated constant.

	private int width = INVALID_WIDTH;
	private boolean valid = false;
	private int intValue = 0;
	private String stringValue = null;
	private ArrayList<Identifier> references = null;

	public Expression() {
	}

	public Expression(int width) {
		setWidth(width);
	}

	public Expression(int width, int value) {
		setWidth(width);
		setValue(value);
	}

	public Expression(int width, String value) {
		setWidth(width);
		setValue(value);
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public int getVisibleWidth() {
		return isFixedWidth() ? getWidth() : 32;
	}

	public boolean isValidWidth() {
		return width != INVALID_WIDTH;
	}

	public boolean isFixedWidth() {
		return width != UNFIXED_WIDTH && width != INVALID_WIDTH;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValue(int value) {
		intValue = value;
		valid = true;
	}

	public void setValue(String value) {
		stringValue = value;
		valid = true;
	}
	
	public boolean isValidInt() {
		return stringValue == null;
	}

	public int intValue() {
		return intValue;
	}

	public String stringValue() {
		return stringValue;
	}

	public void addReference(Identifier ident) {
		if (references == null) {
			references = new ArrayList<Identifier>();
		}
		references.add(ident);
	}

	public void addReference(Expression exp) {
		if (exp.references != null) {
			Iterator<Identifier> iter = exp.references.iterator();
			while (iter.hasNext()) {
				addReference(iter.next());
			}
		}
	}

	public Identifier[] getReferences() {
		if (references == null)
			return null;
		else
			return references.toArray(new Identifier[0]);
	}

	public void parseIntegerLiteral(String image) {
		int idx = image.indexOf('\'');
		int width;
		if (idx < 0) {
			width = UNFIXED_WIDTH;
			setValue(parseInt(image, 10));
		} else {
			if (idx == 0)
				width = 32;
			else
				width = parseInt(image.substring(0, idx), 10);
			char rx = image.charAt(idx + 1);
			int radix = 10;
			switch (rx) {
			case 'h':
			case 'H':
				radix = 16;
				break;
			case 'o':
			case 'O':
				radix = 8;
				break;
			case 'b':
			case 'B':
				radix = 2;
				break;
			}
			try {
				setValue(parseInt(image.substring(idx + 2), radix));
			} catch (NumberFormatException e) {
				valid = false;
			}
		}
		setWidth(width);
	}

	private static int parseInt(String str, int radix) {
		str = str.replace("_", ""); // Verilog allows "_" in integer literal
		return Integer.parseInt(str, radix);
	}

	public void parseRealLiteral(String image) {
		setValue(image);
	}

	public void parseStringLiteral(String image) {
		setValue(image);
	}

	public String toString() {
		if (valid) {
			if (stringValue == null)
				return Integer.toString(intValue);
			else
				return stringValue;
		} else
			return "";
	}
}
