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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * manage module port and instance,
 * It is owned by VerilogParser or VhdlParser
 */
public class ParserManager 
{
	private int context = IParser.OUT_OF_MODULE;
	private IParser parser;
	private IProject project;
	private String prefix, postfix;

	//  depend on calling parse or getContext
	private boolean updateDatabase;

	public ParserManager(IParser parser, IProject project)
	{
		this.parser = parser;
		this.project = project;
	}
	
	private Module getCurrentModule()
	{
		int size = mods.size();
		if ( size == 0 )
		{
			return null;
		}
		else
		{
			return (Module)mods.get(size - 1);
		}
	}
	private List mods = new ArrayList();

	public String getCurrentModuleName()
	{
		return currentModuleName;
	}
	private String currentModuleName;

	// called by VerilogParser or VhdlParser
	protected void addModule(int begin, String name, IFile file)
	{
		context = IParser.IN_MODULE;
		currentModuleName = name;
		if (updateDatabase)
		{
			Module module = ModuleList.getCurrent().newModule(begin, name, file);
			mods.add(module);
		}
	}
	protected void endModule(int line)
	{
		context = IParser.OUT_OF_MODULE;
		if (updateDatabase)
		{
			Module module = getCurrentModule();
			if (module != null)
				module.setEndLine(line);
		}
	}
	protected void addPort(int line, String portName)
	{
		if (updateDatabase)
		{
			Module module = getCurrentModule();
			if (module != null)
				module.addPort(portName, prefix, postfix);
			// addElement(line, line, "port", portName);
		}
	}
	protected void addSignal(int line, String varName)
	{
		if (updateDatabase)
		{
			Module module = getCurrentModule();
			if (module != null)
				module.addSignal(varName, prefix, postfix);
		}
	}
	protected void addParameter(int line, String name, String value)
	{
		if (updateDatabase)
		{
			Module module = getCurrentModule();
			if (module != null)
				module.addParameter(name, value);
		}
	}
	protected void addElement(int begin, int end, String type, String name)
	{
		if (updateDatabase)
		{
			Module module = getCurrentModule();
			if (module != null)
				module.addElement(begin, end, type, name);
		}
	}
	protected void addInstance(int begin, int end, String moduleName, String inst)
	{
		int period = moduleName.lastIndexOf('.');
		if (period >= 0)
			moduleName = moduleName.substring(period + 1);
		if (updateDatabase)
		{
			Module module = getCurrentModule();
			if (module != null)
				module.addInstance(begin, end, moduleName, inst);
		}
	}
	protected void beginStatement()
	{
		context = IParser.IN_STATEMENT;
	}
	protected void endStatement()
	{
		context = IParser.IN_MODULE;
	}
	protected void setPrefix(String fix)
	{
		prefix = fix;
	}
	protected void setPostfix(String fix)
	{
		postfix = fix;
	}
	
	protected Iterator getModuleIterator()
	{
		return mods.iterator();
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

	public void dispose()
	{
		mods = null;
	}

	public void setUpdateDatabase(boolean updateDatabase)
	{
		this.updateDatabase = updateDatabase;
	}

	private int getContext()
	{
		return context;
	}

	private void setContext(int context)
	{
		this.context = context;
	}

	/**
	 * analyze context from top of file.
	 * @return enum of context 
	 */
	public int parseContext()
	{
		setUpdateDatabase(false);
		setContext(IParser.OUT_OF_MODULE);
		try
		{
			parser.parse();
		}
		catch (ParseException e)
		{
		}
		catch (TokenMgrError e)
		{
			return IParser.OUT_OF_MODULE;
		}
		return getContext();
	}
	

	/**
	 * check syntax. if error, throw exception and caller should show it on GUI
	 * @throws ParseException
	 */
	public void parseSyntax() throws ParseException
	{
		setUpdateDatabase(true);
		setContext(IParser.OUT_OF_MODULE);
		parser.parse();
	}

	/**
	 * parse source code and update module database
	 * @param project
	 * @param file
	 * @return if error, return false
	 */
	public boolean parse()
	{
		setUpdateDatabase(true);
		setContext(IParser.OUT_OF_MODULE);
		ModuleList.setCurrent(project);
		try
		{
			parser.parse();
			return true;
		}
		catch (ParseException e)
		{
			if (e.currentToken != null)
				endModule(e.currentToken.endLine);
			//System.out.println(file);
			//System.out.println(e);
			//VerilogPlugin.println(file.toString() + "\n" + e.toString());
			return false;
		}
	}
	
	public void parseLineComment()
	{
		parser.parseLineComment();
	}

}
