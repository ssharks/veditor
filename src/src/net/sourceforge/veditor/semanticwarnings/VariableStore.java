package net.sourceforge.veditor.semanticwarnings;

import java.util.Vector;

import net.sourceforge.veditor.parser.vhdl.*;

public class VariableStore {
	public static class DeclaredSymbol {
		public static final int DECLARATIONTYPE_UNKNOWN = 0;
		public static final int DECLARATIONTYPE_COMPONENT = 1;
		public static final int DECLARATIONTYPE_SIGNAL = 2;
		public static final int DECLARATIONTYPE_SUBPROGRAM = 3;
		public static final int DECLARATIONTYPE_CONSTANT = 4;
		public static final int DECLARATIONTYPE_VARIABLE = 5;
		public static final int DECLARATIONTYPE_FULLTYPE = 6;
		public static final int DECLARATIONTYPE_INTERFACEINPUT = 7;
		public static final int DECLARATIONTYPE_INTERFACEOUTPUT = 8;
		public static final int DECLARATIONTYPE_INTERFACEGENERIC = 9;
		
		DeclaredSymbol(String n,int l,int d) {
			name=n;
			lineNr=l;
			declarationType=d;
		}
		
		public String name;
		public int lineNr;
		public int declarationType;
		
		public static int getDeclarationType(SimpleNode node) {
			int declarationtype = DECLARATIONTYPE_UNKNOWN;
			if(node instanceof ASTcomponent_declaration) declarationtype=DECLARATIONTYPE_COMPONENT;
			if(node instanceof ASTsignal_declaration) declarationtype=DECLARATIONTYPE_SIGNAL;
			if(node instanceof ASTsubprogram_body) declarationtype=DECLARATIONTYPE_SUBPROGRAM;
			if(node instanceof ASTconstant_declaration) declarationtype=DECLARATIONTYPE_CONSTANT;
			if(node instanceof ASTfull_type_declaration) declarationtype=DECLARATIONTYPE_FULLTYPE;
			if(node instanceof ASTvariable_declaration) declarationtype=DECLARATIONTYPE_VARIABLE;
			return declarationtype;
		}
	}
	
	
	public VariableStore() {
		assignedsymbols = new Vector<String>();
		usedsymbols = new Vector<String>();
		declaredsymbols_this = new Vector<DeclaredSymbol>();
		declaredsymbols_parent = new Vector<DeclaredSymbol>();
	}
	
	public VariableStore(Vector<String> assigned, Vector<String> used,
			Vector<DeclaredSymbol> decl_par, Vector<DeclaredSymbol> decl_this) {
		assignedsymbols = new Vector<String>(assigned);
		usedsymbols = new Vector<String>(used);
		declaredsymbols_parent = new Vector<DeclaredSymbol>(decl_par);
		declaredsymbols_this = new Vector<DeclaredSymbol>(decl_this);
	}
	
	public VariableStore(VariableStore a) {
		assignedsymbols = new Vector<String>(a.assignedsymbols);
		usedsymbols = new Vector<String>(a.usedsymbols);
		declaredsymbols_parent = new Vector<DeclaredSymbol>(a.declaredsymbols_parent);
		declaredsymbols_this = new Vector<DeclaredSymbol>(a.declaredsymbols_this);
	}
	
	public boolean isAssigned(String name) {
		for(String s:assignedsymbols) {
			if(name.equalsIgnoreCase(s)) return true;
		}
		return false;
	}
	
	public boolean isUsed(String name) {
		for(String s:usedsymbols) {
			if(name.equalsIgnoreCase(s)) return true;
		}
		return false;
	}
		
	public void addUsedSymbol(String s) {
		if(!isUsed(s)) usedsymbols.add(new String(s));
	}
	
	public void addAssignedSymbol(String s) {
		if(!isAssigned(s)) assignedsymbols.add(new String(s));
	}

	public void addAssignedSymbols(Vector<String> strings) {
		for(String s:strings) addAssignedSymbol(s);
	}	

	public void addUsedSymbols(Vector<String> strings) {
		for(String s:strings) addUsedSymbol(s);
	}

	public void addSymbols(VariableStore b) {
		addAssignedSymbols(b.assignedsymbols);
		addUsedSymbols(b.usedsymbols);
	}
	
	public String toString() {
		String result = "Assigned symbols:\n";
		for(int i=0;i<assignedsymbols.size();i++) {
			result+="\t"+assignedsymbols.get(i)+"\n";
		}
		result += "Used symbols:\n";
		for(int i=0;i<usedsymbols.size();i++) {
			result+="\t"+usedsymbols.get(i)+"\n";
		}
		result += "Declared symbols parent:\n";
		for(int i=0;i<declaredsymbols_parent.size();i++) {
			result+="\t("+i+") "+declaredsymbols_parent.get(i).name+"\n";
		}
		result += "Declared symbols this:\n";
		for(int i=0;i<declaredsymbols_this.size();i++) {
			result+="\t("+i+") "+declaredsymbols_this.get(i).name+"\n";
		}	
		return result;
	}
	
	public void addDeclaredSymbol(DeclaredSymbol symb) {
		declaredsymbols_this.add(symb);		
	}
	
	public Vector<DeclaredSymbol> getDeclaredSymbolsParent() {
		return declaredsymbols_parent;
	}
	
	public Vector<DeclaredSymbol> getDeclaredSymbolsThis() {
		return declaredsymbols_this;
	}
	
	public Vector<DeclaredSymbol> getDeclaredSymbolsComplete() {
		Vector<DeclaredSymbol> result=new Vector<DeclaredSymbol>();
		result.addAll(declaredsymbols_parent);
		result.addAll(declaredsymbols_this);
		return result;
	}
	
	public void clear() {
		assignedsymbols = new Vector<String>();
		usedsymbols = new Vector<String>();
		declaredsymbols_this = new Vector<DeclaredSymbol>();
		declaredsymbols_parent = new Vector<DeclaredSymbol>();		
	}

	public void removeDeclaredSymbolsThis() {
		for(DeclaredSymbol s: declaredsymbols_this) {
			for(int index=0;index<usedsymbols.size();index++) {
				if(usedsymbols.get(index).equalsIgnoreCase(s.name)) {
					usedsymbols.remove(index);
					index--;
				}
			}
			for(int index=0;index<assignedsymbols.size();index++) {
				if(assignedsymbols.get(index).equalsIgnoreCase(s.name)) {
					assignedsymbols.remove(index);
					index--;
				}
			}
		}
	}
	
	public Vector<String> getAssignedSymbols() {
		return assignedsymbols;
	}
	public Vector<String> getUsedSymbols() {
		return usedsymbols;
	}	
	
	private Vector<String> assignedsymbols;
	private Vector<String> usedsymbols;
	private Vector<DeclaredSymbol> declaredsymbols_parent;
	private Vector<DeclaredSymbol> declaredsymbols_this;


}
