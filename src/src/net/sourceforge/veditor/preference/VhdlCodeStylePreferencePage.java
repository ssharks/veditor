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
package net.sourceforge.veditor.preference;


import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class VhdlCodeStylePreferencePage extends AbstractSimplePreferencePage {
	public static final String PAD_OPERATORS="Style.Vhdl.PadOperators";
	public static final String INDENT_LIBRARY="Style.Vhdl.IndentLibrary";
	public static final String KEYWORDS_LOWERCASE="Style.Vhdl.KeywordsLowercase";
	public static final String ALIGNONARROWRIGHT="Style.Vhdl.AlignOnArrowRight";
	public static final String ALIGNONARROWLEFT="Style.Vhdl.AlignOnArrowLeft";
	public static final String ALIGNONCOLON="Style.Vhdl.AlignOnColon";
	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		addField(new BooleanFieldEditor(PAD_OPERATORS,"Pad operators",parent));
		addField(new BooleanFieldEditor(INDENT_LIBRARY,"Indent Library use",parent));
		addField(new BooleanFieldEditor(KEYWORDS_LOWERCASE,"keywords in lowercase",parent));
		addField(new BooleanFieldEditor(ALIGNONARROWRIGHT,"Align On =>",parent));
		addField(new BooleanFieldEditor(ALIGNONARROWLEFT,"Align On <=",parent));
		addField(new BooleanFieldEditor(ALIGNONCOLON,"Align On :",parent));
	}

}
