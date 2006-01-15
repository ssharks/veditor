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
