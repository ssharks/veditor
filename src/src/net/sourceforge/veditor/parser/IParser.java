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

import java.io.Reader;

import org.eclipse.core.resources.IFile;

public interface IParser
{
	public static final int OUT_OF_MODULE = 0;
	public static final int IN_MODULE = 1;
	public static final int IN_STATEMENT = 2;

	public ParserManager getManager();
	public IFile getFile();
	public void parse() throws ParseException;
	public void parseLineComment(Reader reader);
}

