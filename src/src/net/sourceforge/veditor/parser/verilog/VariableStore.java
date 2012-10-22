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
		private String type;
		private int width;
		private int value;
		private boolean assignd;
		private boolean used;

		Symbol(String name, int line, String type, int width, int value) {
			this.name = name;
			this.line = line;
			this.type = type;
			this.width = width;
			this.value = value;
			this.assignd = false;
			this.used = false;
			
			if (isParameter()) {
				this.assignd = true;
			}
		}

		public String getName() {
			return name;
		}
		
		public int getLine() {
			return line;
		}

		public String getType() {
			return type;
		}
		
		public boolean isParameter() {
			return type.startsWith("parameter") || type.startsWith("localparam");
		}
		
		public boolean containsType(String word) {
			return type.contains(word);
		}
		
		public int getWidth() {
			return width;
		}

		public int getValue() {
			return value;
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
			String[] types = type.split("#");
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

	public boolean addSymbol(String name, int line, String type, String bitRange) {
		return addSymbol(name, line, type, bitRange, 0);
	}

	public boolean addSymbol(String name, int line, String type, String bitRange,
			int value) {
		if (symbols.containsKey(name)) {
			return false; // redefinition error
		} else {
			int width = 1;
			String[] bit = bitRange.split("[:\\[\\]]");
			if (bit.length >= 3) {
				width = Integer.parseInt(bit[1]) - Integer.parseInt(bit[2]);
			}
			symbols.put(name, new Symbol(name, line, type, width, value));
			return true;
		}
	}

	public Symbol addAssignedSymbol(String name) {
		Symbol sym = symbols.get(name);
		if (sym != null) {
			sym.setAssignd();
		}
		return sym;
	}

	public Symbol addUsedSymbol(String name) {
		Symbol sym = symbols.get(name);
		if (sym != null) {
			sym.setUsed();
		}
		return sym;
	}

	public Symbol findSymbol(String name) {
		return symbols.get(name);
	}

	public Iterator<Symbol> iterator() {
		return symbols.values().iterator();
	}
}
