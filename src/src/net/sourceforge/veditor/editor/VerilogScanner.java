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

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * 予約語を検出する
 */
public class VerilogScanner extends RuleBasedScanner
{
	public VerilogScanner(ColorManager manager)
	{
		IToken keyword =
			new Token(
				new TextAttribute(
					manager.getColor(VerilogColorConstants.KEY_WORD)));
		IToken other =
			new Token(
				new TextAttribute(
					manager.getColor(VerilogColorConstants.DEFAULT)));

		List rules = new ArrayList();

		WordRule wordRule = new WordRule(new IWordDetector()
		{
			public boolean isWordPart(char character)
			{
				return Character.isJavaIdentifierPart(character);
			}
			public boolean isWordStart(char character)
			{
				return Character.isJavaIdentifierStart(character);
			}
		}, other);

		String[] words = {
			"module", "endmodule",
			"assign", "always", "initial",
			"wire", "reg", "input", "output", "inout",
			"if", "else", "for", "while", "case", "endcase",
			"function", "endfunction", "task", "endtask",
			"begin", "end", "parameter", "fork", "join", "integer" };
			
		for (int i = 0; i < words.length; i++)
		{
			wordRule.addWord(words[i], keyword);
		}

		rules.add(wordRule);

		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
}
