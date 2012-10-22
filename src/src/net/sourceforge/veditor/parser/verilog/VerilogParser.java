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
	private static final String CANNOT_RESOLVED = "%s cannot be resolved to a signal or parameter";
	private static final String NOT_ASSIGNED_AND_USED = "The signal %s is not assigned and used";
	private static final String NOT_USED = "The value of %s is not used";
	private static final String NOT_ASSIGNED = "The signal %s is not assigned";
	
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
			m_OutlineContainer.beginElement(name, type, begin.beginLine, begin.beginColumn, m_File,
					m_OutlineElementFactory);
		}
	}

	protected void endOutlineElement(Token end, String name, String type) {
		if (type.equals("module#")) {
			m_Context = IParser.OUT_OF_MODULE;
			checkVariables();
		}
		if (m_OutlineContainer != null) {
			int line = end.endLine;
			m_OutlineContainer.endElement(name, type, end.endLine,
					end.endColumn, m_File);
			String[] types = type.split("#");
			String bitRange = null;
			if (types[0].equals("parameter") || types[0].equals("localparam")) {
				bitRange = (types.length > 2) ? types[2] : "";
				int value = (types.length > 3) ? Integer.parseInt(types[3]) : 0;
				if (bitRange.equals(""))
					bitRange = "[31:0]";
				if (variableStore.addSymbol(name, line, type, bitRange, value) == false) {
					warning(end.endLine, String.format(DUPLICATE_PARAM, name));
				}
			} else {
				if (types[0].equals("variable"))
					bitRange = (types.length > 2) ? types[2] : "";
				else if (types[0].equals("port"))
					bitRange = (types.length > 3) ? types[3] : "";
				if (bitRange != null) {
					if (variableStore.addSymbol(name, line, type, bitRange) == false) {
						// c-style port declaration can add reg or wire modifiers.
						if (types[0].equals("variable")) {
							VariableStore.Symbol ref = variableStore.findSymbol(name);
							if (ref.getType().contains("cstyle")) {
								ref.addModifier(types[1]);
								return;
							}
						}
						warning(end.endLine, String.format(DUPLICATE_SIGNAL, name));
					}
				}
			}
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
				if (notUsed && notAssigned)
					warning(line, String.format(NOT_ASSIGNED_AND_USED, name));
				else if (notUsed)
					warning(line, String.format(NOT_USED, name));
				else
					warning(line, String.format(NOT_ASSIGNED, name));
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
			VariableStore.Symbol sym = variableStore.addUsedSymbol(name);
			if (sym == null) {
				warning(line, String.format(CANNOT_RESOLVED, name));
			} else {
				int width = ident.getWidth();
				if (width == 0)
					width = sym.getWidth(); // doesn't have bit range
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

	protected Expression functionReference(Identifier ident) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void variableAssignment(Identifier ident) {
		if (m_OutlineContainer != null) {
			String name = ident.image;
			int line = ident.beginLine;
			VariableStore.Symbol sym = variableStore.addAssignedSymbol(name);
			if (sym == null) {
				warning(line, String.format(CANNOT_RESOLVED, name));
			}
		}
	}

	protected void taskReference(Identifier ident) {
		// TODO Auto-generated method stub
		
	}

	protected void variableConnection(Expression arg, String module, String port) {
		if (m_OutlineContainer != null) {
			Identifier[] idents = arg.getReferences();
			if (idents != null) {
				for (Identifier ident : idents) {
					variableStore.addAssignedSymbol(ident.image);
				}
			}
		}
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


