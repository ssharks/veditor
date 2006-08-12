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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.jface.text.rules.WordRule;

/**
 * parse comment
 */
abstract public class HdlPartitionScanner extends RuleBasedPartitionScanner
{
	public static final String DOXYGEN_COMMENT = "__hdl_doxygen_comment";

	public static final String SINGLE_LINE_COMMENT = "__hdl_singleline_comment";

	public static final String MULTI_LINE_COMMENT = "__hdl_multiline_comment";

	public static final String STRING = "__hdl_string";

	public static HdlPartitionScanner createVerilogPartitionScanner()
	{
		return new VerilogPartitionScanner();
	}

	public static HdlPartitionScanner createVhdlPartitionScanner()
	{
		return new VhdlPartitionScanner();
	}

	public static String[] getContentTypes()
	{
		return new String[] { DOXYGEN_COMMENT, SINGLE_LINE_COMMENT, MULTI_LINE_COMMENT,
				STRING };
	}

	public static HdlTextAttribute[] getContentTypeAttributes()
	{
		// must be same sequence with getContentTypes
		return new HdlTextAttribute[] { HdlTextAttribute.DOXYGEN_COMMENT,
				HdlTextAttribute.SINGLE_LINE_COMMENT,
				HdlTextAttribute.MULTI_LINE_COMMENT, HdlTextAttribute.STRING };
	}

	private HdlPartitionScanner()
	{
		super();
	}

	private static class VerilogPartitionScanner extends HdlPartitionScanner
	{
		public VerilogPartitionScanner()
		{
			super();

			IToken string = new Token(STRING);
			IToken multiLineComment = new Token(MULTI_LINE_COMMENT);
			IToken singleLineComment = new Token(SINGLE_LINE_COMMENT);
			IToken doxygenComment = new Token(DOXYGEN_COMMENT);

			List rules = new ArrayList();

			// doxygen comment
			rules.add(new EndOfLineRule("///", doxygenComment));
			rules.add(new EndOfLineRule("//@", doxygenComment));
			rules.add(new MultiLineRule("/**", "*/", doxygenComment));

			// single line comments.
			rules.add(new EndOfLineRule("//", singleLineComment));

			// strings.
			rules.add(new SingleLineRule("\"", "\"", string, '\\'));

			// special case word rule.
			EmptyCommentRule wordRule = new EmptyCommentRule(multiLineComment);
			rules.add(wordRule);

			// multi-line comments
			rules.add(new MultiLineRule("/*", "*/", multiLineComment));

			IPredicateRule[] result = new IPredicateRule[rules.size()];
			rules.toArray(result);
			setPredicateRules(result);
		}

		/**
		 * Word rule for empty comments.
		 */
		private static class EmptyCommentRule extends WordRule implements IPredicateRule
		{
			private IToken successToken;

			public EmptyCommentRule(IToken successToken)
			{
				super(new EmptyCommentDetector());
				this.successToken = successToken;
				addWord("/**/", this.successToken);
			}

			public IToken evaluate(ICharacterScanner scanner, boolean resume)
			{
				return evaluate(scanner);
			}

			public IToken getSuccessToken()
			{
				return successToken;
			}

			/**
			 * Detector for empty comments.
			 */
			private static class EmptyCommentDetector implements IWordDetector
			{
				public boolean isWordStart(char c)
				{
					return (c == '/');
				}

				public boolean isWordPart(char c)
				{
					return (c == '*' || c == '/');
				}
			}

		}
	}

	private static class VhdlPartitionScanner extends HdlPartitionScanner
	{
		public VhdlPartitionScanner()
		{
			super();

			IToken string = new Token(STRING);
			IToken singleLineComment = new Token(SINGLE_LINE_COMMENT);

			List rules = new ArrayList();

			// single line comments.
			rules.add(new EndOfLineRule("--", singleLineComment));

			// strings.
			rules.add(new SingleLineRule("\"", "\"", string, '\\'));
			rules.add(new WordPatternRule(new StdLogicDetector(), "\'", "\'", string));

			IPredicateRule[] result = new IPredicateRule[rules.size()];
			rules.toArray(result);
			setPredicateRules(result);
		}

		private static class StdLogicDetector implements IWordDetector
		{
			public boolean isWordStart(char c)
			{
				return (c == '\'');
			}

			public boolean isWordPart(char c)
			{
				String words = "UX01ZWLH-";
				return (c == '\'') || (words.indexOf(c) != -1);
			}
		}
	}

}
