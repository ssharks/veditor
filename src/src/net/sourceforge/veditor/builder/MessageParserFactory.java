//
//  Copyright 2004, 2006, KOBAYASHI Tadashi
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
package net.sourceforge.veditor.builder;


public class MessageParserFactory
{
	private static AbstractMessageParser parsers[] =
	{
		new CverParser(),
		new IverilogParser()
	};
	
	public static AbstractMessageParser[] createAll()
	{
		return parsers;
	}

	public static AbstractMessageParser create(String compiler)
	{
		for (int i = 0; i < parsers.length; i++)
		{
			if (parsers[i].getCompilerName().equals(compiler))
			{
				return parsers[i];
			}
		}
		return null;
	}

	static private class CverParser extends AbstractMessageParser
	{

		public String getCompilerName()
		{
			return "cver";
		}

		protected boolean parse()
		{
			String line = getLine();
			if (line.indexOf("CVER") == -1)
				return false;

			for (line = getLine(); line != null; line = getLine())
			{
				if (line.charAt(0) == '*')
				{
					String[] segs = line.split("\\*\\*", 3);
					String[] ssegs = segs[1].split("[()]");
					if (ssegs[2].indexOf("WARN") != -1)
						setWarningMarker(ssegs[0], ssegs[1], segs[2]);
					else
						setErrorMarker(ssegs[0], ssegs[1], segs[2]);
				}
			}
			return true;
		}
		
	}

	static private class IverilogParser extends AbstractMessageParser
	{
		public String getCompilerName()
		{
			return "iverilog";
		}

		protected boolean parse()
		{
			for (String line = getLine(); line != null; line = getLine())
			{
				String[] segs = line.split(":", 4);
				if (segs.length >= 3)
				{
					if (segs[2].indexOf("parse error") != -1)
					{
						setErrorMarker(segs[0], segs[1], "parse error");
					}
					else if (segs.length >= 4)
					{
						if (segs[2].indexOf("warning") != -1)
							setWarningMarker(segs[0], segs[1], segs[3]);
						else if (segs[2].indexOf("error") != -1)
							setErrorMarker(segs[0], segs[1], segs[3]);
					}
				}
			}
			return true;
		}
	}
	
}
