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
package net.sourceforge.veditor.editor;

import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.ParserFactory;

public class VhdlDocument extends HdlDocument
{
	public VhdlDocument(IProject project, IFile file)
	{
		super(project, file);
	}

	public HdlPartitionScanner createPartitionScanner()
	{
		return HdlPartitionScanner.createVhdlPartitionScanner();
	}
	
	public IParser createParser(Reader reader)
	{
		return ParserFactory.createVhdlParser(reader);
	}

}
