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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.OutlineContainer;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.OutlineElementFactory;
import net.sourceforge.veditor.parser.VariableStore;
import net.sourceforge.veditor.parser.OutlineContainer.Collapsible;
import net.sourceforge.veditor.parser.ParserReader;
import net.sourceforge.veditor.preference.PreferenceStrings;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;

/**
 * implementation class of VerilogParserCore<p/>
 * for separating definition from JavaCC code 
 */
public class VerilogParser extends VerilogParserCore implements IParser, PreferenceStrings
{
	// these are semantic error.
	private static final String DUPLICATE_PARAM = "Duplicate parameter %s";
	private static final String DUPLICATE_SIGNAL = "Duplicate signal %s";
	private static final String DUPLICATE_TASK = "Duplicate task %s";
	private static final String DUPLICATE_FUNCTION = "Duplicate function %s";
	private static final String CANNOT_RESOLVED_TASK = "%s cannot be resolved to a task";
	private static final String CANNOT_RESOLVED_FUNCTION = "%s cannot be resolved to a function";
	private static final String ASSIGN_WIRE = "The wire %s assign in initial or always block";
	private static final String ASSIGN_REG = "The reg %s assign in assign statement";

	// these are semantic warning.
	private static final String CANNOT_RESOLVED_SIGNAL = "%s cannot be resolved to a signal or parameter";
	private static final String NOT_ASSIGNED_AND_USED = "The signal %s is not assigned and used";
	private static final String NOT_USED = "The value of %s is not used";
	private static final String NOT_USED_TASK = "The task %s is not used";
	private static final String NOT_USED_FUNCTION = "The function %s is not used";
	private static final String NOT_ASSIGNED = "The signal %s is not assigned";
	private static final String REG_CONNECT_OUTPUT = "The register %s cannot be connected to output port";
	private static final String ASSIGN_WIDTH_MISMATCH = "Assignment bit width mismatch: %s";
	private static final String BOTH_ASSIGNMENT = "Both blocking and non-blocking assinments in a initial or always block";
	private static final String BLOCKING_ALWAYS = "Blocking assinments in always block";
	private static final String CANNOT_RESOLVED_MODULE = "The module %s is not found in the project";
	private static final String MODULE_HAS_NO_PORT = "The module %s does not have port %s.";
	private static final String BAD_PORT_CONNECTION = "The signal cannot connect to output or inout port %s";
	private static final String UNCONNECTED_PORT = "The port %s in module %s does not connected.";

	private static final int NO_ASSIGN = 0;
	private static final int NON_BLOCKING = 1;
	private static final int BLOCKING = 2;
	
	private IFile m_File;
	private ParserReader m_Reader;
	private IProject m_Project;
	private static OutlineElementFactory m_OutlineElementFactory = new VerilogOutlineElementFactory();
	private OutlineContainer m_OutlineContainer;
	private int m_Context;
	private int blockContext;
	private int blockStatus;
	private Pattern[] taskTokenPattern;
	private boolean isPortConnect;
	private VariableStore variableStore = new VariableStore();
	private InstanceStore instanceStore = new InstanceStore();
	private List<String> generateBlock = new ArrayList<String>();
	
	private Preferences preferences = new Preferences();
	
	public VerilogParser(ParserReader reader, IProject project, IFile file) {
		super(reader);
		m_Context = IParser.OUT_OF_MODULE;
		m_Reader = reader;
		m_Project = project;
		m_File = file;
		m_OutlineContainer = null;
		blockContext = STATEMENT; // no check of block or assign statement

		// because JavaCC counts '\t' as 8 columns by default.
		jj_input_stream.setTabSize(1);

   	    // if project == null, the context scanning is running
		// no update outline and error markers 

		if (project != null) {
			// in outline scanning
			OutlineDatabase database;
			database = OutlineDatabase.getProjectsDatabase(project);
			if (database != null) {
				m_OutlineContainer = database.getOutlineContainer(file);
			}
		}
		
		taskTokenPattern=new Pattern[taskCommentTokens.length];
        for(int i=0; i< taskCommentTokens.length;i++){
            String regex=".*\\b("+taskCommentTokens[i]+")(\\b.*)";
            taskTokenPattern[i]= Pattern.compile(regex);
        }
	}
	
	// called by VerilogParserCore
	protected void beginOutlineElement(Token begin, String name, String type) {
		if (type.equals("module#")) {
			m_Context = IParser.IN_MODULE;
			variableStore.openScope(name, begin.beginLine);
		}
		if (m_OutlineContainer != null) {
			int line = begin.beginLine;
			int col = begin.beginColumn;
			m_OutlineContainer.beginElement(name, type, line,
					begin.beginColumn, m_File, m_OutlineElementFactory);
			String[] types = type.split("#");
			if (types[0].equals("parameter") || types[0].equals("localparam")) {
				String bitRange = (types.length > 2) ? types[2] : "";
				if (bitRange.equals(""))
					bitRange = "[31:0]";
				VariableStore.Symbol ref;
				ref = variableStore.addSymbol(name, line, col, types, bitRange);
				if (ref == null) {
					warning(line, DUPLICATE_PARAM, name);
				} 
			} else if (types[0].equals("task")) {
				addTask(name, line, col, types);
			} else if (types[0].equals("function")) {
				addFunciton(name, line, col, types);
			} else if (types[0].equals("instance")) {
				isPortConnect = true;
				instanceStore.addInstance(types[1], line);
			} else {
				addVariable(name, line, col, types);
			}
		}
	}

	private void addTask(String name, int line, int col, String[] types) {
		VariableStore.Symbol ref = variableStore.findSymbol(name);
		if (ref == null) {
			ref = variableStore.addSymbol(name, line, col, types, "");
		} else {
			if (ref.isTask() == false || ref.isAssigned()) {
				warning(line, DUPLICATE_TASK, name);
				return;
			}
		}
		ref.setAssigned(line, col);
	}

	private void addFunciton(String name, int line, int col, String[] types) {
		String bitRange = (types.length > 1) ? types[1] : "";
		VariableStore.Symbol ref = variableStore.findSymbol(name);
		if (ref == null) {
			ref = variableStore.addSymbol(name, line, col, types, bitRange);
		} else {
			if (ref.isFunction() == false || ref.isAssigned()) {
				warning(line, DUPLICATE_FUNCTION, name);
				return;
			}
			ref.setWidth(bitRange);
		}
		ref.setAssigned(line, col);
	}

	private void addVariable(String name, int line, int col, String[] types) {
		String bitRange = null;
		int dim = 0;
		if (types[0].equals("variable")) {
			if (types[1].contains("genvar"))
				bitRange = "[-1:0]"; // unfixed
			else if (types[1].contains("integer"))
				bitRange = "[31:0]";
			else
				bitRange = (types.length > 2) ? types[2] : "";
			dim = (types.length > 3) ? Integer.parseInt(types[3]) : 0;
		} else if (types[0].equals("port")) {
			bitRange = (types.length > 3) ? types[3] : "";
		}
		if (bitRange != null) {
			String head = "";
			for(int i = 0; i < generateBlock.size(); i++) {
				head += generateBlock.get(i) + ".";
			}
			name = head + name;
			
			if (variableStore.addSymbol(name, line, col, types, bitRange, dim) == null) {
				// c-style port declaration can add reg or wire modifiers.
				if (types[0].equals("variable")) {
					VariableStore.Symbol ref = variableStore.findSymbol(name);
					if (ref.containsType("cstyle")) {
						ref.addModifier(types[1]);
						return;
					}
				}
				warning(line, DUPLICATE_SIGNAL, name);
			}
		}
	}

	protected void endOutlineElement(Token end, String name, String type) {
		if (type.equals("module#")) {
			m_Context = IParser.OUT_OF_MODULE;
			variableStore.closeScope(end.beginLine);
		}
		if (m_OutlineContainer != null) {
			if (type.startsWith("instance#")) {
				isPortConnect = false;
			}
			m_OutlineContainer.endElement(name, type, end.endLine,
					end.endColumn, m_File);
		}
	}
	
	protected void parameterAssignment(Identifier ident, Expression value) {
		VariableStore.Symbol sym;
		sym = variableStore.getVariableSymbol(ident.image, generateBlock);
		if (sym != null) {
			if (value.isValidInt())
				sym.setValue(value.intValue());
			else
				sym.setValue(value.toString());
			sym.setAssigned(ident.beginLine, ident.beginColumn);
		}
	}
	
	private void updateConnection(OutlineDatabase database, VariableStore.Symbol sym) {
		String conns[] = sym.getConnections();
		if (conns == null)
			return;
		for (String conn : conns) {
			String[] csplit = conn.split("#");
			String moduleName = csplit[0];
			String portName = csplit[1];
			OutlineElement module = database.findTopLevelElement(moduleName);
			if (module != null) {
				OutlineElement port = findPortInModule(module, portName);
				if (port != null) {
					String type = port.getType();
					int line = Integer.parseInt(csplit[2]);
					int col = Integer.parseInt(csplit[3]);
					if (type.startsWith("port#input")) {
						sym.setUsed(line, col);
					} else if (type.startsWith("port#output")) {
						if (sym.isReg()) {
							warning(line, REG_CONNECT_OUTPUT, sym.getName());
						}
						sym.setAssigned(line, col);
					} else if (type.startsWith("port#inout")) {
						sym.setUsed(line, col);
						sym.setAssigned(line, col);
					}
				}
			}
		}
	}
	
	private OutlineElement findPortInModule(OutlineElement module, String name) {
		OutlineElement port;
		if (Character.isDigit(name.charAt(0))) {
			int index = Integer.parseInt(name);
			port = module.getChild(index);
		} else {
			port = module.findChild(name);
		}
		if (port == null)
			return null;
		if (port.getType().startsWith("port#")) {
			return port;
		} else {
			return null;
		}
	}

	private void checkVariables() {
		OutlineDatabase database = OutlineDatabase.getProjectsDatabase(m_Project);
		
		for (VariableStore.Symbol sym : variableStore.collection()) {
			updateConnection(database, sym);

			boolean notUsed = false;
			boolean notAssigned = false;
			if (sym.isUsed() == false && sym.containsType("output") == false) {
				notUsed = true;
			}
			if (sym.isAssigned() == false && sym.containsType("input") == false) {
				notAssigned = true;
			}
			if (notUsed || notAssigned) {
				int line = sym.getPosition().line;
				String name = sym.getName();
				if (sym.isTask()) {
					if (notUsed) {
						if (preferences.noUsed)
							warning(line, NOT_USED_TASK, name);
					} else {
						warning(line, CANNOT_RESOLVED_TASK, name);
					}
				} else if (sym.isFunction()) {
					if (notUsed) {
						if (preferences.noUsed)
							warning(line, NOT_USED_FUNCTION, name);
					} else {
						warning(line, CANNOT_RESOLVED_FUNCTION, name);
					}
				} else if (preferences.noUsed) {
					if (notUsed && notAssigned)
						warning(line, NOT_ASSIGNED_AND_USED, name);
					else if (notUsed)
						warning(line, NOT_USED, name);
					else
						warning(line, NOT_ASSIGNED, name);
				}
			}
		}
	}

	private void checkInstance()  {
		OutlineDatabase database = OutlineDatabase.getProjectsDatabase(m_Project);

		InstanceStore store = instanceStore;
		for(InstanceStore.Instance inst : store.collection()) {
			String moduleName = inst.getName();
			OutlineElement module = database.findTopLevelElement(moduleName);
			if (module == null) {
				if (preferences.unresolvedModule) {
					int line = inst.getLine();
					warning(line, CANNOT_RESOLVED_MODULE, moduleName);
				}
			} else {
				if (preferences.moduleConnection) {
					checkInstancePort(inst, module);
				}
			}
		}
	}

	private void checkInstancePort(InstanceStore.Instance inst, OutlineElement module) {
		String moduleName = inst.getName();

		// scan all port in instance.
		for (InstanceStore.Port port : inst.getPorts()) {
			String name = port.getName();
			int line = port.getLine();
			OutlineElement eport = findPortInModule(module, name);
			if (eport == null) {
				warning(line, MODULE_HAS_NO_PORT, moduleName, name);
			} else {
				String type = eport.getType();
				if (type.startsWith("port#input") == false) {
					Expression signal = port.getSignal();
					if (signal != null && signal.isAssignable() == false) {
						warning(line, BAD_PORT_CONNECTION, name);
					}
				}
			}
		}

		if (inst.isNamedMap() == false)
			return;

		// scan all port in module definition.
		for (OutlineElement child : module.getChildren()) {
			String type = child.getType();
			if (type.startsWith("port#")) {
				String portName = child.getName();
				if (inst.findPort(portName) == null) {
					int line = inst.getLine();
					warning(line, UNCONNECTED_PORT, portName, moduleName);
				}
			}
		}
	}
	
	protected void addCollapsible(int startLine, int endLine) {
		if (m_OutlineContainer != null) {
			Collapsible c = m_OutlineContainer.new Collapsible(startLine,
					endLine);
			m_OutlineContainer.addCollapsibleRegion(c);
		}
	}

	protected void begin(int mode) {
		m_Context = IParser.IN_STATEMENT;
		blockContext = mode;
		blockStatus = NO_ASSIGN;
	}

	protected void end(int mode) {
		m_Context = IParser.IN_MODULE;
		blockContext = STATEMENT; // no check of block or assign statement
	}
	
	protected Expression operator(Expression arg, Token op) {
		Operator ope = new Operator(op.image);
		Expression ret = ope.operate(arg);
		if (ope.isWarning())
			warning(op.beginLine, ope.getWarning());
		ret.addReference(arg);
		return ret;
	}

	protected Expression operator(Expression arg1, Token op, Expression arg2) {
		Operator ope = new Operator(op.image);
		Expression ret = ope.operate(arg1, arg2);
		if (ope.isWarning())
			warning(op.beginLine, ope.getWarning());
		ret.addReference(arg1);
		ret.addReference(arg2);
		return ret;
	}

	protected Expression operator(Expression arg1, Token op, Expression arg2,
			Expression arg3) {
		Operator ope = new Operator(op.image);
		Expression ret = ope.operate(arg1, arg2, arg3);
		ret.addReference(arg1);
		ret.addReference(arg2);
		ret.addReference(arg3);
		if (ope.isWarning())
			warning(op.beginLine, ope.getWarning());
		return ret;
	}

	protected Expression variableReference(Identifier ident) {
		if (m_OutlineContainer != null) {
			String name = ident.image;
			if (name.contains("."))
				return new Expression();
			int line = ident.beginLine;
			VariableStore.Symbol sym;
			if (isPortConnect) {
				// I cannot decide whether the variable is referred or assigned.
				sym = variableStore.getVariableSymbol(name, generateBlock);
			} else {
				sym = variableStore.addUsedVariable(name, ident.beginLine, ident.beginColumn, generateBlock);
			}
			if (sym == null) {
				if (preferences.unresolved)
					warning(line, CANNOT_RESOLVED_SIGNAL, name);
			} else {
				int width = ident.getWidth();
				if (width == 0) {
					width = sym.getWidth(); // doesn't have bit range
				} else if (width == 1) {
					if (ident.getDimension() <= sym.getDimemsion())
						width = sym.getWidth();
				}
				if (sym.isParameter()) {
					if (sym.isValidInt())
						return new Expression(width, sym.getValue());
					else
						return new Expression(width, sym.toString());
				} else {
					Expression exp = new Expression(width, ident);
					return exp;
				}
			}
		}
		return new Expression();
	}

	protected void taskReference(Identifier ident) {
		if (m_OutlineContainer != null) {
			String name = ident.image;
			if (name.contains("."))
				return;
			int line = ident.beginLine;
			int col = ident.beginColumn;
			VariableStore.Symbol sym = variableStore.findSymbol(name);
			if (sym == null) {
				String[] types = {"task"};
				sym = variableStore.addSymbol(name, line, col, types, "");
			}
			sym.setUsed(line, col);
		}
	}

	protected Expression functionReference(Identifier ident) {
		if (m_OutlineContainer != null) {
			String name = ident.image;
			if (name.contains("."))
				return new Expression();
			int line = ident.beginLine;
			int col = ident.beginColumn;
			VariableStore.Symbol sym = variableStore.findSymbol(name);
			if (sym == null) {
				String[] types = {"function", ""};
				sym = variableStore.addSymbol(name, line, col, types, "");
				sym.setUsed(line, col);
			} else {
				int width = ident.getWidth();
				if (width == 0)
					width = sym.getWidth(); // doesn't have bit range
				Expression exp = new Expression(width);
				exp.addReference(ident);
				sym.setUsed(line, col);
				return exp;
			}
		}
		return new Expression();
	}

	protected void variableAssignment(Identifier ident) {
		if (m_OutlineContainer != null) {
			String name = ident.image;
			int line = ident.beginLine;
			int col = ident.beginColumn;
			VariableStore.Symbol sym = variableStore.addAssignedVariable(name, line, col, generateBlock);
			if (sym == null || sym.isVariable() == false) {
				if (preferences.unresolved)
					warning(line, CANNOT_RESOLVED_SIGNAL, name);
			} else {
				int width = ident.getWidth();
				if (width <= 1 && sym.getDimemsion() >= ident.getDimension())
					ident.setWidth(sym.getWidth());
				if (blockContext == ASSIGN_STMT && sym.isReg()) {
					warning(line, ASSIGN_REG, name);
				} else if ((blockContext == INITIAL_BLOCK || blockContext == ALWAYS_BLOCK)
						&& sym.isReg() == false) {
					warning(line, ASSIGN_WIRE, name);
				}
			}
		}
	}

	/**
	 * Evaluate positional port connection.
	 */
	protected void variableConnection(Expression arg, String module, int portIndex) {
		if (arg == null)
			return;

		// Don't add port to instanceStore.
		// The connection check is available in named port connection only.
		
		Identifier[] idents = arg.getReferences();
		if (idents == null)
			return;

		String name = Integer.toString(portIndex);
		variableConnection(arg, module, name);
	}

	/**
	 * Evaluate named port connection.
	 */
	protected void variableConnection(Expression arg, String module, Identifier port) {
		if (m_OutlineContainer == null)
			return;

		int line = port.beginLine;
		String portName = port.image;
		instanceStore.addPort(portName, line, arg);

		if (arg != null)
			variableConnection(arg, module, portName);
	}
	
	private void variableConnection(Expression arg, String module, String portName) {
		Identifier[] idents = arg.getReferences();
		if (idents == null)
			return;

		for (Identifier ident : idents) {
			int line = ident.beginLine;
			int col = ident.beginColumn;
			if (preferences.moduleConnection) {
				if (arg.isAssignable()) {
					// The decision of input or output is delayed, just recorded now.
					variableStore.addConnection(ident.image, generateBlock,
							module + "#" + portName + "#" + line + "#" + col);
				} else {
					// Expression must be used as input only.
					variableStore.addUsedVariable(ident.image, line, col, generateBlock);
				}
			} else {
				// no checking module port connection, assume inout port.
				variableStore.addAssignedVariable(ident.image, line, col, generateBlock);
				variableStore.addUsedVariable(ident.image, line, col, generateBlock);
			}
		}
	}
	
	protected void evaluateAssignment(Token asn, int lvalue, Expression exp) {
		if (m_OutlineContainer == null)
			return;

		if (blockContext == INITIAL_BLOCK || blockContext == ALWAYS_BLOCK) {
			if (preferences.blocking) {
				if (asn.image.equals("=")) {
					if (blockStatus == NON_BLOCKING) {
						warning(asn.beginLine, BOTH_ASSIGNMENT);
					} else {
						blockStatus = BLOCKING;
					}
				} else {
					if (blockStatus == BLOCKING) {
						warning(asn.beginLine, BOTH_ASSIGNMENT);
					} else {
						blockStatus = NON_BLOCKING;
					}
				}
			}
		}

		if (blockContext == ALWAYS_BLOCK) {
			if (preferences.blockingAlways && asn.image.equals("=")) {
				warning(asn.beginLine, BLOCKING_ALWAYS);
			}
		}

		if (exp.isFixedWidth() == false || lvalue == 0)
			return;

		int width = exp.getWidth();
		if (preferences.intConst == false) {
			if (width == 32 && exp.isValid())
				return;
		}
		if (preferences.bitWidth && exp.isValidWidth() && lvalue != width) {
			String message = "from " + width + " to " + lvalue;
			warning(asn.beginLine, ASSIGN_WIDTH_MISMATCH, message);
		}
	}

	protected void beginGenerateBlock(Identifier block) {
		generateBlock.add(block.image);
	}
	
	protected void endGenerateBlock(Identifier block) {
		generateBlock.remove(generateBlock.size() - 1);
	}

	private void warning(int line, String format, String arg1, String arg2) {
		String message = String.format(format, arg1, arg2);
		VerilogPlugin.setWarningMarker(m_File, line, message);
	}

	private void warning(int line, String format, String arg) {
		String message = String.format(format, arg);
		VerilogPlugin.setWarningMarker(m_File, line, message);
	}

	private void warning(int line, String message) {
		VerilogPlugin.setWarningMarker(m_File, line, message);
	}

	public int getContext() {
		return m_Context;
	}
	
	private void close() {
		try {
			m_Reader.close();
		} catch (IOException e) {
		}
	}

	public void parse() throws HdlParserException
	{
		try {
			m_Reader.reset();
		} catch (IOException e) {			
		}
		
		if (m_OutlineContainer != null) {
			VerilogPlugin.deleteMarkers(m_File);
			preferences.updatePreferences();
			Expression.setPreferences(preferences);
			Operator.setPreferences(preferences);
		}
		try
		{
			//start by looking for modules
			verilogText();
		}
		catch(ParseException e){

			if (m_OutlineContainer != null) {
				// add error marker in outline scanning 
				VerilogPlugin.setErrorMarker(m_File, e.currentToken.beginLine,
						e.getMessage());
			}
			close();

			//convert the exception to a generic one
			throw new HdlParserException(e);
		}

		if (m_OutlineContainer != null) {
			parseLineComment();
		}
		close();
		
		checkVariables();
		checkInstance();
	}

	public VariableStore getVariableStore() {
		return variableStore;
	}
	 
	/**
     * Checks to see if a comment contains a task token
     * @param comment The comment to check
     * @param msg String array to receive the message type associated with the string
     * @return The string of the Token. null if the comment does not have a task token
     */
    protected String getTaskToken(String comment,String []msg ){
                
        for(Pattern pattern:taskTokenPattern){          
            Matcher matcher= pattern.matcher(comment);
            if(matcher.find()){             
                msg[0]=matcher.group(1)+matcher.group(2);
                return matcher.group(1);
            }
        }
        //if we get here, no task was found
        return null;
    }
    /**
     * Adds a task to the given line based on the comment token
     * @param type
     * @param line
     */
    protected void addTaskToLine(String type,String msg, int line){
        //The first marker is considered high priority
        if(type.startsWith(taskCommentTokens[0])){
            VerilogPlugin.setTaskMarker(m_File, line, msg,IMarker.PRIORITY_HIGH);
        }
        else{
            VerilogPlugin.setTaskMarker(m_File, line, msg,IMarker.PRIORITY_NORMAL);
        }
    }
    
    /**
     * Removes a task from the given line
     * @param line Line number to remove the task from
     */
    protected void removeTaskFromLine(int line){
        VerilogPlugin.clearAutoTaskMarker(m_File, line);
    }
    
    /**
     * Removes all the auto generated tasks from the file
     */
    protected void clearAutoTasks(){
        VerilogPlugin.clearAllAutoTaskMarkers(m_File);
    }
    
    
	/**
	 * parse line comment for collapse
	 */
	private void parseLineComment() {
		try {
			m_Reader.reset();
			clearAutoTasks();

			boolean continued = false;
			int startLine = -1;
			int line = 1;
			boolean validLine = false;
			int c = m_Reader.read();
			while (c != -1) {
				switch (c) {
				case '\n':
					if (continued == true) {
						continued = false;
						if (line - startLine >= 2)
							addCollapsible(startLine, line - 1);
					}
					line++;
					validLine = false;
					c = m_Reader.read();
					break;
				case '/':
					c = m_Reader.read();
					if (!validLine && c == '/') {
						if (continued == false) {
							startLine = line;
							continued = true;
						}

						String comment = getLineComment(m_Reader);
						if (comment != null){
						    String []msg=new String[1];								
                        
                            addComment(line, comment);
                            //check to see if we need to add a task
                            String taskToken=getTaskToken(comment,msg);
                            if(taskToken != null){                                  
                                addTaskToLine(taskToken, msg[0], line);
                            }                               
                        }
						line++;
					}
					break;
				default:
					if (!Character.isWhitespace((char)c)){
						validLine = true;
					}
					c = m_Reader.read();
					break;
				}
			}
		} catch (IOException e) {
		}
	}

	private String getLineComment(Reader reader) throws IOException
	{
		StringBuffer str = new StringBuffer();
		boolean enable = false;

		//  copy to StringBuffer
		int c = reader.read();
		while (c != '\n' && c != -1)
		{
			if (Character.isLetterOrDigit((char)c) || enable)
			{
				str.append((char)c);
				enable = true;
			}
			c = reader.read();
		}

		// delete tail
		for (int i = str.length() - 1; i >= 0; i--)
		{
			char ch = str.charAt(i);
			if (!Character.isSpaceChar(ch))
				break;
			else
				str.deleteCharAt(i);
		}

		if (str.length() != 0)
			return str.toString();
		else
			return null;
	}

	private void addComment(int line, String comment)
	{
		// ignore continuous comments
		if (prevCommentLine + 1 == line)
		{
			prevCommentLine = line;
			return;
		}

		prevCommentLine = line;

		//TODO need to add the comment functionality back in
	}
	private int prevCommentLine;

	// preferences
	public static class Preferences {
		public boolean unresolved;
		public boolean noUsed;
		public boolean bitWidth;
		public boolean intConst;
		public boolean blocking;
		public boolean blockingAlways;
		public boolean unresolvedModule;
		public boolean moduleConnection;

		public void updatePreferences() {
			unresolved = get(WARNING_UNRESOLVED);
			noUsed = get(WARNING_NO_USED_ASIGNED);
			bitWidth = get(WARNING_BIT_WIDTH);
			intConst = get(WARNING_INT_CONSTANT);
			blocking = get(WARNING_BLOCKING_ASSIGNMENT);
			blockingAlways = get(WARNING_BLOCKING_ASSIGNMENT_IN_ALWAYS);
			unresolvedModule = get(WARNING_UNRESOLVED_MODULE);
			moduleConnection = get(WARNING_MODULE_CONNECTION);
		}

		private boolean get(String key) {
			return VerilogPlugin.getPreferenceBoolean(key);
		}
	}
}


