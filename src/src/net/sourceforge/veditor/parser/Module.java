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

import org.eclipse.core.resources.IFile;

/**
 * Module definition<p/>
 * Only ModuleList can instantiate
 */
public abstract class Module extends Segment
{
	/**
	 * module name - unique in project
	 */
	private String name;

	public Module(String name)
	{
		this.name = name;
	}

	public Module(int line, String name)
	{
		super(line);
		this.name = name;

	}

	public String toString()
	{
		return name;
	}

	//  for module datebase
	public boolean equals(Object obj)
	{
		if (obj instanceof Module)
			return name.equals(obj.toString());
		else
			return false;
	}
	public int hashCode()
	{
		return name.hashCode();
	}

	public Object[] getElements()
	{
		return null;
	}
	public Object[] getInstance()
	{
		return null;
	}
	public IFile getFile()
	{
		return null;
	}
	public Object[] getPorts()
	{
		return null;
	}
	public Object[] getVariables()
	{
		return null;
	}
	public Object[] getParameters()
	{
		return null;
	}
	public Object[] getParameterValues()
	{
		return null;
	}
	public void addPort(String name)
	{
	}
	public void addElement(int begin, int end, String typeName, String name)
	{
	}
	public void addInstance(int begin, int end, String typeName, String name)
	{
	}
	public void addVariable(String str)
	{
	}
	public void addParameter(String str, String value)
	{
	}
	public Module findModule(String str)
	{
		return null;
	}
}
