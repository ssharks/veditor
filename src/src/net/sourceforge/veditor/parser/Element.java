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

/**
 * instance, function, task and comment in source code
 */
public class Element extends Segment implements Comparable
{
	private Segment parent;		//  includes this
	private String typeName;	//  module name, "function", "task" or "//"
	private String name; 		//  instance name, function name, task name or comment

	public Element(int line, Segment parent, String typeName, String name)
	{
		super(line);
		this.parent = parent;
		this.typeName = typeName;
		this.name = name;
	}

	public Segment getParent()
	{
		return parent;
	}

	public String toString()
	{
		return typeName + " " + name;
	}

	public String getTypeName()
	{
		return typeName;
	}

	public int compareTo(Object arg)
	{
		String str = toString();
		return str.compareTo(arg.toString());
	}
}
