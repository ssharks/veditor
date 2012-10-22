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
	public static final int INVALID_WIDTH = 0;

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

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public boolean isValidWidth() {
		return width != INVALID_WIDTH;
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

	public Expression operate(String operator) {
		Operator op = new Operator(operator);
		return op.operate(this);
	}

	public Expression operate(String operator, Expression arg) {
		Operator op = new Operator(operator);
		return op.operate(this, arg);
	}

	public void parseLiteral(String image) {
		int idx = image.indexOf('\'');
		int width;
		if (idx < 0) {
			width = 32;
			setValue(Integer.parseInt(image));
		} else {
			if (idx == 0)
				width = 32;
			else
				width = Integer.parseInt(image.substring(0, idx));
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
				setValue(Integer.parseInt(image.substring(idx + 2), radix));
			} catch (NumberFormatException e) {
				valid = false;
			}
		}
		setWidth(width);
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
