/*******************************************************************************
 * Copyright (c) 2004, 2006 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    KOBAYASHI Tadashi - initial API and implementation
 *******************************************************************************/

package net.sourceforge.veditor.editor;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class EditorMessages
{
	private static final String RESOURCE_BUNDLE =
		"net.sourceforge.veditor.editor.EditorMessages";

	private static ResourceBundle resourceBundle =
		ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private EditorMessages()
	{
	}

	public static String getString(String key)
	{
		try
		{
			return resourceBundle.getString(key);
		}
		catch (MissingResourceException e)
		{
			return "!" + key + "!";
		}
	}

	public static ResourceBundle getResourceBundle()
	{
		return resourceBundle;
	}
}
