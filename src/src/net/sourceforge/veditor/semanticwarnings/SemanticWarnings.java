package net.sourceforge.veditor.semanticwarnings;

import java.util.Vector;

import org.eclipse.core.resources.IFile;
import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.parser.vhdl.*;
import net.sourceforge.veditor.semanticwarnings.VariableStore.DeclaredSymbol;

public class SemanticWarnings {
	private IFile m_File;
	private String m_entityname;
	private static int nwarningsgenerated=0;
	
	public SemanticWarnings(IFile f) {
		m_File=f;
	}
	
	// CodeBlock              | CodeBlock declaration              | CodeBlock statements
	// ASTarchitecture_body   | ASTarchitecture_declarative_part   | ASTarchitecture_statement_part
	// ASTblock_statement     | ASTblock_declarative_part          | ASTblock_statement_part
	// ASTsubprogram_body     | ASTsubprogram_declarative_part     | ASTsubprogram_statement_part
	// ASTgenerate_statement  | ASTgenerate_statement              | ASTgenerate_statement
	// ASTprocess_statement   | ASTprocess_declarative_part        | ASTprocess_statement_part
	
	private boolean isCodeBlock(SimpleNode node) {
		return node instanceof ASTarchitecture_body ||
				node instanceof ASTblock_statement ||
				node instanceof ASTsubprogram_body ||
				node instanceof ASTgenerate_statement ||
				node instanceof ASTprocess_statement;
	}
	
	@SuppressWarnings("unused")
    private boolean isCodeBlockDeclaration(SimpleNode node) {
		return node instanceof ASTarchitecture_declarative_part ||
				node instanceof ASTblock_declarative_part ||
				node instanceof ASTsubprogram_declarative_part ||
				node instanceof ASTgenerate_statement ||
				node instanceof ASTprocess_declarative_part;
	}
	
	private boolean isCodeBlockStatements(SimpleNode node) {
		return node instanceof ASTarchitecture_statement_part ||
				node instanceof ASTblock_statement_part ||
				node instanceof ASTsubprogram_statement_part ||
				node instanceof ASTgenerate_statement ||
				node instanceof ASTprocess_statement_part;
	}
	
	
	public void check(ASTdesign_file file) {
		VariableStore store = new VariableStore();
		analyze(file,store);
	}
	
	private void generateWarnings(DeclaredSymbol symbol, VariableStore assignedsymb) {
		String identif = symbol.name;
		if(!assignedsymb.isAssigned(identif)) {
			if(symbol.declarationType==DeclaredSymbol.DECLARATIONTYPE_SIGNAL)
				setWarningMarker(symbol.lineNr, "Signal "+identif+" is never assigned");
			if(symbol.declarationType==DeclaredSymbol.DECLARATIONTYPE_VARIABLE)
				setWarningMarker(symbol.lineNr, "Variable "+identif+" is never assigned");
			if(symbol.declarationType==DeclaredSymbol.DECLARATIONTYPE_INTERFACEOUTPUT)
				setWarningMarker(symbol.lineNr, "Entity output "+identif+" is never assigned");
		}
		if(!assignedsymb.isUsed(identif)) {
			if(symbol.declarationType==DeclaredSymbol.DECLARATIONTYPE_SIGNAL)
				setWarningMarker(symbol.lineNr, "Signal "+identif+" is never read");
			if(symbol.declarationType==DeclaredSymbol.DECLARATIONTYPE_SUBPROGRAM)
				setWarningMarker(symbol.lineNr, "Function "+identif+" is never used");
			if(symbol.declarationType==DeclaredSymbol.DECLARATIONTYPE_CONSTANT)
				setWarningMarker(symbol.lineNr, "Constant "+identif+" is never read");
			if(symbol.declarationType==DeclaredSymbol.DECLARATIONTYPE_VARIABLE)
				setWarningMarker(symbol.lineNr, "Variable "+identif+" is never read");
			if(symbol.declarationType==DeclaredSymbol.DECLARATIONTYPE_COMPONENT)
				setWarningMarker(symbol.lineNr, "Component "+identif+" is never used");
			if(symbol.declarationType==DeclaredSymbol.DECLARATIONTYPE_FULLTYPE)
				setWarningMarker(symbol.lineNr, "Type "+identif+" is never used");
			if(symbol.declarationType==DeclaredSymbol.DECLARATIONTYPE_INTERFACEINPUT)
				setWarningMarker(symbol.lineNr, "Entity input "+identif+" is never used");
			if(symbol.declarationType==DeclaredSymbol.DECLARATIONTYPE_INTERFACEGENERIC)
				setWarningMarker(symbol.lineNr, "Entity generic "+identif+" is never used");
		}
	}	
	
	private void setWarningMarker(int lineNumber,String msg)
	{
		nwarningsgenerated++;
		if(nwarningsgenerated < 1000) VerilogPlugin.setWarningMarker(m_File, lineNumber, msg);
	}
	
	public static void clearWarningsGenerated() {
		nwarningsgenerated=0;
	}
	
	private void analyze(SimpleNode node, VariableStore store) {
		//analyze has the declared symbols as input, 
		//updates the assigned and used symbols.
		if(isCodeBlock(node)) {
			VariableStore oldstore = store;
			
			store = new VariableStore(new Vector<String>(),new Vector<String>(),
						oldstore.getDeclaredSymbolsComplete(), new Vector<DeclaredSymbol>());
			
			if(node instanceof ASTarchitecture_body) {
				String archname=null;
				for(int i=0;i<node.getChildCount();i++) {
					if(node.getChild(i) instanceof ASTname) {
						archname = node.getChild(i).getLastToken().image;
					}
				}
				
				if(archname!=null && archname.equalsIgnoreCase(m_entityname)) {
					for(DeclaredSymbol symb:oldstore.getDeclaredSymbolsThis()) {
						if (symb.declarationType==DeclaredSymbol.DECLARATIONTYPE_INTERFACEINPUT ||
								symb.declarationType==DeclaredSymbol.DECLARATIONTYPE_INTERFACEOUTPUT ||
								symb.declarationType==DeclaredSymbol.DECLARATIONTYPE_INTERFACEBIDIR) {
							store.addDeclaredSymbol(symb);
						}
					}
					store.addUsedSymbols(oldstore.getUsedSymbols());
				}
			}
			
			for(int i=0;i<node.getChildCount();i++) {
				if(node.getChild(i) instanceof ASTidentifier) {
				}
				else if(node.getChild(i) instanceof ASTidentifier_list) {
				}
				else if (node.getChild(i) instanceof ASTsubprogram_specification) {
					addPortToDeclaredSymbols((ASTsubprogram_specification)node.getChild(i), store, false, true);
				} else {
					analyze(node.getChild(i),store);
				}
			}

			if(node instanceof ASTprocess_statement) {			
				checkProcess((ASTprocess_statement)node, store);
			}
			
			checkIfSignalsShouldBeMovedToSubBlock(store.getDeclaredSymbolsThis(),node);
			
			Vector<DeclaredSymbol> symbols = store.getDeclaredSymbolsThis();
			for(DeclaredSymbol symbol:symbols) {
				generateWarnings(symbol,store);
			}
			store.removeDeclaredSymbolsThis();
			oldstore.addAssignedSymbols(store.getAssignedSymbols());
			oldstore.addUsedSymbols(store.getUsedSymbols());
			store=oldstore;
			return;
		}
		else if(node instanceof ASTentity_declaration) {
			m_entityname = "";
			store.clear();
			addPortToDeclaredSymbols((ASTentity_declaration)node, store, false, false);
		}

		if(node instanceof ASTassociation_element) {
			for(int i=0;i<node.getChildCount();i++) {
				if(node.getChild(i) instanceof ASTactual_part) {
					Vector<String> identifiers = getAllIdentifiers(node);
					store.addUsedSymbols(identifiers);
					store.addAssignedSymbols(identifiers);
				}
			}
			return;
		}
		
		if( node instanceof ASTprocedure_call_statement ||
			node instanceof ASTfunction_call ||
			node instanceof ASTconcurrent_procedure_call_statement) {
			Vector<String> identifiers = getAllIdentifiers(node);
			store.addUsedSymbols(identifiers);
			store.addAssignedSymbols(identifiers);
			return;
		}

		
		int declarationtype = DeclaredSymbol.getDeclarationType(node);
		if(declarationtype!=DeclaredSymbol.DECLARATIONTYPE_UNKNOWN) {
			for(int i=0;i<node.getChildCount();i++) {
				if(node.getChild(i) instanceof ASTidentifier) {
					Token tok = node.getChild(i).getFirstToken();
					store.addDeclaredSymbol(new DeclaredSymbol(tok.toString(),tok.beginLine,declarationtype));
				}
				if(node.getChild(i) instanceof ASTidentifier_list) {
					for(int j=0;j<node.getChild(i).getChildCount();j++) {
						if(node.getChild(i).getChild(j) instanceof ASTidentifier) {
							Token tok = node.getChild(i).getChild(j).getFirstToken();
							store.addDeclaredSymbol(new DeclaredSymbol(tok.toString(),tok.beginLine,declarationtype));
						}
					}
				}
				if(declarationtype==DeclaredSymbol.DECLARATIONTYPE_SUBPROGRAM && node.getChild(i) instanceof ASTsubprogram_specification) {
					for(int j=0;j<node.getChild(i).getChildCount();j++) {
						if(node.getChild(i).getChild(j) instanceof ASTidentifier) {
							Token tok = node.getChild(i).getChild(j).getFirstToken();
							store.addDeclaredSymbol(new DeclaredSymbol(tok.toString(),tok.beginLine,declarationtype));
						}
					}
				}
			}
		}		
		
		if( node instanceof ASTsignal_declaration ||
			node instanceof ASTconstant_declaration	||
			node instanceof ASTcomponent_declaration ||
			node instanceof ASTconstant_declaration ||
			node instanceof ASTinterface_signal_declaration ||
			node instanceof ASTfull_type_declaration ||
			node instanceof ASTvariable_declaration ||
			node instanceof ASTcomponent_instantiation_statement
		) {
			for(int i=0;i<node.getChildCount();i++) {
				if(node.getChild(i) instanceof ASTidentifier) {
				}
				else if(node.getChild(i) instanceof ASTidentifier_list) {
				}
				else {
					analyze(node.getChild(i),store);
				}
			}
			return;
		}
		
		if( node instanceof ASTalias_declaration ||
			node instanceof ASTsubprogram_specification		
		) {
			for(int i=0;i<node.getChildCount();i++) {
				if(node.getChild(i) instanceof ASTalias_designator) {
				}
				else if(node.getChild(i) instanceof ASTidentifier) {
				}
				else {
					Vector<String> identifiers = getAllIdentifiers(node.getChild(i));
					store.addUsedSymbols(identifiers);
					store.addAssignedSymbols(identifiers);
				}
			}
			return;
		}

		if(	node instanceof ASTwait_statement ||
			node instanceof ASTfactor ||
			node instanceof ASTchoices ||
			node instanceof ASTterm ||
			node instanceof ASTcomposite_type_definition ||
			node instanceof ASTinstantiated_unit ||
			node instanceof ASTsubtype_indication ||
			node instanceof ASTgeneration_scheme ||
			node instanceof ASTiteration_scheme
		) {
			Vector<String> identifiers = getAllIdentifiers(node);
			store.addUsedSymbols(identifiers);
			return;
		}
		
		if( node instanceof ASTconditional_signal_assignment ||
			node instanceof ASTsignal_assignment_statement ||
			node instanceof ASTvariable_assignment_statement ||
			node instanceof ASTselected_signal_assignment 
		) {
			checkAssignmentSymbol(store, node);
			
			for(int i=0;i<node.getChildCount();i++) {
				if(node.getChild(i) instanceof ASTname) {
					for(int j=0;j<node.getChild(i).getChildCount();j++) {
						if(node.getChild(i).getChild(j) instanceof ASTsubtype_indication) {
							Vector<String> identifiers = getAllIdentifiers(node.getChild(i).getChild(j));
							store.addUsedSymbols(identifiers);
						} else if(node.getChild(i).getChild(j) instanceof ASTrange) {
							Vector<String> identifiers = getAllIdentifiers(node.getChild(i).getChild(j));
							store.addUsedSymbols(identifiers);
						} else if(node.getChild(i).getChild(j) instanceof ASTidentifier) {
							store.addAssignedSymbol(node.getChild(i).getChild(j).getFirstToken().toString());
						}
					}
				} else {
					Vector<String> identifiers = getAllIdentifiers(node.getChild(i));
					store.addUsedSymbols(identifiers);
				} 
			}
			return;
		}
		
		if( node instanceof ASTliteral ||
			node instanceof ASTsensitivity_list ||
			node instanceof ASTuse_clause ||
			node instanceof ASTformal_parameter_list ||
			node instanceof ASTport_clause
		) {
			return;			
		}
		
		if(node instanceof ASTidentifier) {
			return;
		}
		
		for(int i=0;i<node.getChildCount();i++) {
			analyze(node.getChild(i),store);
		}
	}

	
	private void addPortToDeclaredSymbols(SimpleNode node, VariableStore store, boolean isgeneric, boolean subprogram) {
		if(node instanceof ASTinterface_constant_declaration || node instanceof ASTinterface_signal_declaration || node instanceof ASTinterface_variable_declaration) {
			int declarationtype = (node instanceof ASTinterface_constant_declaration)?
					DeclaredSymbol.DECLARATIONTYPE_INTERFACEINPUT:
					DeclaredSymbol.DECLARATIONTYPE_INTERFACEOUTPUT;
			
			if (subprogram) declarationtype = DeclaredSymbol.DECLARATIONTYPE_INTERFACEINPUT;
			
			if(isgeneric) declarationtype = DeclaredSymbol.DECLARATIONTYPE_INTERFACEGENERIC;
			
			for(int i=0;i<node.getChildCount();i++) {
				if(node.getChild(i) instanceof ASTmode) {
					String mode = node.getChild(i).getFirstToken().image;
					if(mode.equalsIgnoreCase("in")) declarationtype=DeclaredSymbol.DECLARATIONTYPE_INTERFACEINPUT;
					if(mode.equalsIgnoreCase("inout")) declarationtype=DeclaredSymbol.DECLARATIONTYPE_INTERFACEBIDIR;
					if(mode.equalsIgnoreCase("out")) declarationtype=DeclaredSymbol.DECLARATIONTYPE_INTERFACEOUTPUT;
				}
			}
			
			// differentiate between subprogram and entity
			if (subprogram) {
				if (declarationtype == DeclaredSymbol.DECLARATIONTYPE_INTERFACEOUTPUT) {
					declarationtype = DeclaredSymbol.DECLARATIONTYPE_SUBPROGRAMOUTPUT;
				}
				if (declarationtype == DeclaredSymbol.DECLARATIONTYPE_INTERFACEBIDIR) {
					declarationtype = DeclaredSymbol.DECLARATIONTYPE_SUBPROGRAMBIDIR;
				}
			}
			
			for(int i=0;i<node.getChildCount();i++) {
				if(node.getChild(i) instanceof ASTidentifier) {
					Token tok = node.getChild(i).getFirstToken();
					store.addDeclaredSymbol(new DeclaredSymbol(tok.toString(),tok.beginLine,declarationtype));
				}
				else if(node.getChild(i) instanceof ASTidentifier_list) {
					for(int j=0;j<node.getChild(i).getChildCount();j++) {
						if(node.getChild(i).getChild(j) instanceof ASTidentifier) {
							Token tok = node.getChild(i).getChild(j).getFirstToken();
							store.addDeclaredSymbol(new DeclaredSymbol(tok.toString(),tok.beginLine,declarationtype));
						}
					}
				} else
					analyze(node.getChild(i),store);
			}
			return;
		}
		for(int i=0;i<node.getChildCount();i++) {
			if(node instanceof ASTentity_declaration && node.getChild(i) instanceof ASTidentifier) {
				m_entityname = node.getChild(i).getLastToken().image;
			}
			if(node instanceof ASTgeneric_clause) isgeneric=true;
			else if(!(node instanceof ASTinterface_list)) isgeneric=false;
			addPortToDeclaredSymbols(node.getChild(i),store,isgeneric, subprogram);
		}
	}

	private Vector<SimpleNode> getAllBaseIdentifierNodes(SimpleNode node) {
		if (node instanceof ASTname) {
			Vector<SimpleNode> result = new Vector<SimpleNode>();
			if (node.getChildCount() > 0) {
				if (node.getChild(0) instanceof ASTidentifier) {
					result.add(node.getChild(0));
				}
			}
			return result;
		}
		
		// do not go into expressions like "port'range"
		Vector<SimpleNode> result = new Vector<SimpleNode>();
		for(int i=0;i<node.getChildCount();i++) {
			result.addAll(getAllBaseIdentifierNodes(node.getChild(i)));
		}
		return result;
	}
	
	private Vector<String> getAllIdentifiers(SimpleNode node) {
		if(node instanceof ASTidentifier) {
			Vector<String> result = new Vector<String>();
			result.add(node.getFirstToken().toString());
			return result;
		}
		
		// do not go into expressions like "port'range"
		Vector<String> result = new Vector<String>();
		for(int i=0;i<node.getChildCount();i++) {
			result.addAll(getAllIdentifiers(node.getChild(i)));
		}
		return result;
	}

	private void checkIfSignalsShouldBeMovedToSubBlock(Vector<DeclaredSymbol> symbols, SimpleNode node) { //node is CodeBlock)
		if(!isCodeBlock(node)) return;
		
		SimpleNode statementsnode = node;
		for(int i=0;i<node.getChildCount();i++) {
			if (isCodeBlockStatements(node.getChild(i))) {
				statementsnode = node.getChild(i);
			}
		}
		if(!isCodeBlockStatements(statementsnode)) return;
		
		int[] Ncodeblockswheresignalused = new int[symbols.size()];
		String[] assignedinblock = new String[symbols.size()];
		for(int i=0;i<symbols.size();i++) {
			Ncodeblockswheresignalused[i]=0;
			assignedinblock[i]=new String();
		}
		
		for(int i=0;i<statementsnode.getChildCount();i++) {
			VariableStore store = new VariableStore();
			analyze(statementsnode.getChild(i),store);
			int increment = 2;
			String blockname = new String();
			if(statementsnode.getChild(i) instanceof ASTblock_statement) {
				increment = 1;
				for(int j=0;j<statementsnode.getChild(i).getChildCount();j++) {
					if(statementsnode.getChild(i).getChild(j) instanceof ASTidentifier) {
						blockname = statementsnode.getChild(i).getChild(j).getLastToken().toString();
					}
				}
			}
			for(int j=0;j<symbols.size();j++) {
				if(store.isUsed(symbols.get(j).name) || store.isAssigned(symbols.get(j).name)) {
					assignedinblock[j] = blockname;
					Ncodeblockswheresignalused[j]+=increment;
				}
			}
		}
		
		for(int j=0;j<symbols.size();j++) {
			if(Ncodeblockswheresignalused[j]==1 && symbols.get(j).declarationType==DeclaredSymbol.DECLARATIONTYPE_SIGNAL) {
				VerilogPlugin.setWarningMarker(m_File, symbols.get(j).lineNr, "Signal "+symbols.get(j).name+" only used in block "+assignedinblock[j]);
			}
		}		
	}
	
	private void checkProcess(ASTprocess_statement node, VariableStore store) {
		//printTokenTree(node,0);
		Vector<String> sensitivlist = new Vector<String>();
		for(int i=0;i<node.getChildCount();i++) {
			if(node.getChild(i) instanceof ASTsensitivity_list) {
				sensitivlist = getAllIdentifiers(node.getChild(i));
			}
		}
		if(sensitivlist.size()==0) return;

		for(int i=0;i<node.getChildCount();i++) {
			if(node.getChild(i) instanceof ASTprocess_statement_part) {
				for(int j=0;j<node.getChild(i).getChildCount();j++) {
					checkCombinatorialPaths(node.getChild(i).getChild(j),sensitivlist,store);
				}
			}	
		}
	}

	private void checkCombinatorialPaths(SimpleNode node, Vector<String> sensitivlist, VariableStore store) {
		if(node instanceof ASTif_statement) {
			for(int i=0;i<node.getChildCount();i+=2) {
				if(!(node.getChild(i+1) instanceof ASTsequence_of_statements)) {
					return;
				}
				
				boolean stopcombinpath = false;
				Vector<String> identifiers = getAllIdentifiers(node.getChild(i));
				for(String str:identifiers) {
					if(str.equalsIgnoreCase("rising_edge")) stopcombinpath=true;
					if(str.equalsIgnoreCase("falling_edge")) stopcombinpath=true;
					if(str.equalsIgnoreCase("event")) stopcombinpath=true;					
				}
				checkCombinatorialPaths(node.getChild(i),sensitivlist,store);
				if(!stopcombinpath) {
					checkCombinatorialPaths(node.getChild(i+1),sensitivlist,store);
					if(i+2==node.getChildCount()-1) {
						checkCombinatorialPaths(node.getChild(i+2),sensitivlist,store);
					}
				}
			}
			return;
		}
		
		VariableStore newstore = new VariableStore();
		analyze(node,newstore);
		Vector<String> usedsymb = newstore.getUsedSymbols();
		Vector<DeclaredSymbol> declared = store.getDeclaredSymbolsComplete();
		
		for(int i=0;i<usedsymb.size();i++) {
			boolean found = false;
			for(int j=0;j<sensitivlist.size();j++) {
				if(sensitivlist.get(j).equalsIgnoreCase(usedsymb.get(i))) {
					found=true;
				}
			}
			boolean issignal = false;
			for(int j=0;j<declared.size();j++) {
				if(declared.get(j).name.equalsIgnoreCase(usedsymb.get(i))) {
					if(declared.get(j).declarationType==DeclaredSymbol.DECLARATIONTYPE_SIGNAL ||
							declared.get(j).declarationType==DeclaredSymbol.DECLARATIONTYPE_INTERFACEINPUT)
					issignal=true;
				}
			}
			if(!found && issignal) {
				VerilogPlugin.setWarningMarker(m_File, node.getFirstToken().beginLine, "Signal "+usedsymb.get(i)+" not in sensitivity list");	
			}
		}
	}

	// check if it is not an identifier in the order of 'length
	private boolean checkForSignature(SimpleNode node) {
		Node parent = node.jjtGetParent();
		if (parent != null) {
			if (parent instanceof ASTname) {
				ASTname astParent = (ASTname)parent;
				for(int i=0;i<astParent.getChildCount();i++) {
					if (astParent.getChild(i) instanceof ASTsignature) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// check for incorrect assignment statements, line using := to assign a signal or <= to assign a variable
	private void checkAssignmentSymbol(VariableStore store, SimpleNode node) {
		if (node instanceof ASTsignal_assignment_statement ||
			node instanceof ASTvariable_assignment_statement ||
			node instanceof ASTconditional_signal_assignment) {
			for(int i=0;i<node.getChildCount()-1;i++) {
				Vector<DeclaredSymbol> declared = store.getDeclaredSymbolsComplete();
				if (node.getChild(i) instanceof ASTname) {
					String assignedName = node.getChild(i).getFirstToken().toString();
					checkAssignmentType(node, declared, assignedName);
					
					// find in the inputs for the expression if there are any outputs used
					Vector<SimpleNode> inputName = getAllBaseIdentifierNodes(node.getChild(i+1));
					
					// check if not of the inputs is an output of the entity
					for (int k=0;k<inputName.size();k++) {
						// walk backwards through the list to give the highest priority to local symbols
						for(int j=declared.size()-1;j>=0;j--) {
							if(declared.get(j).name.equalsIgnoreCase(inputName.get(k).getFirstToken().toString())) {
								if ((declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_INTERFACEOUTPUT) 
										&& (checkForSignature(inputName.get(k)) == false)) {
									VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "Output " + inputName.get(k).getFirstToken().toString() +" cannot be used in an expression ");
								}
								if ((declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_SUBPROGRAMOUTPUT)  
										&& (checkForSignature(inputName.get(k)) == false)) {
									VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "Output " + inputName.get(k).getFirstToken().toString() +" cannot be used in an expression ");
								}
								break;
							}
						}
					}
				}
			}
		}
	}

	// check whether assignments are allowed to the affected type
	private void checkAssignmentType(SimpleNode node,
			Vector<DeclaredSymbol> declared, String assignedName) {
		for(int j=declared.size()-1;j>=0;j--) {
			if(declared.get(j).name.equalsIgnoreCase(assignedName)) {
				
				// check for incorrectly assigning using a variable statement
				if (node instanceof ASTvariable_assignment_statement) {
					if (declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_INTERFACEOUTPUT){
						VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "Output " + assignedName + " assigned using :=, use <= instead");
					}
					if (declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_INTERFACEBIDIR){
						VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "InOutput " + assignedName + " assigned using :=, use <= instead");
					}
					if (declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_SIGNAL){
						VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "Signal " + assignedName + " assigned using :=, use <= instead");
					}
				}
				
				// check for incorrectly assigning using a signal statement
				if (node instanceof ASTsignal_assignment_statement) {
					if (declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_VARIABLE){
						VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "Variable " + assignedName + " assigned using <=, use := instead");
					}
				}
				
				// check for declarations that cannot be assigned at all
				if (declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_INTERFACEINPUT) {
					VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "Cannot assign to an input " + assignedName);
				}
				if (declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_CONSTANT) {
					VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "Cannot assign to a constant " + assignedName);
				}
				if (declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_INTERFACEGENERIC) {
					VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "Cannot assign to a generic " + assignedName);
				}
				if (declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_COMPONENT) {
					VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "Cannot assign to a component " + assignedName);
				}
				if (declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_FULLTYPE) {
					VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "Cannot assign to a type " + assignedName);
				}
				if (declared.get(j).declarationType == DeclaredSymbol.DECLARATIONTYPE_SUBPROGRAM) {
					VerilogPlugin.setErrorMarker(m_File, node.getFirstToken().beginLine, "Cannot assign to a procedure or a function " + assignedName);
				}
				// break when found one, to prevent duplicate warnings
				break;
			}
		}
	}
}