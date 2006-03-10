//
//  Copyright 2004, 2006 KOBAYASHI Tadashi
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

package net.sourceforge.veditor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class
 */
public class VerilogPlugin extends AbstractUIPlugin
{
	private static final String CONSOLE_NAME = "veditor";
	private static VerilogPlugin plugin;
	private static final String MARKER_TYPE = "org.eclipse.core.resources.problemmarker";

	public VerilogPlugin()
	{
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static VerilogPlugin getPlugin()
	{
		return plugin;
	}
	
	/**
	 * Returns PreferenceStore
	 */
	public static IPreferenceStore getStore()
	{
		return getPlugin().getPreferenceStore();
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace()
	{
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin preferences
	 */
	public static String getPreferenceString(String key)
	{
		return getStore().getString(key);
	}

	/**
	 * Returns the string from the plugin preferences
	 */
	public static boolean getPreferenceBoolean(String key)
	{
		return getStore().getBoolean(key);
	}
	
	/**
	 * Returns the RGB from the plugin preferences
	 */
	public static RGB getPreferenceColor(String key)
	{
		return PreferenceConverter.getColor(getStore(), key);
	}
	
	/**
	 * set the string to the plugin preferences
	 */
	public static void setPreference(String key, String value)
	{
		getStore().setValue(key, value);
	}

	/**
	 * set the string to the plugin preferences
	 */
	public static void setPreference(String key, boolean value)
	{
		getStore().setValue(key, value);
	}
	
	/**
	 * set the RGB to the plugin preferences
	 */
	public static void setPreference(String key, RGB rgb)
	{
		PreferenceConverter.setValue(getStore(), key, rgb);
	}
	
	public static void setDefaultPreference(String key)
	{
		getStore().setToDefault(key);
	}

	/**
	 * Show message in console view
	 */
	public static void println(String msg)
	{
		MessageConsoleStream out = findConsole(CONSOLE_NAME).newMessageStream();
		out.println(msg);
	}
	
	/**
	 * FIXME:
	 * I cannot use clearConsole!
	 * When clearConsole is called, println is ignored
	 */
	public static void clear()
	{
		findConsole(CONSOLE_NAME).clearConsole();
	}
	
	private static MessageConsole findConsole(String name)
	{
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] consoles = man.getConsoles();
		for (int i = 0; i < consoles.length; i++)
		{
			if (consoles[i].getName().equals(name))
				return (MessageConsole)consoles[i];
		}

		// if not exists, add new console
		MessageConsole newConsole = new MessageConsole(name, null);
		man.addConsoles(new IConsole[]{newConsole});
		return newConsole;
	}

	public static void setErrorMarker(IResource file, int lineNumber,
			String msg)
	{
		setProblemMarker(file, "error", lineNumber, msg);
	}

	public static void setWarningMarker(IResource file, int lineNumber,
			String msg)
	{
		setProblemMarker(file, "warning", lineNumber, msg);
	}

	public static void setProblemMarker(IResource file, String type,
			int lineNumber, String msg)
	{
		int level;
		if (type.indexOf("warning") != -1)
			level = IMarker.SEVERITY_WARNING;
		else
			level = IMarker.SEVERITY_ERROR;
		try
		{
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.SEVERITY, level);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.MESSAGE, msg);
		}
		catch (CoreException e)
		{
		}
	}

	public static void clearProblemMarker(IResource file)
	{
		try
		{
			IMarker[] markers = file.findMarkers(MARKER_TYPE, true, 1);
			for (int i = 0; i < markers.length; i++)
				markers[i].delete();
		}
		catch (CoreException e)
		{
		}
	}
}

