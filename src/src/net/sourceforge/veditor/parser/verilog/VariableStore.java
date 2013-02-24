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

import java.util.HashMap;
import java.util.Iterator;

public class VariableStore {

	public static class Symbol {
		private String name;
		private int line;
		private String[] types;
		private int width = 1;
		private int dimemsion = 0;
		private int intValue = 0;
		private String stringValue;
		private boolean assignd = false;
		private boolean used = false;

		Symbol(String name, int line, String[] types) {
			this.name = name;
			this.line = line;
			this.types = types;
		}

		public String getName() {
			return name;
		}

		public int getLine() {
			return line;
		}

		public String[] getTypes() {
			return types;
		}
		
		public boolean isParameter() {
			return types[0].equals("parameter")
					|| types[0].equals("localparam");
		}
		
		public boolean isVariable() {
			return types[0].equals("variable") || types[0].equals("port");
		}
		
		public boolean isPort() {
			return types[0].equals("port");
		}
		
		public boolean isTask() {
			return types[0].equals("task");
		}
		
		public boolean isFunction() {
			return types[0].equals("function");
		}
		
		public boolean isReg() {
			int idx = isPort() ? 2 : 1;
			if (idx >= types.length)
				return false;
			String modifier = types[idx];
			if (modifier.contains("reg") || modifier.contains("integer"))
				return true;
			else
				return false;
		}

		public boolean containsType(String word) {
			for (int i = 0; i < types.length; i++) {
				if (types[i].contains(word))
					return true;
			}
			return false;
		}

		public void setWidth(String bitRange) {
			String[] bit = bitRange.split("[:\\[\\]]");
			if (bit.length >= 3) {
				width = Integer.parseInt(bit[1]) - Integer.parseInt(bit[2]) + 1;
			}
		}
		
		public int getWidth() {
			return width;
		}
		
		public void setDimemsion(int dimemsion) {
			this.dimemsion = dimemsion;
		}

		public int getDimemsion() {
			return dimemsion;
		}

		public void setValue(int value) {
			this.intValue = value;
		}

		public void setValue(String value) {
			this.stringValue = value;
		}

		public int getValue() {
			return intValue;
		}

		boolean isValidInt() {
			return stringValue == null;
		}

		public String toString() {
			return stringValue;
		}

		public boolean isAssignd() {
			return assignd;
		}

		public void setAssignd() {
			this.assignd = true;
		}

		public boolean isUsed() {
			return used;
		}

		public void setUsed() {
			this.used = true;
		}

		public void addModifier(String mod) {
			if (types[0].equals("port")) {
				types[2] = mod + " ";
			} else {
				types[1] += mod + " ";
			}
		}
	}

	private HashMap<String, Symbol> symbols;

	public VariableStore() {
		symbols = new HashMap<String, Symbol>();
	}

	public Symbol addSymbol(String name, int line, String[] types,
			String bitRange) {
		return addSymbol(name, line, types, bitRange, 0);
	}

	public Symbol addSymbol(String name, int line, String[] types,
			String bitRange, int dim) {
		if (symbols.containsKey(name)) {
			return null; // redefinition error
		} else {
			Symbol sym = new Symbol(name, line, types);
			sym.setWidth(bitRange);
			sym.setDimemsion(dim);
			symbols.put(name, sym);
			return sym;
		}
	}

	public Symbol addAssignedVariable(String name) {
		Symbol sym = symbols.get(name);
		if (sym != null && sym.isVariable()) {
			sym.setAssignd();
			return sym;
		}
		return null;
	}

	public Symbol addUsedVariable(String name) {
		Symbol sym = symbols.get(name);
		if (sym != null && (sym.isParameter() || sym.isVariable())) {
			sym.setUsed();
			return sym;
		}
		return null;
	}

	public Symbol findSymbol(String name) {
		return symbols.get(name);
	}

	public Iterator<Symbol> iterator() {
		return symbols.values().iterator();
	}
}
