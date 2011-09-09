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

import net.sourceforge.veditor.parser.verilog.VerilogParser;
import net.sourceforge.veditor.parser.vhdl.VhdlParser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * generate Verilog or VHDL parser
 */
abstract public class ParserFactory
{
	// don't instantiate
	private ParserFactory()
	{
	}
		
	public static IParser createVerilogParser(String text, IProject project, IFile file)
	{   
	    ParserReader reader=new ParserReader(text);
		return new VerilogParser(reader, project, file);
	}

	public static IParser createVerilogParser(ParserReader reader, IProject project, IFile file)
	{	    
		return new VerilogParser(reader, project, file);
	}

	public static IParser createVhdlParser(String text, IProject project, IFile file)
	{
	    ParserReader reader=new ParserReader(text);
		return new VhdlParser(reader, project, file);
	}

	public static IParser createVhdlParser(ParserReader reader, IProject project, IFile file)
	{
		return new VhdlParser(reader, project, file);
	}

}


