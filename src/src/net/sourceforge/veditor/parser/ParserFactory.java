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

import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.IFile;

/**
 * generate Verilog or VHDL parser
 */
public class ParserFactory
{
	// don't instatiate
	private ParserFactory()
	{
	}
	
	public static IParser create(Reader reader, IFile file)
	{
		String extension = file.getFileExtension();
		if (extension.equals("v"))
			return new VerilogParser(new AsciiReader(reader));
		else if (extension.equals("vhd"))
			return new VhdlParser(new AsciiReader(reader));
		else
			return null;
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
				if (cbuf[i] >= 0x100)	// convert two bytes code to space
					cbuf[i] = ' ';
			}
			return n;
		}
	}

	
}
