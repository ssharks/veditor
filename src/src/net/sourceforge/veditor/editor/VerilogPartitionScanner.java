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
import org.eclipse.jface.text.rules.WordRule;

/**
 * ÉRÉÅÉìÉgÇåüèoÇ∑ÇÈ
 */
public class VerilogPartitionScanner extends RuleBasedPartitionScanner
{
	public final static String VERILOG_DEFAULT = "__verilog_default";
	public final static String VERILOG_SINGLE_LINE_COMMENT = "__verilog_singleline_comment";
	public final static String VERILOG_MULTI_LINE_COMMENT = "__verilog_multiline_comment";
	public final static String VERILOG_STRING = "__verilog_string";

	public VerilogPartitionScanner()
	{
		super();

		IToken string = new Token(VERILOG_STRING);
		IToken multiLineComment = new Token(VERILOG_MULTI_LINE_COMMENT);
		IToken singleLineComment = new Token(VERILOG_SINGLE_LINE_COMMENT);

		List rules = new ArrayList();

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
	 * Detector for empty comments.
	 */
	static class EmptyCommentDetector implements IWordDetector
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

	/**
	 * Word rule for empty comments.
	 */
	static class EmptyCommentRule extends WordRule implements IPredicateRule
	{
		private IToken successToken;
		public EmptyCommentRule(IToken successToken)
		{
			super(new EmptyCommentDetector());
			this.successToken = successToken;
			addWord("/**/", this.successToken); //$NON-NLS-1$
		}

		public IToken evaluate(ICharacterScanner scanner, boolean resume)
		{
			return evaluate(scanner);
		}

		public IToken getSuccessToken()
		{
			return successToken;
		}
	}
}
