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

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class
 */
public class VerilogPlugin extends AbstractUIPlugin
{
	private static VerilogPlugin plugin;

//	for eclipse 2.1
//	public VerilogPlugin(IPluginDescriptor descriptor)
//	{
//		super(descriptor);
//		plugin = this;
//	}

//	for eclipse 3.0
	public VerilogPlugin()
	{
		super();
		plugin = this;
	}

//	move to VerilogPreferenceInitializer
//	protected void initializeDefaultPreferences(IPreferenceStore store)
//	{
//		super.initializeDefaultPreferences(store);
//
//		store.setDefault("Color.DoxygenComment", "404080");
//		store.setDefault("Color.SingleLineComment", "008080");
//		store.setDefault("Color.MultiLineComment", "008080");
//		store.setDefault("Color.String", "000080");
//		store.setDefault("Color.Default", "000000");
//		store.setDefault("Color.KeyWord", "800080");
//		store.setDefault("Compile.command", "iverilog -tnull -y . -Wall");
//	}

	/**
	 * Returns the shared instance.
	 */
	public static VerilogPlugin getPlugin()
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
	 * Returns the string from the plugin preferences
	 */
	public static String getPreferenceString(String key)
	{
		return getPlugin().getPreferenceStore().getString(key);
	}

	/**
	 * Returns the string from the plugin preferences
	 */
	public static boolean getPreferenceBoolean(String key)
	{
		return getPlugin().getPreferenceStore().getBoolean(key);
	}
	
	
}

