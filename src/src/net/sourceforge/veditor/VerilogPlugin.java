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

package net.sourceforge.veditor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class
 */
public class VerilogPlugin extends AbstractUIPlugin {

	private static VerilogPlugin plugin;
	private ResourceBundle preferences;
	
	public VerilogPlugin(IPluginDescriptor descriptor)
	{
		super(descriptor);
		plugin = this;
		try
		{
			URL url = find(new Path("preferences.properties"));
			InputStream input;
			input = url.openStream();
			preferences = new PropertyResourceBundle(input);
		}
		catch (IOException e)
		{
			preferences = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static VerilogPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace()
	{
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin preferences file
	 */
	public static String getPreferenceString(String key)
	{
		ResourceBundle bundle = VerilogPlugin.getDefault().getPreferences();
		try
		{
			return bundle.getString(key);
		}
		catch (MissingResourceException e)
		{
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getPreferences()
	{
		return preferences;
	}
}

