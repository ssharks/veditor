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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * generate Verilog or VHDL parser
 */
abstract public class ParserFactory
{
	// don't instatiate
	private ParserFactory()
	{
	}
	
//	public static IParser create(Reader reader, IFile file)
//	{
//		String extension = file.getFileExtension();
//		if (extension.equals("v"))
//			return new VerilogParser(new AsciiReader(reader));
//		else if (extension.equals("vhd") || extension.equals("VHD"))
//			return new VhdlParser(new AsciiReader(reader));
//		else
//			return null;
//	}
	
	public static IParser createVerilogParser(String text, IProject project, IFile file)
	{
		return new VerilogParser(new AsciiReader(text), project, file);
	}

	public static IParser createVerilogParser(Reader reader, IProject project, IFile file)
	{
		return new VerilogParser(reader, project, file);
	}

	public static IParser createVhdlParser(String text, IProject project, IFile file)
	{
		return new VhdlParser(new AsciiReader(text), project, file);
	}

	public static IParser createVhdlParser(Reader reader, IProject project, IFile file)
	{
		return new VhdlParser(reader, project, file);
	}

	/**
	 *	Wrapper of StringReader. It can support rewind.
	 *  @note
	 *  The earlier version had special code for two byte character.
	 *  But it is no longer necessary because JavaCC can handle correctly.
	 */
	private static class AsciiReader extends Reader
	{
		private String text;
		private StringReader reference;

		public AsciiReader(String text)
		{
			this.text = text;
			reset();
		}
		
		public void close() throws IOException
		{
			reference.close();
		}

		public int read(char[] cbuf, int off, int len) throws IOException
		{
			int n = reference.read(cbuf, off, len);
			return n;
		}

		public void reset()
		{
			reference = new StringReader(text);
		}
	}
}


