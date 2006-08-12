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

import org.eclipse.core.resources.IFile;

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
	
	public static IParser createVerilogParser(Reader reader, IFile file)
	{
		return new VerilogParser(new AsciiReader(reader), file);
	}

	public static IParser createVhdlParser(Reader reader, IFile file)
	{
		return new VhdlParser(new AsciiReader(reader), file);
	}


	/**
	 *	Wrapper of reader to ignore two bytes code because of JavaCC bug.
	 *	It may be no problem. The two bytes characters are allowed in comment only.
	 */
	private static class AsciiReader extends Reader
	{
		private Reader reference;

		public AsciiReader(Reader reference)
		{
			this.reference = reference;
		}
		
		public void close() throws IOException
		{
			reference.close();
		}

		public int read(char[] cbuf, int off, int len) throws IOException
		{
			int n = reference.read(cbuf, off, len);
			for (int i = 0; i < n; i++)
			{
				if (cbuf[i] >= 0x100)	// convert from two bytes code to space
					cbuf[i] = ' ';
			}
			return n;
		}
	}
}


