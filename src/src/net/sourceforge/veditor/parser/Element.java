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

/**
 * instance, function, task and comment in source code
 */
public class Element extends Segment
{
	private Segment module ;
	private String typeName ;	//  module名またはfunction名
	private String name ;		//  インスタンス名

	public Element(int line, Segment module, String typeName, String name)
	{
		super(line);
		this.module = module ;
		this.typeName = typeName ;
		this.name = name ;
	}

	public Segment getParent()
	{
		return module ;
	}

	public String toString()
	{
		return typeName + " " + name ;
	}


}
