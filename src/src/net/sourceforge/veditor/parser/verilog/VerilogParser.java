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
	private static OutlineElementFactory m_OutlineElementFactory=new VerilogOutlineElementFactory();
	private OutlineContainer m_OutlineContainer;
	int m_Context;
	

	public VerilogParser(Reader reader, IProject project, IFile file)
	{
		super(reader);
		m_Reader = reader;
		m_File = file;
		OutlineDatabase database = OutlineDatabase.getProjectsDatabase(project);		
		if(database != null){
			m_OutlineContainer = database.getOutlineContainer(file);
		}
	}

	
	// called by VerilogParserCore	
	protected void beginOutlineElement(int begin,int col,String name,String type){
		m_OutlineContainer.beginElement(name, type, begin, col, m_File,m_OutlineElementFactory);
	}
	protected void addCollapsible(int startLine,int endLine){
		Collapsible c= m_OutlineContainer.new Collapsible(startLine,endLine);
		m_OutlineContainer.addCollapsibleRegion(c);
	}
	protected void endOutlineElement(int end,int col,String name,String type){
		m_OutlineContainer.endElement(name,type,end,col, m_File);
	}	
	protected void beginStatement()
	{
		m_Context = IParser.IN_STATEMENT;
	}
	protected void endStatement()
	{
		m_Context = IParser.IN_MODULE;
	}

	public void parse() throws HdlParserException
	{
		try
		{
			m_Reader.reset();
			//start by looking for modules
			modules();
		}
		catch (IOException e)
		{
		}
		catch(ParseException e){
			//convert the exception to a generic one
			throw new HdlParserException(e);
		}
		
	}
	
	/**
	 * parse line comment for content outline
	 */
	public void parseLineComment()
	{
		try
		{
			m_Reader.reset();

			int line = 1;
			int column = 0;
			int c = m_Reader.read();
			while (c != -1)
			{
				column++;
				switch (c)
				{
					case '\n' :
						line++;
						column = 0;
						c = m_Reader.read();
						break;
					case '/' :
						c = m_Reader.read();
						if (c == '/' && column == 1)
						{
							String comment = getLineComment(m_Reader);
							if (comment != null)
								addComment(line, comment);
							line++;
							column = 0;
						}
						break;
					default :
						c = m_Reader.read();
						break;
				}
			}
		}
		catch (IOException e)
		{
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


