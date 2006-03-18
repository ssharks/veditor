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

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

public abstract class AbstractMessageParser
{
	private IContainer folder;
	private String message;
	private int msgIdx;

	/**
	 * parse compiler message.
	 * if return false, ExternalBuilder have to try next MessageParser
	 * @return if success return true
	 */
	public boolean parse(IContainer folder, String message)
	{
		this.folder = folder;
		this.message = message;
		msgIdx = 0;
		return parse();
	}

	public abstract String getCompilerName();
	
	protected abstract boolean parse();
	
	//
	// utility methods
	//
	
	protected String getLine()
	{
		int next = message.indexOf("\n", msgIdx);
		if (next >= 0)
		{
			String line = message.substring(msgIdx, next);
			msgIdx = next + 1;
			return line;
		}
		else
			return null;
	}
	
	private int parseLineNumber(String str)
	{
		try
		{
			return Integer.parseInt(str);
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}
	
	protected void setErrorMarker(String filename, String line, String msg)
	{
		IResource file = folder.findMember(filename);
		int lineNumber = parseLineNumber(line);
		if (file != null && lineNumber > 0)
			VerilogPlugin.setErrorMarker(file, lineNumber, msg);
	}

	protected void setWarningMarker(String filename, String line, String msg)
	{
		IResource file = folder.findMember(filename);
		int lineNumber = parseLineNumber(line);
		if (file != null && lineNumber > 0)
			VerilogPlugin.setWarningMarker(file, lineNumber, msg);
	}

	protected void setInfoMarker(String filename, String line, String msg)
	{
		IResource file = folder.findMember(filename);
		int lineNumber = parseLineNumber(line);
		if (file != null && lineNumber > 0)
			VerilogPlugin.setInfoMarker(file, lineNumber, msg);
	}
}


