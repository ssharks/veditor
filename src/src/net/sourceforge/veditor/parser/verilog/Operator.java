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

	// semantic warning.
	private static final String COMPARE_WIDTH_MISMATCH = "Compare bit width mismatch: %d and %d";
	private static final String CONDITIONAL_WIDTH_MISMATCH = "Conditional operator bit width mismatch: %d ? %d : %d";
	private static final String LOGICAL_WIDTH_MISMATCH = "Logical operator bit width mismatch: %d and %d";

	private static VerilogParser.Preferences preferences = null;

	public static void setPreferences(VerilogParser.Preferences p) {
		preferences = p;
	}

	private String image;
	private String warning = null;

	public Operator(String image) {
		this.image = image;
	}
	
	public boolean isWarning() {
		return warning != null;
	}
	
	public String getWarning() {
		return warning;
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
	
	private static final String opLeft = " / >> << ** >>> <<< ";
	private static final String opLog = " && || ";
	private static final String opCmp = " == != === !== < <= > >= ";
	
	public Expression operate(Expression arg1, Expression arg2) {
		String key = " " + image + " ";
		int width1 = arg1.getWidth();
		int width2 = arg2.getWidth();
		int width = 0;
		if (image.equals("*")) {
			width = width1 + width2;
		} else if (image.equals("%")) {
			width = width2;
		} else if (opLeft.contains(key)) {
			width = width1;
		} else if (opLog.contains(key)) {
			if (width1 != 1 || width2 != 1) {
				warning = String.format(LOGICAL_WIDTH_MISMATCH,
						arg1.getVisibleWidth(), arg2.getVisibleWidth());
			}
			width = 1;
		} else if (opCmp.contains(key)) {
			if (width1 != width2 && arg1.isFixedWidth() && arg2.isFixedWidth()) {
				warning = String.format(COMPARE_WIDTH_MISMATCH,
						arg1.getVisibleWidth(), arg2.getVisibleWidth());
			}
			width = 1;
		} else if (width2 == 32 && arg2.isValid()) {
			width = width1;
		} else {
			width = (width1 > width2) ? width1 : width2;
		}
		
		if (arg1.isValid() && arg2.isValid()) {
			int value = 0;
			if (arg1.isValidInt()) {
				value = getValue2(arg1.intValue(), arg2.intValue());
			} else {
				value = getValue2(arg1.stringValue(), arg2.stringValue());
			}
			return new Expression(width, value);
		} else {
			return new Expression(width);
		}
	}
	
	public Expression operate(Expression cond, Expression arg1, Expression arg2) {
		int widthc = cond.getVisibleWidth();
		int width1 = arg1.getVisibleWidth();
		int width2 = arg2.getVisibleWidth();
		if (preferences.intConst == false && widthc == 32 && cond.isValid()) {
			// assume constant integer as one bit width
			widthc = 1;
		}
		if (widthc != 1 || width1 != width2) {
			if (arg1.isFixedWidth() && arg2.isFixedWidth()) {
				warning = String.format(CONDITIONAL_WIDTH_MISMATCH, widthc,
						width1, width2);
			}
		}
		if (cond.isValid()) {
			Expression ref = (cond.intValue() != 0) ? arg1 : arg2;
			if (ref.isValid()) {
				return new Expression(ref.getWidth(), ref.intValue());
			} else {
				return new Expression(ref.getWidth());
			}
		} else {
			return new Expression(arg1.getWidth());
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
		if (image.equals("/")) {
			if (value2 == 0)
				return 0;
			else
				return value1 / value2;
		}
		if (image.equals("%")) {
			if (value2 == 0)
				return 0;
			else
				return value1 % value2;
		}
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

	private int getValue2(String value1, String value2) {
		if (image.equals("==") || image.equals("==="))
			return value1.equals(value2) ? 1 : 0;
		if (image.equals("!=") || image.equals("!=="))
			return value1.equals(value2) ? 0 : 1;
		return 0;
	}
}
