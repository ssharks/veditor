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
import java.util.List;

import org.eclipse.core.resources.IProject;

/**
 * super class of VerilogParser<p/>
 * for separating definition from JavaCC code
 */
abstract public class VerilogParserBase
{
	private List mods = new ArrayList();

	private Module getCurrentModule()
	{
		int n = mods.size() - 1 ;
		return (Module)mods.get( n );
	}

	// called by VerilogParser
	protected void addModule(int begin, String name)
	{
		Module module = new Module(begin, name); 
		mods.add(module);
	}
	protected void endModule(int line)
	{
		getCurrentModule().setEndLine(line);
	}
	protected void addPort(int line, String portName)
	{
		getCurrentModule().addPort(portName);

		// addElement(line, line, "port", portName);
	}
	protected void addElement(int begin, int end, String module, String inst)
	{
		getCurrentModule().addElement(begin, end, module, inst);
	}
	protected void addComment( int begin, String comment )
	{
		getCurrentModule().addComment( begin, comment );
	}

	//  called by editor
	public Segment getModule( int n )
	{
		return (Segment)mods.get( n );
	}
	public int size()
	{
		return mods.size();
	}

	public void parse(IProject project)
	{
		ModuleList.setCurrent(project);
		try
		{
			parse();
		}
		catch (ParseException e)
		{
			System.out.println( e );
		}
	}
	
	public void dispose()
	{
		mods = null;
	}
	
	abstract protected void parse() throws ParseException;
}





