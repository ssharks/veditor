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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class VerilogPreferenceInitializer extends AbstractPreferenceInitializer
{
	public VerilogPreferenceInitializer()
	{
		super();
	}

	public void initializeDefaultPreferences()
	{
		Preferences preferences = VerilogPlugin.getPlugin().getPluginPreferences();
		preferences.setDefault("Color.DoxygenComment", "404080");
		preferences.setDefault("Color.SingleLineComment", "008080");
		preferences.setDefault("Color.MultiLineComment", "008080");
		preferences.setDefault("Color.String", "000080");
		preferences.setDefault("Color.Default", "000000");
		preferences.setDefault("Color.KeyWord", "800080");
	}
}
