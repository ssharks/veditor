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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	 * Returns the string list separated by "\n" from the plugin preferences
	 */
	public static List getPreferenceStrings(String key)
	{
		String string = getPreferenceString(key);
		if (string == null)
			return null;

		int index = string.indexOf('\n');
		if (index >= 0)
		{
			// check version number
			if (!string.substring(0, index).equals("1"))
				return null;
		}
	
		List list = new ArrayList();
		int length = string.length();
		while(index >= 0 && index < length - 1)
		{
			int next = string.indexOf('\n', index + 1);
			if (next >= 0)
			{
				list.add(string.substring(index + 1, next));
			}
			index = next;
		}
		return list;
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
	
	/**
	 * set the string list separated by "\n"
	 */
	public static void setPreference(String key, List list)
	{
		StringBuffer value = new StringBuffer("1\n");
		Iterator i = list.iterator();
		while(i.hasNext())
		{
			value.append(i.next().toString());
			value.append("\n");
		}
		setPreference(key, value.toString());
	}
	
	/**
	 * initialize default
	 */
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

	public static void setErrorMarker(IResource file, int lineNumber, String msg)
	{
		setProblemMarker(file, IMarker.SEVERITY_ERROR, lineNumber, msg);
	}

	public static void setWarningMarker(IResource file, int lineNumber,
			String msg)
	{
		setProblemMarker(file, IMarker.SEVERITY_WARNING, lineNumber, msg);
	}

	public static void setInfoMarker(IResource file, int lineNumber, String msg)
	{
		setProblemMarker(file, IMarker.SEVERITY_INFO, lineNumber, msg);

	}

	public static void setProblemMarker(IResource file, int level,
			int lineNumber, String msg)
	{
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
			IMarker[] markers = file.findMarkers(MARKER_TYPE, true,
					IResource.DEPTH_INFINITE);
			for (int i = 0; i < markers.length; i++)
				markers[i].delete();
		}
		catch (CoreException e)
		{
		}
	}
}

