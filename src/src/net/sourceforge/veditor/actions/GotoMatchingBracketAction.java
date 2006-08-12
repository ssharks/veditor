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
package net.sourceforge.veditor.actions;

import org.eclipse.swt.custom.StyledText;

/**
 * jamp bracket which includes begin/end
 */
public class GotoMatchingBracketAction extends AbstractAction
{
	public GotoMatchingBracketAction()
	{
		super("GotoMatchingBracket");
	}
	public void run()
	{
		StyledText widget = getViewer().getTextWidget();
		String text = widget.getText();

		int pos = widget.getCaretOffset();
		String[] open = { "(", "{", "[", "begin", "fork" };
		String[] close = { ")", "}", "]", "end", "join" };

		int openIdx = searchWord(open, text, pos);
		int closeIdx = searchWord(close, text, pos);

		int refPos = -1;
		if (openIdx != -1)
		{
			refPos = searchCloseBracket(text, pos, open[openIdx], close[openIdx]);
		}
		else if (closeIdx != -1)
		{
			refPos = searchOpenBracket(text, pos, open[closeIdx], close[closeIdx]);
		}
		if (refPos >= 0)
		{
			widget.setSelection(refPos);
			return;
		}
		beep();
	}

	/**
	 * search open/close keywords
	 * @param words	keywords
	 * @param text	refered text
	 * @param pos	begining of searching
	 * @return keywords index
	 */
	private int searchWord(String[] words, String text, int pos)
	{
		for (int i = 0; i < words.length; i++)
		{
			int len = words[i].length();
			if (text.substring(pos - len, pos).equals(words[i]))
				return i;
		}
		return -1;
	}

	/**
	 * search closing bracket
	 * @param text	refered text
	 * @param pos	begining of searching
	 * @param open	open word
	 * @param close	close word
	 * @return offset
	 */
	private int searchCloseBracket(String text, int pos, String open, String close)
	{
		int level = 1;
		int len = text.length();
		pos++;
		int openLen = open.length();
		int closeLen = close.length();
		while (pos < len)
		{
			// String ref = text.substring(pos, pos + 1);
			if (testBracket(text, pos, open, openLen))
				level++;
			if (testBracket(text, pos, close, closeLen))
				level--;
			if (level == 0)
				return pos + closeLen;
			pos++;
		}
		return -1;
	}

	/**
	 * search opening bracket
	 * @param text	refered text
	 * @param pos	begining of searching
	 * @param open	open word
	 * @param close	close word
	 * @return offset
	 */
	private int searchOpenBracket(String text, int pos, String open, String close)
	{
		int level = 1;
		int openLen = open.length();
		int closeLen = close.length();
		pos -= 1 + closeLen;
		while (pos >= 0)
		{
			if (testBracket(text, pos, open, openLen))
				level--;
			if (testBracket(text, pos, close, closeLen))
				level++;
			if (level == 0)
				return pos + openLen;
			pos--;
		}
		return -1;
	}

	private boolean testBracket(String text, int pos, String bracket, int len)
	{
		if (bracket.equals(text.substring(pos, pos + len)))
		{
			if (Character.isJavaIdentifierStart(bracket.charAt(0)))
			{
				return !Character.isJavaIdentifierPart(text.charAt(pos - 1))
						&& !Character.isJavaIdentifierPart(text.charAt(pos + len));
			}
			else
				return true;
		}
		else
			return false;
	}
}



