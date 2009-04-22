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

package net.sourceforge.veditor.editor.scanner;
import net.sourceforge.veditor.editor.HdlTextAttribute;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * This is an abstract class that defines the common behavior
 * between the VHDL and Verilog Partition scanners
 */
abstract public class HdlPartitionScanner extends RuleBasedPartitionScanner
{
	public static final String DOXYGEN_SINGLE_LINE_COMMENT = "__hdl_doxygen_singleline_comment";
	public static final String DOXYGEN_MULTI_LINE_COMMENT = "__hdl_doxygen_multiline_comment";
	public static final String SINGLE_LINE_COMMENT = "__hdl_singleline_comment";
	public static final String MULTI_LINE_COMMENT = "__hdl_multiline_comment";
	public static final String STRING = "__hdl_string";	
	public static final String TASK_TAG = "_hdl_task_tag";
	public static IToken stringToken = new Token(STRING);
	public static IToken singleLineCommentToken = new Token(SINGLE_LINE_COMMENT);
	public static IToken doxygenSingleLineCommentToken = new Token(DOXYGEN_SINGLE_LINE_COMMENT);
	public static IToken doxygenMultiLineCommentToken = new Token(DOXYGEN_MULTI_LINE_COMMENT);
	public static IToken taskTagToken = new Token(TASK_TAG);
	public static IToken multiLineCommentToken=new Token(MULTI_LINE_COMMENT);

	
	public static String[] getContentTypes()
	{
		return new String[] { 
		        SINGLE_LINE_COMMENT,
		        DOXYGEN_SINGLE_LINE_COMMENT, 
		        DOXYGEN_MULTI_LINE_COMMENT,
		        MULTI_LINE_COMMENT,
				STRING,
				TASK_TAG
				};
	}

	public static HdlTextAttribute[] getContentTypeAttributes()
	{
		// must be same sequence with getContentTypes
		return new HdlTextAttribute[] { 
		        HdlTextAttribute.SINGLE_LINE_COMMENT,
		        HdlTextAttribute.DOXYGEN_COMMENT,
		        HdlTextAttribute.DOXYGEN_COMMENT,
				HdlTextAttribute.MULTI_LINE_COMMENT, 
				HdlTextAttribute.STRING,
				HdlTextAttribute.AUTOTASKS,
				};
	}

	protected HdlPartitionScanner()
	{
		super();
	}
}
