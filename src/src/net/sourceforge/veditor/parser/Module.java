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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Module definition
 */
public class Module extends Segment
{

	/**
	 * module name - unique in project
	 */
	private String name ;

	/**
	 * flag of parsed or not
	 */
	private boolean doneParse ;
	public boolean isDoneParse()
	{
		return doneParse;
	}

	public Module(String name)
	{
		super(-1);
		this.name = name ;	// file name
		doneParse = false ;

		ModuleList.getCurrent().replaceModule(this);
	}

	public Module(int line, String name)
	{
		super(line);
		this.name = name ;  // module name
		doneParse = true ;

		elements = new ArrayList();

		//  replace module in database
		ModuleList.getCurrent().replaceModule(this);

		// System.out.println("module " + name);
	}

	public String toString()
	{
		return name ;
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


	/**
	 * instance, function, task, comment
	 */
	private List elements ;
	public Segment getElement(int n)
	{
		return (Segment)elements.get(n);
	}
	public int size()
	{
		if (elements != null)
			return elements.size();
		else
			return 0;
	}

	/**
	 * input/output prots
	 */
	private List ports = new ArrayList();
	public Iterator getPortIterator()
	{
		return ports.iterator();
	}


	//  called by parser
	public void addPort(String name)
	{
		ports.add(name);
	}
	public void addElement(int begin, int end, String typeName, String name)
	{
		Element child = new Element(begin, this, typeName, name);
		child.setEndLine(end);
		elements.add(child);
	}
	public void addComment(int begin, String str)
	{
		if (!isValidComment(str))
			return;

		//  コメント行が連続している場合は最初で代表させる
		if (begin == lastCommentLine + 1)
		{
			lastCommentLine = begin;
			return;
		}

		//  行の最初の"//"と無駄な文字を削除する
		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			if (Character.isLetterOrDigit(ch))
			{
				str = str.substring(i);
				break;
			}
		}
		//  行の最後の無駄な文字を削除する
		for (int i = str.length() - 1; i >= 0; i--)
		{
			char ch = str.charAt(i);
			if (Character.isLetterOrDigit(ch))
			{
				str = str.substring(0, i + 1);
				break;
			}
		}

		Element comment = new Element(begin, this, "//", str);
		elements.add(comment);

		lastCommentLine = begin;
	}
	private int lastCommentLine;
	private boolean isValidComment(String str)
	{
		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			if (Character.isLetterOrDigit(ch))
				return true;
		}
		return false;
	}

}
