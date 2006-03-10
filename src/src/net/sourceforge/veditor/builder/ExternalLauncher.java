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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.resources.IContainer;

public class ExternalLauncher extends Thread
{
	private IContainer folder;
	private String command;
	private StringBuffer message;

	public ExternalLauncher(IContainer folder, String command)
	{
		this.folder = folder;
		this.command = command;
		message = new StringBuffer();
	}
	public void run()
	{
		//System.out.println(command);

		File dir = folder.getLocation().toFile();
		Runtime runtime = Runtime.getRuntime();
		try
		{
			Process process = runtime.exec(command, null, dir);
			MessageThread stderr = new MessageThread(process.getErrorStream());
			MessageThread stdout = new MessageThread(process.getInputStream());
			stderr.start();
			stdout.start();
			
			try
			{
				process.waitFor();
			}
			catch (InterruptedException e)
			{
				process.destroy();
			}
		}
		catch (IOException e)
		{
			String msg = "Runtime error: cannot execute " + command;
			VerilogPlugin.println(msg);
		}
	}

	public void waitFor()
	{
		waitFor(0);
	}

	public void waitFor(int msec)
	{
		try
		{
			if (msec == 0)
			{
				while(isAlive())
				{
					join(500);
				}
			}
			else
			{
				if (isAlive())
				{
					join(msec);
				}
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getMessage()
	{
		return message.toString();
	}
	
	private class MessageThread extends Thread
	{
		private Reader reader;

		public MessageThread(InputStream is)
		{
			reader = new InputStreamReader(new BufferedInputStream(is));
		}

		public void run()
		{
			int c;
			StringBuffer buffer = new StringBuffer();
			try
			{
				c = reader.read();
				while (c != -1)
				{
					if (c == '\n')
					{
						VerilogPlugin.println(buffer.toString());
						message.append(buffer);
						message.append('\n');
						buffer.setLength(0);
					}
					else
						buffer.append((char) c);
					c = reader.read();
				}
			}
			catch (IOException e)
			{
			}
		}
	}
}


