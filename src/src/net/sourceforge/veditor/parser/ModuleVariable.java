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

public class ModuleVariable
{
	private String name;
	private String prefix, postfix;
		
	public ModuleVariable(String name)
	{
		this.name = name;
	}

	public ModuleVariable(String name, String prefix, String postfix)
	{
		this.name = name;
		this.prefix = prefix;
		this.postfix = postfix;
	}
	
	public String toString()
	{
		return name;
	}
	
	public int hashCode()
	{
		return name.hashCode();
	}

	public boolean equals(Object obj)
	{
		return name.equals(obj);
	}
	
	public String getValue()
	{
		return name;
	}
	
	public String getDetail()
	{
		StringBuffer buf = new StringBuffer();
		if (prefix != null)
			buf.append(prefix);
		buf.append(name);
		if (postfix != null)
			buf.append(postfix);
		return buf.toString();
	}
}
