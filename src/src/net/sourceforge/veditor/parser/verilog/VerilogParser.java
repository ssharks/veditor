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
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.OutlineContainer;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElementFactory;
import net.sourceforge.veditor.parser.OutlineContainer.Collapsible;
import net.sourceforge.veditor.parser.ParserReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;

/**
 * implementation class of VerilogParserCore<p/>
 * for separating definition from JavaCC code 
 */
public class VerilogParser extends VerilogParserCore implements IParser
{
	private static final String DUPLICATE_PARAM = "Duplicate parameter %s";
	private static final String DUPLICATE_SIGNAL = "Duplicate signal %s";
	private static final String DUPLICATE_TASK = "Duplicate task %s";
	private static final String DUPLICATE_FUNCTION = "Duplicate function %s";
	private static final String CANNOT_RESOLVED = "%s cannot be resolved to a signal or parameter";
	private static final String CANNOT_RESOLVED_TASK = "%s cannot be resolved to a task";
	private static final String CANNOT_RESOLVED_FUNCTION = "%s cannot be resolved to a function";
	private static final String NOT_ASSIGNED_AND_USED = "The signal %s is not assigned and used";
	private static final String NOT_USED = "The value of %s is not used";
	private static final String NOT_USED_TASK = "The task %s is not used";
	private static final String NOT_USED_FUNCTION = "The function %s is not used";
	private static final String NOT_ASSIGNED = "The signal %s is not assigned";
	private static final String ASSIGN_WIDTH_MISMATCH = "Assignment bit width mismatch: %s";
	
	private IFile m_File;
	private ParserReader m_Reader;
	private static OutlineElementFactory m_OutlineElementFactory = new VerilogOutlineElementFactory();
	private OutlineContainer m_OutlineContainer;
	private int m_Context;
	private Pattern[] taskTokenPattern;
	private VariableStore variableStore;

	public VerilogParser(ParserReader reader, IProject project, IFile file) {
		super(reader);
		m_Context = IParser.OUT_OF_MODULE;
		m_Reader = reader;
		m_File = file;
		m_OutlineContainer = null;

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
			variableStore = new VariableStore();
		}
		if (m_OutlineContainer != null) {
			int line = begin.beginLine;
			m_OutlineContainer.beginElement(name, type, line,
					begin.beginColumn, m_File, m_OutlineElementFactory);
			String[] types = type.split("#");
			if (types[0].equals("parameter") || types[0].equals("localparam")) {
				String bitRange = (types.length > 2) ? types[2] : "";
				int value = (types.length > 3) ? Integer.parseInt(types[3]) : 0;
				if (bitRange.equals(""))
					bitRange = "[31:0]";
				VariableStore.Symbol ref;
				ref = variableStore.addSymbol(name, line, types, bitRange);
				if (ref == null) {
					warning(line, DUPLICATE_PARAM, name);
				} else {
					ref.setValue(value);
					ref.setAssignd();
				}
			} else if (types[0].equals("task")) {
				addTask(name, line, types);
			} else if (types[0].equals("function")) {
				addFunciton(name, line, types);
			} else {
				addVariable(name, line, types);
			}
		}
	}

	private void addTask(String name, int line, String[] types) {
		VariableStore.Symbol ref = variableStore.findSymbol(name);
		if (ref == null) {
			ref = variableStore.addSymbol(name, line, types, "");
		} else {
			if (ref.isTask() == false || ref.isAssignd()) {
				warning(line, DUPLICATE_TASK, name);
				return;
			}
		}
		ref.setAssignd();
	}

	private void addFunciton(String name, int line, String[] types) {
		String bitRange = (types.length > 1) ? types[1] : "";
		VariableStore.Symbol ref = variableStore.findSymbol(name);
		if (ref == null) {
			ref = variableStore.addSymbol(name, line, types, bitRange);
		} else {
			if (ref.isFunction() == false || ref.isAssignd()) {
				warning(line, DUPLICATE_FUNCTION, name);
				return;
			}
			ref.setWidth(bitRange);
		}
		ref.setAssignd();
	}

	private void addVariable(String name, int line, String[] types) {
		String bitRange = null;
		int dim = 0;
		if (types[0].equals("variable")) {
			if (types[1].contains("integer"))
				bitRange = "[31:0]";
			else
				bitRange = (types.length > 2) ? types[2] : "";
			dim = (types.length > 3) ? Integer.parseInt(types[3]) : 0;
		} else if (types[0].equals("port")) {
			bitRange = (types.length > 3) ? types[3] : "";
		}
		if (bitRange != null) {
			if (variableStore.addSymbol(name, line, types, bitRange, dim) == null) {
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
			checkVariables();
		}
		if (m_OutlineContainer != null) {
			m_OutlineContainer.endElement(name, type, end.endLine,
					end.endColumn, m_File);
		}
	}
	
	private void checkVariables() {
		Iterator<VariableStore.Symbol> iter = variableStore.iterator();
		while (iter.hasNext()) {
			VariableStore.Symbol sym = iter.next();
			boolean notUsed = false;
			boolean notAssigned = false;
			if (sym.isUsed() == false && sym.containsType("output") == false) {
				notUsed = true;
			}
			if (sym.isAssignd() == false && sym.containsType("input") == false) {
				notAssigned = true;
			}
			if (notUsed || notAssigned) {
				int line = sym.getLine();
				String name = sym.getName();
				if (sym.isTask()) {
					if (notUsed)
						warning(line, NOT_USED_TASK, name);
					else
						warning(line, CANNOT_RESOLVED_TASK, name);
				} else if (sym.isFunction()) {
					if (notUsed)
						warning(line, NOT_USED_FUNCTION, name);
					else
						warning(line, CANNOT_RESOLVED_FUNCTION, name);
				} else if (notUsed && notAssigned)
					warning(line, NOT_ASSIGNED_AND_USED, name);
				else if (notUsed)
					warning(line, NOT_USED, name);
				else
					warning(line, NOT_ASSIGNED, name);
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

	protected void beginStatement() {
		m_Context = IParser.IN_STATEMENT;
	}

	protected void endStatement() {
		m_Context = IParser.IN_MODULE;
	}
	
	protected Expression variableReference(Identifier ident) {
		if (m_OutlineContainer != null) {
			String name = ident.image;
			if (name.contains("."))
				return new Expression();
			int line = ident.beginLine;
			VariableStore.Symbol sym = variableStore.addUsedVariable(name);
			if (sym == null) {
				warning(line, CANNOT_RESOLVED, name);
			} else {
				int width = ident.getWidth();
				if (width == 0) {
					width = sym.getWidth(); // doesn't have bit range
				} else if (width == 1) {
					if (ident.getDimension() <= sym.getDimemsion())
						width = sym.getWidth();
				}
				if (sym.isParameter()) {
					return new Expression(width, sym.getValue());
				} else {
					Expression exp = new Expression(width);
					exp.addReference(ident);
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
			VariableStore.Symbol sym = variableStore.findSymbol(name);
			if (sym == null) {
				String[] types = {"task"};
				sym = variableStore.addSymbol(name, line, types, "");
				sym.setUsed();
			}
		}
	}

	protected Expression functionReference(Identifier ident) {
		if (m_OutlineContainer != null) {
			String name = ident.image;
			if (name.contains("."))
				return new Expression();
			int line = ident.beginLine;
			VariableStore.Symbol sym = variableStore.findSymbol(name);
			if (sym == null) {
				String[] types = {"function", ""};
				sym = variableStore.addSymbol(name, line, types, "");
				sym.setUsed();
			} else {
				int width = ident.getWidth();
				if (width == 0)
					width = sym.getWidth(); // doesn't have bit range
				Expression exp = new Expression(width);
				exp.addReference(ident);
				return exp;
			}
		}
		return new Expression();
	}

	protected void variableAssignment(Identifier ident) {
		if (m_OutlineContainer != null) {
			String name = ident.image;
			int line = ident.beginLine;
			VariableStore.Symbol sym = variableStore.addAssignedVariable(name);
			if (sym == null) {
				warning(line, CANNOT_RESOLVED, name);
			} else {
				int width = ident.getWidth();
				if (width <= 1 && sym.getDimemsion() >= ident.getDimension())
					ident.setWidth(sym.getWidth());
			}
		}
	}

	protected void variableConnection(Expression arg, String module, String port) {
		if (m_OutlineContainer != null) {
			Identifier[] idents = arg.getReferences();
			if (idents != null) {
				for (Identifier ident : idents) {
					variableStore.addAssignedVariable(ident.image);
				}
			}
		}
	}
	
	protected void evaluateAssignment(Token asn, int lvalue, Expression exp) {
		int width = exp.getWidth();
		if (lvalue == 0 || width == 0)
			return;
		// TODO: should depend on preference.
		// ignore assignment from integer constant 
		if (width == 32 && exp.isValid())
			return;
		if (lvalue != width) {
			String message = "from " + width + " to " + lvalue;
			warning(asn.beginLine, ASSIGN_WIDTH_MISMATCH, message);
		}
	}


	private void warning(int line, String format, String arg) {
		String message = String.format(format, arg);
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
		
		if (m_OutlineContainer != null)
			VerilogPlugin.deleteMarkers(m_File);
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

}


