/*******************************************************************************
 * Copyright (c) 2006 Ali Ghorashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali Ghorashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.actions;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.jface.resource.ImageDescriptor;

public class CollapseAll extends AbstractAction {
	private static final String COLLAPSE_ALL_ACTION_IMAGE="$nl$/icons/collapse_all.gif";
	public CollapseAll() {
		super("CollapseAll");		
	}

	@Override
	public void run() {
		getEditor().collapseAll();
	}
	
	public ImageDescriptor getImageDescriptor(){
		return VerilogPlugin.getPlugin().getImageDescriptor(COLLAPSE_ALL_ACTION_IMAGE);
	}
}
