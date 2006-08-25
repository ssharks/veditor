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

package net.sourceforge.veditor.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * implementation class of VhdlParserCore<p/>
 * for separating definition from JavaCC code
 */
class VhdlParser extends VhdlParserCore implements IParser
{
	private IFile file;
	private ParserManager manager;
	private Reader reader;
	private List varList = new ArrayList();
	private int varMode;
	private final int MODE_PARAMETER = 0;
	private final int MODE_PORT = 1;
	private final int MODE_SIGNAL = 2;

	public VhdlParser(Reader reader, IProject project, IFile file)
	{
		super(reader);
		this.reader = reader;
		this.file = file;
		manager = new ParserManager(this, project);
	}

	public ParserManager getManager()
	{
		return manager;
	}

	public IFile getFile()
	{
		return file;
	}

	// called by VhdlParserCore
	protected void addModule(int begin, String name)
	{
		manager.addModule(begin, name, file);
	}
	protected void endModule(int line)
	{
		manager.endModule(line);
	}
	protected void addPort(int line, String portName)
	{
		varMode = MODE_PORT;
		varList.add(new Variable(line, portName));
	}
	protected void addVariable(int line, String varName)
	{
		varMode = MODE_SIGNAL;
		varList.add(new Variable(line, varName));
	}
	protected void addParameter(int line, String name)
	{
		varMode = MODE_PARAMETER;
		varList.add(new Variable(line, name));
	}
	protected void addElement(int begin, int end, String type, String name)
	{
		manager.addElement(begin, end, type, name);
	}
	protected void addInstance(int begin, int end, String moduleName, String inst)
	{
		manager.addInstance(begin, end, moduleName,inst);
	}
	protected void beginStatement()
	{
		manager.beginStatement();
	}
	protected void endStatement()
	{
		manager.endStatement();
	}
	protected void setPrefix(String fix)
	{
		manager.setPrefix(fix);
	}
	protected void setPostfix(String fix)
	{
		manager.setPostfix(fix);
		Iterator i; 
		
		i = varList.iterator();
		while(i.hasNext())
		{
			Variable var = (Variable)i.next();
			switch(varMode)
			{
				case MODE_PARAMETER:
					manager.addParameter(var.line, var.name, fix);
					break;
				case MODE_PORT:
					manager.addPort(var.line, var.name);
					break;
				case MODE_SIGNAL:
					manager.addSignal(var.line, var.name);
					break;
			}
		}
		varList.clear();
	}
	
	private static class Variable
	{
		public int line;
		public String name;
		
		Variable(int line, String name)
		{
			this.line = line;
			this.name = name;
		}
	}

	public void parse() throws ParseException
	{
		try
		{
			reader.reset();
		}
		catch (IOException e)
		{
		}
		super.parse();
	}

	/**
	 * parse line comment for content outline
	 */
	public void parseLineComment()
	{
		try
		{
			reader.reset();

			int line = 1;
			int column = 0;
			int c = reader.read();
			while (c != -1)
			{
				column++;
				switch (c)
				{
					case '\n' :
						line++;
						column = 0;
						c = reader.read();
						break;
					case '-' :
						c = reader.read();
						if (c == '-' && column == 1)
						{
							String comment = getLineComment(reader);
							if (comment != null)
								addComment(line, comment);
							line++;
							column = 0;
						}
						break;
					default :
						c = reader.read();
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
			if (Character.isLetterOrDigit(ch))
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

		Iterator i = manager.getModuleIterator();
		while (i.hasNext())
		{
			Module mod = (Module)i.next();
			if (line >= mod.getLine() + mod.getLength())
				break;
			if (line >= mod.getLine())
			{
				// System.out.println(comment);
				mod.addElement(line, line, "--", comment);
			}
		}
	}
	private int prevCommentLine;
	
}







