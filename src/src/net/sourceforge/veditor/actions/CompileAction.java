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
package net.sourceforge.veditor.actions;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.builder.AbstractMessageParser;
import net.sourceforge.veditor.builder.ExternalLauncher;
import net.sourceforge.veditor.builder.MessageParserFactory;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;

public class CompileAction extends AbstractAction
{
	public CompileAction()
	{
		super("Compile");
	}
	
	public void run()
	{
		IFile file = getEditor().getHdlDocument().getFile();
		IContainer parent = file.getParent();
		IContainer folder = parent;
		
		String command = VerilogPlugin.getPreferenceString("Compile.command")
				+ " " + file.getName();

		ExternalLauncher launchar = new ExternalLauncher(folder, command);
		launchar.run();
		
		String msg = launchar.getMessage();

		AbstractMessageParser[] parsers = MessageParserFactory.createAll();
		for(int i = 0 ; i < parsers.length ; i++)
		{
			parsers[i].parse(folder, msg);
		}

		getEditor().update();
	}
}






