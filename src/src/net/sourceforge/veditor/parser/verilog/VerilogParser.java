/*******************************************************************************
 * Copyright (c) 2004, 2006 KOBAYASHI Tadashi and others.
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

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.OutlineContainer;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElementFactory;
import net.sourceforge.veditor.parser.OutlineContainer.Collapsible;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * implementation class of VerilogParserCore<p/>
 * for separating definition from JavaCC code 
 */
public class VerilogParser extends VerilogParserCore implements IParser
{
	private IFile m_File;
	private Reader m_Reader;
	private static OutlineElementFactory m_OutlineElementFactory = new VerilogOutlineElementFactory();
	private OutlineContainer m_OutlineContainer;
	private int m_Context;

	public VerilogParser(Reader reader, IProject project, IFile file) {
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
	}
	
	// called by VerilogParserCore
	protected void beginOutlineElement(int begin, int col, String name, String type) {
		if (type.equals("module#")) {
			m_Context = IParser.IN_MODULE;
		}
		if (m_OutlineContainer != null) {
			m_OutlineContainer.beginElement(name, type, begin, col, m_File,
					m_OutlineElementFactory);
		}
	}
	protected void addCollapsible(int startLine, int endLine) {
		if (m_OutlineContainer != null) {
			Collapsible c = m_OutlineContainer.new Collapsible(startLine,
					endLine);
			m_OutlineContainer.addCollapsibleRegion(c);
		}
	}
	protected void endOutlineElement(int end, int col, String name, String type) {
		if (type.equals("module#")) {
			m_Context = IParser.OUT_OF_MODULE;
		}
		if (m_OutlineContainer != null) {
			m_OutlineContainer.endElement(name, type, end, col, m_File);
		}
	}	
	protected void beginStatement() {
		m_Context = IParser.IN_STATEMENT;
	}

	protected void endStatement() {
		m_Context = IParser.IN_MODULE;
	}

	public int getContext() {
		return m_Context;
	}

	public void parse() throws HdlParserException
	{
		try {
			m_Reader.reset();
		} catch (IOException e) {			
		}
		
		try
		{
			//start by looking for modules
			modules();
		}
		catch(ParseException e){

			if (m_OutlineContainer != null) {
				// add error marker in outline scanning 
				VerilogPlugin.setErrorMarker(m_File, e.currentToken.beginLine,
						e.getMessage());
			}

			//convert the exception to a generic one
			throw new HdlParserException(e);
		}

		if (m_OutlineContainer != null) {
			parseLineComment();
		}
	}
	
	/**
	 * parse line comment for collapse
	 */
	private void parseLineComment() {
		try {
			m_Reader.reset();

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
						if (comment != null)
							addComment(line, comment);
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


