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

public class Operator {

	private String image;

	public Operator(String image) {
		this.image = image;
	}
	
	public Expression operate(Expression arg) {
		int width;
		if (image.equals("~") || image.equals("+") || image.equals("-"))
			width = arg.getWidth();
		else
			width = 1;
		
		if (arg.isValid()) {
			int value = getValue1(arg.intValue(), arg.getWidth());
			return new Expression(width, value);
		} else {
			return new Expression(width);
		}
	}
	
	public Expression operate(Expression arg1, Expression arg2) {
		String opLeft = " / >> << ** >>> <<< ";
		String op1 = " == != === !== && || < <= > >= ";
		int width1 = arg1.getWidth();
		int width2 = arg2.getWidth();

		String key = " " + image + " ";
		int width = 0;
		if (image.equals("*"))
			width = width1 + width2;
		else if (image.equals("%"))
			width = width2;
		else if (opLeft.contains(key))
			width = width1;
		else if (op1.contains(key))
			width = 1;
		else if (width2 == 32 && arg2.isValid())
			width = width1;
		else
			width = (width1 > width2) ? width1 : width2;
		
		if (arg1.isValid() && arg2.isValid()) {
			int value = getValue2(arg1.intValue(), arg2.intValue());
			return new Expression(width, value);
		} else {
			return new Expression(width);
		}
	}

	private int getValue1(int value1, int width1) {
		if (image.equals("~"))
			return ~value1;
		if (image.equals("+"))
			return value1;
		if (image.equals("-"))
			return -value1;
		if (image.equals("!"))
			return (value1 == 0) ? 1 : 0;
		if (image.equals("&")) {
			int pat = (1 << width1) - 1;
			return (value1 == pat) ? 1 : 0;
		}
		if (image.equals("|"))
			return (value1 == 0) ? 0 : 1;
		if (image.equals("~&")) {
			int pat = (1 << width1) - 1;
			return (value1 == pat) ? 0 : 1;
		}
		if (image.equals("~|"))
			return (value1 == 0) ? 1 : 0;
		if (image.equals("^")) {
			boolean ret = false;
			for (int i = 0; i < width1; i++) {
				if ((value1 & (1 << i)) != 0)
					ret = !ret;
			}
			return ret ? 1 : 0;
		}
		if (image.equals("~^") || image.equals("^~")) {
			boolean ret = false;
			for (int i = 0; i < width1; i++) {
				if ((value1 & (1 << i)) != 0)
					ret = !ret;
			}
			return ret ? 0 : 1;
		}
		return 0;
	}

	private int getValue2(int value1, int value2) {
		if (image.equals("+"))
			return value1 + value2;
		if (image.equals("-"))
			return value1 - value2;
		if (image.equals("&"))
			return value1 & value2;
		if (image.equals("|"))
			return value1 | value2;
		if (image.equals("^"))
			return value1 ^ value2;
		if (image.equals("~^") || image.equals("^~"))
			return ~(value1 ^ value2);
		if (image.equals("==") || image.equals("==="))
			return (value1 == value2) ? 1 : 0;
		if (image.equals("!=") || image.equals("!=="))
			return (value1 != value2) ? 1 : 0;
		if (image.equals("&&"))
			return (value1 != 0 && value2 != 0) ? 1 : 0;
		if (image.equals("||"))
			return (value1 != 0 || value2 != 0) ? 1 : 0;
		if (image.equals("<"))
			return (value1 < value2) ? 1 : 0;
		if (image.equals("<="))
			return (value1 <= value2) ? 1 : 0;
		if (image.equals(">"))
			return (value1 > value2) ? 1 : 0;
		if (image.equals(">="))
			return (value1 >= value2) ? 1 : 0;
		if (image.equals("*"))
			return value1 * value2;
		if (image.equals("/"))
			if (value2 == 0)
				return 0;
			else
				return value1 / value2;
		if (image.equals("%"))
			return value1 % value2;
		if (image.equals(">>") || image.equals(">>>"))
			return value1 >> value2;
		if (image.equals("<<") || image.equals("<<<"))
			return value1 >> value2;
		if (image.equals("**")) {
			// power
			int ret = 1;
			for (int i = 0; i < value2; i++) {
				ret *= value1;
			}
			return ret;
		}
		return 0;
	}
}
