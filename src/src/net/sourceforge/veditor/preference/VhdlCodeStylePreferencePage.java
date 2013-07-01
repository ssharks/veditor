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

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		addField(new BooleanFieldEditor(PreferenceStrings.PAD_OPERATORS,"Pad operators",parent));
		addField(new BooleanFieldEditor(PreferenceStrings.INDENT_LIBRARY,"Indent Library use",parent));
		addField(new BooleanFieldEditor(PreferenceStrings.KEYWORDS_LOWERCASE,"keywords in lowercase",parent));
		addField(new BooleanFieldEditor(PreferenceStrings.ALIGNONARROWRIGHT,"Align On =>",parent));
		addField(new BooleanFieldEditor(PreferenceStrings.ALIGNONARROWLEFT,"Align On <=",parent));
		addField(new BooleanFieldEditor(PreferenceStrings.ALIGNONCOLON,"Align On :",parent));
		//not supported for now
		//addField( new BooleanFieldEditor( PreferenceStrings.ALIGNONCOMMENT,"Align On --", parent ) );
		addField( new BooleanFieldEditor( PreferenceStrings.ALIGNONASSIGNMENT,"Align On :=", parent ) );
		addField( new BooleanFieldEditor( PreferenceStrings.ALIGNINOUT, "Align After \"in\", \"out\", \"inout\" and \"buffer\"", parent ) );
	}

}
