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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;

public class CompileAction extends AbstractActionDelegate
{
	private static final String MARKER_TYPE = "org.eclipse.core.resources.problemmarker";

	public CompileAction()
	{
	}
	
	public void run(IAction action)
	{
		IFile file = getEditor().getVerilogDocument().getFile();
		IFolder folder = (IFolder)file.getParent();
		File dir = folder.getLocation().toFile();

		String command = VerilogPlugin.getPreferenceString("Compile.command")
				+ " " + file.getName();
		
		String msg = executeCompiler(dir, command);
		try
		{
			IMarker[] markers = folder.findMarkers(MARKER_TYPE, true, 1);
			for (int i = 0; i < markers.length; i++)
				markers[i].delete();
		}
		catch (CoreException e)
		{
		}
		parseMessage(msg, folder);

//		System.out.println(command);
//		System.out.println(msg);
	}

	private String executeCompiler(File dir, String command)
	{
		Runtime runtime = Runtime.getRuntime();
		String message = "";
		try
		{
			Process process = runtime.exec(command, null, dir);
			process.waitFor();

			int len;
			InputStream stdout = process.getInputStream();
			len = stdout.available();
			if ( len > 0 )
			{
				byte msg[] = new byte[len];
				stdout.read(msg);
				message += new String(msg);
			}
			InputStream stderr = process.getErrorStream();
			len = stderr.available();
			if ( len > 0 )
			{
				byte msg[] = new byte[len];
				stderr.read(msg);
				message += new String(msg);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return message;
	}
	
	private void parseMessage(String msg, IFolder folder)
	{
		String[] lines = msg.split("\n");
		for (int i = 0; i < lines.length; i++)
		{
			String[] segs = lines[i].split(":", 4);
			if (segs.length >= 4)
			{
				IResource resource = folder.findMember(segs[0]);
				if ( resource != null )
				{
					try
					{
						int lineNumber = Integer.parseInt(segs[1]);
						setProblemMarker(resource, segs[2], lineNumber, segs[3]);
					}
					catch(NumberFormatException e)
					{
					}
				}
			}
		}
	}
	
	private void setProblemMarker(IResource file, String type, int lineNumber,
			String msg)
	{
		try
		{
			IMarker marker = file.createMarker(MARKER_TYPE);
			if ( type.equals(" error") )
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			else if ( type.equals(" warning") )
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.MESSAGE, msg);
		}
		catch (CoreException e)
		{
		}
	}

	public void dispose()
	{
	}

	public void init(IWorkbenchWindow window)
	{
	}
}





