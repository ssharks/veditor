/*******************************************************************************
 * Copyright (c) 2007 Ali Ghorashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali Ghorashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.actions;

import net.sourceforge.veditor.editor.VerilogEditor;
import net.sourceforge.veditor.editor.VhdlEditor;

public class FormatAction extends AbstractAction
{
	private VerilogFormatAction m_VerilogFormatAction;
	private VhdlFormatAction m_VhdlFormatAction;

	public FormatAction()
	{
		super("Format");
		m_VhdlFormatAction=new VhdlFormatAction();
		m_VerilogFormatAction=new VerilogFormatAction();		
	}
	
	
	public void run()
	{
		//run the appropriate format action
		if(getEditor() instanceof VhdlEditor){
			m_VhdlFormatAction.run();
		}
		else if(getEditor() instanceof VerilogEditor){
			m_VerilogFormatAction.run();
		}
	}
	
	
}

