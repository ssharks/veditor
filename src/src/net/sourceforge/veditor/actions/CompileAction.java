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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;

public class CompileAction extends AbstractActionDelegate
{
	public CompileAction()
	{
	}
	
	public void run(IAction action)
	{
		IFile file = getEditor().getHdlDocument().getFile();
		IContainer parent = file.getParent();
		IContainer folder = parent;
		File dir = folder.getLocation().toFile();

		String command = VerilogPlugin.getPreferenceString("Compile.command")
				+ " " + file.getName();
		
		String msg = executeCompiler(dir, command);
		VerilogPlugin.clearProblemMarker(folder);

		// show message in console
		VerilogPlugin.println(msg);

		parseMessage(msg, folder);
		getEditor().update();
	}

	private String executeCompiler(File dir, String command)
	{
		//System.out.println(command);

		Runtime runtime = Runtime.getRuntime();
		try
		{
			Process process = runtime.exec(command, null, dir);
			MessageThread stderr = new MessageThread(process.getErrorStream());
			MessageThread stdout = new MessageThread(process.getInputStream());
			stderr.start();
			stdout.start();

			process.waitFor();
			
			return stderr.get() + stdout.get();
		}
		catch (IOException e)
		{
		}
		catch (InterruptedException e)
		{
		}
		return "";
	}
	
	private void parseMessage(String msg, IContainer folder)
	{
		String[] lines = msg.split("\n");
		for (int i = 0; i < lines.length; i++)
		{
			String[] segs = lines[i].split(":", 4);
			if (segs.length >= 3)
			{
				IResource resource = folder.findMember(segs[0]);
				if (resource != null)
				{
					try
					{
						int lineNumber = Integer.parseInt(segs[1]);
						if (segs[2].indexOf("parse error") != -1)
						{
							VerilogPlugin.setProblemMarker(resource, "error",
									lineNumber, "parse error");
						}
						else if (segs.length >= 4)
						{
							VerilogPlugin.setProblemMarker(resource, segs[2],
									lineNumber, segs[3]);
						}
					}
					catch (NumberFormatException e)
					{
					}
				}
			}
		}
	}

	private class MessageThread extends Thread
	{
		private Reader reader;
		private StringBuffer buffer;

		public MessageThread( InputStream is )
		{
			reader = new InputStreamReader(new BufferedInputStream(is));
			buffer = new StringBuffer();
		}
		
		public void run()
		{
			int c;
			try
			{
				c = reader.read();
				while( c != -1 )
				{
					buffer.append((char)c);
					c = reader.read();
				}
			}
			catch (IOException e)
			{
			}
		}
		
		public String get()
		{
			return buffer.toString();
		}
	}
}






