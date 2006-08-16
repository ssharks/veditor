/*******************************************************************************
 * Copyright (c) 2004, 2006 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    KOBAYASHI Tadashi - initial API and implementation
 *    Ken Horn - added patch
 *******************************************************************************/

package net.sourceforge.veditor.builder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.resources.IContainer;

/**
 * Launch child process
 * @author tadashi
 */
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
		if (!dir.exists())
		{
			VerilogPlugin.println("working dir: " + dir.getAbsolutePath()
					+ " does not exist");
		}
		else
		{
			if (!dir.isDirectory())
			{
				VerilogPlugin.println("working dir: " + dir.getAbsolutePath()
						+ " is not a directory");
			}
			else
			{
				if (!dir.canWrite())
				{
					VerilogPlugin.println("working dir: "
							+ dir.getAbsolutePath() + " no write access");
				}
			}
		}

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


