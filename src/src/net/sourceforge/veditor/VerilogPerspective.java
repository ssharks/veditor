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

package net.sourceforge.veditor;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class VerilogPerspective implements IPerspectiveFactory
{
	private static final String ID_HIERARCHY = "net.sourceforge.veditor.editor.ModuleHierarchyView";

	private static final String ID_CONSOLE = "org.eclipse.ui.console.ConsoleView";
	
	public VerilogPerspective()
	{
		super();
	}

	public void createInitialLayout(IPageLayout layout)
	{
		defineLayout(layout);
		defineActions(layout);
	}

	private void defineLayout(IPageLayout layout)
	{
		IFolderLayout left = layout.createFolder("Left", IPageLayout.LEFT,
				0.25f, IPageLayout.ID_EDITOR_AREA);
		IFolderLayout bottom = layout.createFolder("Bottom",
				IPageLayout.BOTTOM, 0.75f, IPageLayout.ID_EDITOR_AREA);
		IFolderLayout right = layout.createFolder("Right", IPageLayout.RIGHT,
				0.75f, IPageLayout.ID_EDITOR_AREA);

		addView(layout, left, IPageLayout.ID_RES_NAV);
		addView(layout, right, IPageLayout.ID_OUTLINE);
		addView(layout, right, ID_HIERARCHY);
		addView(layout, bottom, IPageLayout.ID_PROBLEM_VIEW);
		addView(layout, bottom, ID_CONSOLE);
	}

	private void addView(IPageLayout parent, IFolderLayout folder, String viewid)
	{
		folder.addView(viewid);
		IViewLayout layout = parent.getViewLayout(viewid);
		if (layout != null)
		{
			layout.setCloseable(true);
			layout.setMoveable(true);
		}
	}

	private void defineActions(IPageLayout layout)
	{
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(ID_HIERARCHY);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(ID_CONSOLE);

		layout.addNewWizardShortcut("net.sourceforge.veditor.wizard.NewVerilogWizard");
		layout.addNewWizardShortcut("net.sourceforge.veditor.wizard.NewVhdlWizard");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
	}
}








