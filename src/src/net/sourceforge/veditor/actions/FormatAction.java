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
package net.sourceforge.veditor.actions;

import java.util.HashMap;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

public class FormatAction extends AbstractAction
{
	// space operation flag
	private static final int REMAIN_SP = 0;
	private static final int INSERT_SP = 1;
	private static final int DELETE_SP = 2;
	private static final int DEPEND_ON_OTHER = 3;
	
	private TokenAnalyzer analyzer = new TokenAnalyzer();
	private StringBuffer dst = new StringBuffer();
	private String indent;
	private int reservedSpace;
	private boolean afterNewLine;
	private static String indentUnit = "\t";

	public FormatAction()
	{
		super("Format");

		// TODO: These should move to preferences dialog
		// The hard coding is not good solution
		String operators2[] = {"+", "-", "*", "/", "%", "&", "|", "^", "&&",
				"||", "<<", ">>", "==", "!=", "===", "!==", "<", ">", "<=",
				">=", "?", "=", "<="};
		for (int i = 0; i < operators2.length; i++)
		{
			analyzer.newToken(operators2[i], INSERT_SP, INSERT_SP);
		}
		String operators1[] = {"!", "~"};
		for (int i = 0; i < operators1.length; i++)
		{
			analyzer.newToken(operators1[i], DELETE_SP, DELETE_SP);
		}

		analyzer.newToken(",", DELETE_SP, INSERT_SP);
		analyzer.newToken(";", DELETE_SP, DELETE_SP);
		analyzer.newToken("(", DELETE_SP, DELETE_SP);
		analyzer.newToken(")", DELETE_SP, DELETE_SP);
		analyzer.newToken("{", DELETE_SP, DELETE_SP);
		analyzer.newToken("}", DELETE_SP, DELETE_SP);
		analyzer.newToken("[", DELETE_SP, DELETE_SP);
		analyzer.newToken("]", DELETE_SP, DELETE_SP);
		analyzer.newToken("//", REMAIN_SP, REMAIN_SP);
		analyzer.newToken("begin", INSERT_SP, DEPEND_ON_OTHER);
		analyzer.newToken("if", INSERT_SP, INSERT_SP);
	}

	public void run()
	{
		StyledText widget = getViewer().getTextWidget();

		Point point = widget.getSelection();
		int begin = point.x;
		int end = point.y;

		// begin index must be top of line
		int line = widget.getLineAtOffset(begin);
		begin = widget.getOffsetAtLine(line);
		String region = widget.getText(begin, end - 1);

		indent = getIndent(widget, begin);

		// initialize instance variable
		dst.setLength(0);
		reservedSpace = 0;
		afterNewLine = true;

		analyzer.setText(region);
		analyzer.next();

		while(!analyzer.isEos())
		{
			execRegion(0);
		}
		dst.append(analyzer.getSpace());

		widget.replaceTextRange(begin, end - begin, dst.toString());
	}
	
	private void execRegion(int level)
	{
		boolean isStart = true;
		while (!analyzer.isEos())
		{
			String word = analyzer.getWord();
			if (isBlockBegin(word))
			{
				execToken(level);
				execBlock(level+1);
				break;
			}
			else if (isCaseBegin(word))
			{
				execCase(level);
				break;
			}
			else if (word.equals("if"))
			{
				execIf(level);
				break;
			}
			else if (word.equals("while"))
			{
				execToken(level);
				execParen(level);
				execStatement(level);
				break;
			}
			else if (analyzer.equalWord(";"))
			{
				execToken(level);
				break;
			}
			if ( isStart )
				execToken(level);
			else
				execToken(level+1);
			isStart = false;
		}
	}

	private void execBlock(int level)
	{
		while (!analyzer.isEos())
		{
			if (isBlockEnd(analyzer.getWord()))
			{
				execToken(level-1);
				break;
			}
			execRegion(level);
		}
	}
	
	private void execIf(int level)
	{
		execToken(level);	// if
		execParen(level);	// (...)
		
		execStatement(level);
		if (analyzer.equalWord("else"))
		{
			execToken(level);
			if (isCaseBegin(analyzer.getWord()))
				execCase(level);  // exception for "else case"
			else if (analyzer.equalWord("if"))
				execIf(level);  // exception for "else if"
			else
				execStatement(level);
		}
	}
	private void execCase(int level)
	{
		execToken(level);	// case
		execParen(level+1);	// (...)
		
		while (!isCaseEnd(analyzer.getWord()) && !analyzer.isEos())
		{
			while(!analyzer.equalWord(":") && !analyzer.isEos())
			{
				execToken(level+1);
			}
			execToken(level);	// :
			execStatement(level+1);
		}
		execToken(level);	// endcase
	}

	private void execParen(int level)
	{
		execToken(level);	// (

		int paren = 1;
		while( paren >= 1 && !analyzer.isEos())
		{
			if (analyzer.equalWord("("))
				paren++;
			if (analyzer.equalWord(")"))
				paren--;
			execToken(level+paren);
		}
	}

	private void execStatement(int level)
	{
		if (isBlockBegin(analyzer.getWord()))
		{
			execToken(level);
			execBlock(level + 1);
		}
		else
			execRegion(level + 1);
	}

	private int execToken(int level)
	{
		String word = analyzer.getWord();

		if (afterNewLine)
		{
			dst.append(indent);
			for (int i = 0; i < level; i++)
				dst.append(indentUnit);
		}
		else
		{
			int beforeSpace = analyzer.getToken().getBeforeSpace();
			if (reservedSpace == REMAIN_SP || beforeSpace == REMAIN_SP)
				dst.append(analyzer.getSpace());
			else if (reservedSpace == INSERT_SP || beforeSpace == INSERT_SP)
				dst.append(" ");
			else if (reservedSpace == DELETE_SP || beforeSpace == DELETE_SP)
				; // append no space
			else
				dst.append(analyzer.getSpace());
		}
		dst.append(word);
		reservedSpace = analyzer.getToken().getAfterSpace();
		afterNewLine = false;

		analyzer.next();
		word = analyzer.getWord();
		while (word.equals("\n") || word.equals("//"))
		{
			while(!word.equals("\n") && !analyzer.isEos())
			{
				dst.append(analyzer.getSpace());
				dst.append(word);
				analyzer.next();
				word = analyzer.getWord();
			}
			dst.append("\n");
			afterNewLine = true;
			analyzer.next();
			word = analyzer.getWord();
		}
		
		return level;
	}
	private static String getIndent(StyledText widget, int pos)
	{
		String text = widget.getText().substring(pos);
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (!isSpace(c))
				return text.substring(0, i);
		}
		return text;
	}
	private static boolean isBlockBegin(String str)
	{
		return str.equals("begin") || str.equals("fork");
	}
	private static boolean isBlockEnd(String str)
	{
		return str.equals("end") || str.equals("join");
	}
	private static boolean isCaseBegin(String str)
	{
		return str.equals("case") || str.equals("casex");
	}
	private static boolean isCaseEnd(String str)
	{
		return str.equals("endcase");
	}
	private static boolean isIdentChar(char ch)
	{
		return Character.isLetterOrDigit(ch) || ch == '_';
	}
	private static boolean isSpace(char c)
	{
		return c == ' ' || c == '\t';
	}
	
	private static class TokenAnalyzer
	{
		private HashMap tokens = new HashMap();
	
		private int pos;
		private String src;
		private String space;
		private String word;
		private Token defaultToken = new Token();
		
		public TokenAnalyzer()
		{
		}
		public void setText(String src)
		{
			this.src = src;
			pos = 0;
		}
		public boolean next()
		{
			if (skipSpace() == false)
				return false;

			char c = src.charAt(pos);
			if (c == '\n')
			{
				word = "\n";
				pos++;
				return true;
			}
			if (c == '\"')
			{
				parseStringLiteral();
				return true;
			}
			
			StringBuffer buf = new StringBuffer();
			boolean type = isIdentChar(c);
			buf.append(c);
			pos++;
			
			while (pos < src.length())
			{
				c = src.charAt(pos);
				if (type != isIdentChar(c))
					break;
				if (isSpace(c))
					break;
				buf.append(c);
				pos++;
			}
			
			if (type)
			{
				word = buf.toString();
			}
			else
			{
				// match longest token
				StringBuffer remains = new StringBuffer();
				while (buf.length() >= 2)
				{
					if (tokens.get(buf.toString()) != null)
						break;
					int len = buf.length() - 1;
					remains.insert(0, buf.charAt(len));
					buf.setLength(len);
					pos--;
				}
				word = buf.toString();
			}
			return true;
		}
		
		private void parseStringLiteral()
		{
			StringBuffer buf = new StringBuffer();
			buf.append('\"');
			pos++ ;
			while (pos < src.length())
			{
				char c = src.charAt(pos);
				buf.append(c);
				pos++;
				if (c == '\"')
					break;
			}
			word = buf.toString();
		}
		
		private boolean skipSpace()
		{
			StringBuffer buf = new StringBuffer();
			while (pos < src.length())
			{
				char c = src.charAt(pos);
				if (isSpace(c))
					buf.append(c);
				else
					break;
				pos++;
			}
			space = buf.toString();
			if (pos == src.length())
			{
				word = "";
				return false;
			}
			return true;
		}
		public String getSpace()
		{
			return space;
		}
		public String getWord()
		{
			return word;
		}
		public Token getToken()
		{
			Token token = (Token)tokens.get(word.toString());
			if (token == null)
			{
				defaultToken.set(word);
				return defaultToken;
			}
			else
				return token;
		}
		public boolean isEos()
		{
			return (pos >= src.length());
		}
		public boolean equalWord(Object obj)
		{
			return word.equals(obj);
		}
		public void newToken(String word, int before, int after)
		{
			tokens.put(word, new Token(word, before, after));
		}
		
	}

	/**
	 * Token recognized by Formatter
	 */
	private static class Token
	{
		private String word;
		private int beforeSpace;
		private int afterSpace;

		public Token()
		{
			this.word = null;
			this.beforeSpace = DEPEND_ON_OTHER;
			this.afterSpace = DEPEND_ON_OTHER;
		}
		public Token(String word, int before, int after)
		{
			this.word = word;
			this.beforeSpace = before;
			this.afterSpace = after;
		}
		public void set(String word)
		{
			this.word = word;
		}
		public int getBeforeSpace()
		{
			return beforeSpace;
		}
		public int getAfterSpace()
		{
			return afterSpace;
		}
		public String toString()
		{
			return word;
		}
	}
}

