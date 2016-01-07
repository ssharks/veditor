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

package net.sourceforge.veditor.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class VariableStore {
	
	public static class Position {
		public int line;
		public int column;

		Position(int line, int column) {
			this.line = line;
			this.column = column;
		}
	}

	public static class Symbol {
		private String name;
		private Position pos;
		private String[] types;
		private int width = 1;
		private int dimemsion = 0;
		private int intValue = 0;
		private String stringValue;
		private List<Position> assigned = new ArrayList<Position>();
		private List<Position> used = new ArrayList<Position>();
		private List<String> connections = null;

		Symbol(String name, int line, int col, String[] types) {
			this.name = name;
			this.pos = new Position(line, col);
			this.types = types;
		}

		public String getName() {
			return name;
		}

		public Position getPosition() {
			return pos;
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

		public String[] getConnections() {
			if (connections == null)
				return null;
			return connections.toArray(new String[0]);
		}

		public void addConnection(String connection) {
			if (connections == null)
				connections = new ArrayList<String>();
			connections.add(connection);
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

		public boolean isValidInt() {
			return stringValue == null;
		}

		public String toString() {
			return stringValue;
		}

		public boolean isAssigned() {
			return assigned.isEmpty() == false;
		}

		public List<Position> getAssigned() {
			return assigned;
		}

		public void setAssigned(int line, int column) {
			assigned.add(new Position(line, column));
		}

		public boolean isUsed() {
			return used.isEmpty() == false;
		}
		
		public List<Position> getUsed() {
			return used;
		}

		public void setUsed(int line, int column) {
			used.add(new Position(line, column));
		}

		public void addModifier(String mod) {
			if (types[0].equals("port")) {
				types[2] = mod + " ";
			} else {
				types[1] += mod + " ";
			}
		}
	}

	private static class SymbolScope extends HashMap<String, Symbol> {
		private String moduleName;
		private int beginLine, endLine;

		public SymbolScope(String name, int line) {
			moduleName = name;
			beginLine = line;
		}

		public void end(int line) {
			endLine = line;
		}
		
		public boolean hasLine(int line) {
			return (beginLine <= line && line <= endLine);
		}
	}
	
	private SymbolScope symbols;
	private List<SymbolScope> scopeList = new ArrayList<SymbolScope>();

	public VariableStore() {
	}
	
	public void openScope(String name, int line) {
		symbols = new SymbolScope(name, line);
	}
	
	public void closeScope(int line) {
		symbols.end(line);
		scopeList.add(symbols);
		symbols = null;
	}

	public Symbol addSymbol(String name, int line, int col, String[] types,
			String bitRange) {
		return addSymbol(name, line, col, types, bitRange, 0);
	}

	public Symbol addSymbol(String name, int line, int col, String[] types,
			String bitRange, int dim) {
		if (symbols.containsKey(name)) {
			return null; // redefinition error
		} else {
			Symbol sym = new Symbol(name, line, col, types);
			sym.setWidth(bitRange);
			sym.setDimemsion(dim);
			symbols.put(name, sym);
			return sym;
		}
	}

	public Symbol addAssignedVariable(String name, int line, int col, List<String> generateBlock) {
		Symbol sym = getVariableSymbol(name, generateBlock);
		if (sym != null && sym.isVariable()) {
			sym.setAssigned(line, col);
			return sym;
		}
		return null;
	}

	public Symbol addUsedVariable(String name, int line, int col, List<String> generateBlock) {
		Symbol sym = getVariableSymbol(name, generateBlock);
		if (sym != null && (sym.isParameter() || sym.isVariable())) {
			sym.setUsed(line, col);
			return sym;
		}
		return null;
	}

	public Symbol getVariableSymbol(String name, List<String> generateBlock) {
		// The scope of generate block is prior than module scope
		String head = "";
		Symbol sym = null;
		for (int i = 0; i < generateBlock.size() && sym == null; i++) {
			head += generateBlock.get(i) + ".";
			sym = symbols.get(head + name);
		}
		if (sym == null) {
			sym = symbols.get(name);
		}
		return sym;
	}

	public void addConnection(String name, List<String> generateBlock,
			String connection) {
		Symbol sym = getVariableSymbol(name, generateBlock);
		if (sym != null) {
			sym.addConnection(connection);
		}
	}

	public Symbol findSymbol(String name) {
		return symbols.get(name);
	}

	public Collection<Symbol> collection() {
		List<Symbol> ret = new ArrayList<Symbol>();
		for(SymbolScope s : scopeList) {
			ret.addAll(s.values());
		}
		return ret;
	}

	public void findOccurrenceList(List<Integer> readList, List<Integer> writeList, String name, int line, IDocument doc) {
		for (SymbolScope scope : scopeList) {
			if (scope.hasLine(line)) {
				String blockName = findBlockName(scope, name, line);
				if (blockName != null) {
					for (Symbol sym : scope.values()) {
						if (blockName.equals(sym.getName())) {
							Position defpos = sym.getPosition();
							String types[] = sym.getTypes();
							if (types[0].equals("port") && types[1].equals("input")) {
								addOccurrenceOffset(writeList, blockName, defpos, doc);
							} else {
								addOccurrenceOffset(readList, blockName, defpos, doc);
							}
							for (Position pos : sym.getUsed()) {
								addOccurrenceOffset(readList, blockName, pos, doc);
							}
							for (Position pos : sym.getAssigned()) {
								addOccurrenceOffset(writeList, blockName, pos, doc);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * find generate block name such as "block.sig"
	 */
	private String findBlockName(SymbolScope scope, String name, int line) {
		line++; // convert to Verilog parser line
		for (Symbol sym : scope.values()) {
			if (name.equals(sym.getName()) || sym.getName().lastIndexOf("." + name) >= 0) {
				if (sym.getPosition().line == line) {
					return sym.getName();
				}
				for (Position pos : sym.getUsed()) {
					if (pos.line == line) {
						return sym.getName();
					}
				}
				for (Position pos : sym.getAssigned()) {
					if (pos.line == line) {
						return sym.getName();
					}
				}
			}
		}
		return null;
	}

	private void addOccurrenceOffset(List<Integer> list, String name, Position pos, IDocument doc) {
		try {
			int offset = doc.getLineOffset(pos.line - 1) + pos.column - 1;
			list.add(offset);
		} catch (BadLocationException e) {
			// if cannot find, ignore simply
		}
	}
}

