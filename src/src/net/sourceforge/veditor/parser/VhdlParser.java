//
//  Copyright 2004, KOBAYASHI Tadashi
//  $Id$
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

package net.sourceforge.veditor.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * implementation class of VerilogParser<p/>
 * for separating definition from JavaCC code
 */
class VhdlParser extends VhdlParserCore implements IParser
{
	public VhdlParser(Reader reader)
	{
		super(reader);
	}

	private int context = OUT_OF_MODULE;

	//  depend on calling parse or getContext
	private boolean updateDatabase;

	private Module getCurrentModule()
	{
		int n = mods.size() - 1;
		return (Module)mods.get(n);
	}
	private List mods = new ArrayList();

	public String getCurrentModuleName()
	{
		return currentModuleName;
	}
	private String currentModuleName;

	// called by VhdlParserCore
	protected void addModule(int begin, String name)
	{
		context = IN_MODULE;
		currentModuleName = name;
		if (updateDatabase)
		{
			Module module = ModuleList.getCurrent().newModule(begin, name, file);
			mods.add(module);
		}
	}
	protected void endModule(int line)
	{
		context = OUT_OF_MODULE;
		if (updateDatabase)
			getCurrentModule().setEndLine(line);
	}
	protected void addPort(int line, String portName)
	{
		if (updateDatabase)
		{
			getCurrentModule().addPort(portName);
			// addElement(line, line, "port", portName);
		}
	}
	protected void addVariable(int line, String varName)
	{
		if (updateDatabase)
			getCurrentModule().addVariable(varName);
	}
	protected void addElement(int begin, int end, String type, String name)
	{
		if (updateDatabase)
			getCurrentModule().addElement(begin, end, type, name);
	}
	protected void addInstance(int begin, int end, String module, String inst)
	{
		int period = module.lastIndexOf('.');
		if (period >= 0)
			module = module.substring(period + 1);
		if (updateDatabase)
			getCurrentModule().addInstance(begin, end, module, inst);
	}
	protected void beginStatement()
	{
		context = IN_STATEMENT;
	}
	protected void endStatement()
	{
		context = IN_MODULE;
	}

	//  called by editor
	public Segment getModule(int n)
	{
		return (Segment)mods.get(n);
	}
	public int size()
	{
		return mods.size();
	}

	public void parse(IProject project, IFile file)
	{
		updateDatabase = true;
		ModuleList.setCurrent(project);
		this.file = file;
		try
		{
			parse();
		}
		catch (ParseException e)
		{
			endModule(e.currentToken.endLine);
			System.out.println(file);
			System.out.println(e);
		}
	}
	private IFile file;

	public int getContext()
	{
		updateDatabase = false;
		context = OUT_OF_MODULE;
		try
		{
			parse();
		}
		catch (ParseException e)
		{
		}
		return context;
	}

	public void dispose()
	{
		mods = null;
	}

	/**
	 * parse line comment for content outline
	 */
	public void parseLineComment(Reader reader)
	{
		try
		{
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

		Iterator i = mods.iterator();
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







