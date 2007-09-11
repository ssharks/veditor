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

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

/**
 * format region action (Verilog only now)
 * 
 * preference
 *   indent character
 *       tab
 *       n white spaces
 *   indent rule
 *       begin/end, fork/join, generate/endgenerate,
 *       if, for, while, repeat
 *       case, ":",
 *       port list, parameter list,
 *       wrapped expression
 *   white space rule(before and after)
 *       binary operator, unary operator, conditional operator,
 *       assign, comma, (), [], {}
 *       ":"(MSB:LSB), ";"(end of statement)
 *       if, for, while, repeat
 *   context
 *       begin/end, in [], in {}
 */

public class VerilogFormatAction extends AbstractAction {
	// preference
	private String indentString = "\t";
	private boolean noSpaceInBracket = true;
	private boolean spaceBeforeOperator2 = true;
	private boolean spaceAfterOperator2 = true;
	private boolean spaceBeforeOperator1 = false;
	private boolean spaceAfterOperator1 = false;
	private boolean spaceBeforeComma = false;
	private boolean spaceAfterComma = true;
	private boolean spaceBeforeSemicolon = false;
	private boolean spaceBeforeOpenParen = false;
	private boolean spaceAfterOpenParen = false;
	private boolean spaceBeforeCloseParen = false;
	private boolean spaceBeforeOpenBracket = false;
	private boolean spaceAfterOpenBracket = false;
	private boolean spaceBeforeCloseBracket = false;
	private boolean spaceBeforeOpenBrace = false;
	private boolean spaceAfterOpenBrace = false;
	private boolean spaceBeforeCloseBrace = false;
	private boolean spaceBeforeCaseColon = false;
	private boolean spaceAfterCaseColon = true;
	private boolean spaceAfterIf = true;
	private boolean spaceAfterFor = true;
	private boolean spaceAfterWhile = true;
	private boolean spaceAfterRepeat = true;

	// context flag
	private static final int CONTEXT_NORMAL = 1;
	private static final int CONTEXT_BRACKET = 2;
	private static final int CONTEXT_ALL = CONTEXT_NORMAL | CONTEXT_BRACKET;
	
	// space operation flag
	private static final int SP_NOCARE = 0;
	private static final int SP_1 = 1;
	private static final int SP_0 = 2;
	private static final int SP_MULTI = 3;
	
	private TokenAnalyzer analyzer = new TokenAnalyzer();
	private StringBuffer dst = new StringBuffer();
	private String indent;
	private int reservedSpace;
	private boolean afterNewLine;

	public VerilogFormatAction()
	{
		super("VerilogFormatAction");
	}
	
	private void setup()
	{
		loadPreference();

		String operators2[] = {"+", "-", "*", "/", "%", "&", "|", "^", "&&",
				"||", "<<", ">>", "==", "!=", "===", "!==", "<", ">", "<=",
				">=", "?", ":", "=", "<="};
		for (int i = 0; i < operators2.length; i++)
		{
			appendTokenRule(operators2[i], spaceBeforeOperator2,
					spaceAfterOperator2);
		}

		String operators1[] = { "!", "~" };
		for (int i = 0; i < operators1.length; i++)
		{
			appendTokenRule(operators1[i], spaceBeforeOperator1,
					spaceAfterOperator1);
		}

		appendTokenRule(",", spaceBeforeComma, spaceAfterComma);
		appendTokenRule(";", spaceBeforeSemicolon, true);

		appendTokenRule("(", spaceBeforeOpenParen, spaceAfterOpenParen);
		appendTokenRule(")", spaceBeforeCloseParen, false);
		
		appendTokenRule("[", spaceBeforeOpenBracket, spaceAfterOpenBracket);
		appendTokenRule("]", spaceBeforeCloseBracket, false);
		
		appendTokenRule("{", spaceBeforeOpenBrace, spaceAfterOpenBrace);
		appendTokenRule("}", spaceBeforeCloseBrace, false);
		
		analyzer.appendToken(".", SP_0, SP_0);
		analyzer.appendToken("//", SP_MULTI, SP_MULTI);

		appendWordRule("if", spaceAfterIf);
		appendWordRule("for", spaceAfterFor);
		appendWordRule("while", spaceAfterWhile);
		appendWordRule("repeat", spaceAfterRepeat);
		
		analyzer.appendToken("begin", SP_1, SP_1);
		analyzer.appendToken("end", SP_1, SP_1);
		analyzer.appendToken("fork", SP_1, SP_1);
		analyzer.appendToken("join", SP_1, SP_1);
		analyzer.appendToken("else", SP_1, SP_1);
	}
	
	private void loadPreference()
	{
		String indent = VerilogPlugin.getPreferenceString("Style.indent");
		if (indent.equals("Tab"))
			indentString = "\t";
		else
		{
			String size = VerilogPlugin.getPreferenceString("Style.indentSize");
			int n = Integer.parseInt(size);
			StringBuffer buf = new StringBuffer(n);
			for(int i = 0; i < n; i++)
				buf.append(' ');
			indentString = buf.toString();
		}
		
		noSpaceInBracket = getSpacePreference("Style.noSpaceInBracket");
		
		spaceBeforeOperator2 = getSpacePreference("Style.spaceAfterOperator2");
		spaceAfterOperator2 = getSpacePreference("Style.spaceAfterOperator2");
		spaceBeforeOperator1 = getSpacePreference("Style.spaceBeforeOperator1");
		spaceAfterOperator1 = getSpacePreference("Style.spaceAfterOperator1");
		spaceBeforeComma = getSpacePreference("Style.spaceBeforeComma");
		spaceAfterComma = getSpacePreference("Style.spaceAfterComma");
		spaceBeforeSemicolon = getSpacePreference("Style.spaceBeforeSemicolon");
		spaceBeforeOpenParen = getSpacePreference("Style.spaceBeforeOpenParen");
		spaceAfterOpenParen = getSpacePreference("Style.spaceAfterOpenParen");
		spaceBeforeCloseParen = getSpacePreference("Style.spaceBeforeCloseParen");
		spaceBeforeOpenBracket = getSpacePreference("Style.spaceBeforeOpenBracket");
		spaceAfterOpenBracket = getSpacePreference("Style.spaceAfterOpenBracket");
		spaceBeforeCloseBracket = getSpacePreference("Style.spaceBeforeCloseBracket");
		spaceBeforeOpenBrace = getSpacePreference("Style.spaceBeforeOpenBrace");
		spaceAfterOpenBrace = getSpacePreference("Style.spaceAfterOpenBrace");
		spaceBeforeCloseBrace = getSpacePreference("Style.spaceBeforeCloseBrace");
		spaceBeforeCaseColon = getSpacePreference("Style.spaceBeforeCaseColon");
		spaceAfterCaseColon = getSpacePreference("Style.spaceAfterCaseColon");
		spaceAfterIf = getSpacePreference("Style.spaceAfterIf");
		spaceAfterFor = getSpacePreference("Style.spaceAfterFor");
		spaceAfterWhile = getSpacePreference("Style.spaceAfterWhile");
		spaceAfterRepeat = getSpacePreference("Style.spaceAfterRepeat");
	}
	
	private boolean getSpacePreference(String name)
	{
		return VerilogPlugin.getPreferenceBoolean(name);
	}
	
	private void appendTokenRule(String word, boolean b, boolean a)
	{
		int before = b ? SP_1 : SP_0;
		int after = a ? SP_1 : SP_0;
		analyzer.appendToken(word, before, after, CONTEXT_NORMAL);
		if (noSpaceInBracket)
			analyzer.appendToken(word, SP_0, SP_0, CONTEXT_BRACKET);
		else
			analyzer.appendToken(word, before, after, CONTEXT_BRACKET);
	}

	private void appendWordRule(String word, boolean a)
	{
		int after = a ? SP_1 : SP_0;
		analyzer.appendToken(word, SP_NOCARE, after);
	}

	public void run()
	{
		setup();

		StyledText widget = getViewer().getTextWidget();

		Point point = widget.getSelection();
		int begin = point.x;
		int end = point.y;
		
		//if nothing is selected
		if(begin == end){
			return;
		}
		
		// begin index must be top of line
		int line = widget.getLineAtOffset(begin);
		begin = widget.getOffsetAtLine(line);
		String region = widget.getText(begin, end - 1);

		indent = getIndent(widget, begin);

		// initialize instance variable
		dst.setLength(0);
		reservedSpace = SP_NOCARE;
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
			else if (word.equals("while") || word.equals("repeat") || word.equals("for"))
			{
				execToken(level);
				execParen(level);
				execStatement(level);
				break;
			}
			else if (word.equals("reg") || word.equals("wire"))
			{
				execDeclaration(level);
				break;
			}
			else if (word.equals(";"))
			{
				execToken(level);
				break;
			}

			if (isStart)
				execToken(level);
			else
				execToken(level + 1);	// line break causes indent
			
			isStart = false;
		}
	}

	private void execBlock(int level)
	{
		while (!analyzer.isEos())
		{
			if (isBlockEnd(analyzer.getWord()))
			{
				execToken(level - 1);
				break;
			}
			if (analyzer.equalsToCurrent(":"))
			{
				execToken(level);	// :
				execToken(level);	// label
			}
			execRegion(level);
		}
	}
	
	private void execIf(int level)
	{
		execToken(level);	// if
		execParen(level);	// (...)
		
		execStatement(level);
		if (analyzer.equalsToCurrent("else"))
		{
			execToken(level);
			if (isCaseBegin(analyzer.getWord()))
				execCase(level);  // exception for "else case"
			else if (analyzer.equalsToCurrent("if"))
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
			while (!analyzer.equalsToCurrent(":") && !analyzer.isEos())
			{
				execToken(level + 1);
			}

			int before = spaceBeforeCaseColon ? SP_1 : SP_0;
			int after = spaceAfterCaseColon ? SP_1 : SP_0;
			analyzer.modifySpace(before, after);
			execToken(level); // :
			execStatement(level + 1);
		}
		execToken(level); // endcase
	}

	private void execParen(int level)
	{
		execToken(level); // (

		int paren = 1;
		while (paren >= 1 && !analyzer.isEos())
		{
			if (analyzer.equalsToCurrent("("))
				paren++;
			if (analyzer.equalsToCurrent(")"))
				paren--;
			execToken(level + paren);
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
	
	private void execDeclaration(int level)
	{
		execToken(level); // reg/wire
		if (analyzer.equalsToCurrent("["))
		{
			execToken(level + 1);
			while (!analyzer.equalsToCurrent("]") && !analyzer.isEos())
			{
				execToken(level + 1);
			}
			execToken(level);	// "]"
		}
		reservedSpace = SP_1;
		execRegion(level + 1);
	}

	private int execToken(int level)
	{
		String word = analyzer.getWord();

		if (afterNewLine)
		{
			dst.append(indent);
			for (int i = 0; i < level; i++)
				dst.append(indentString);
		}
		else
		{
			int beforeSpace = analyzer.getBeforeSpace();
			if (reservedSpace == SP_MULTI || beforeSpace == SP_MULTI)
				dst.append(analyzer.getSpace());
			else if (reservedSpace == SP_1 || beforeSpace == SP_1)
				dst.append(" ");
			else if (reservedSpace == SP_0 || beforeSpace == SP_0)
				; // append no space
			else
				dst.append(analyzer.getSpace());
		}
		dst.append(word);
		reservedSpace = analyzer.getAfterSpace();
		afterNewLine = false;

		analyzer.next();
		word = analyzer.getWord();
		while (word.equals("\r") || word.equals("\n") || word.equals("//"))
		{
			while (!word.equals("\n") && !analyzer.isEos())
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
		return str.equals("begin") || str.equals("fork") || str.equals("generate");
	}
	private static boolean isBlockEnd(String str)
	{
		return str.equals("end") || str.equals("join") || str.equals("endgenerate");
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
		private Map<TokenKey,Token> tokens = new HashMap<TokenKey,Token>();
	
		private int pos;
		private String src;
		private String space;
		private int context;
		private int nestingLevel;
		private int afterSpace, beforeSpace;
		private Token currentToken = new Token();
		private TokenKey workKey = new TokenKey("");
		
		public TokenAnalyzer()
		{
		}
		public void setText(String src)
		{
			this.src = src;
			context = CONTEXT_NORMAL;
			nestingLevel = 0;
			pos = 0;
			afterSpace = -1;
			beforeSpace = -1;
		}
		public void next()
		{
			beforeSpace = -1;
			afterSpace = -1;

			if (skipSpace() == false)
				return;

			char c = src.charAt(pos);
			if (c == '\"')
			{
				parseStringLiteral();
				return;
			}
			
			StringBuffer buf = new StringBuffer();
			boolean ident = isIdentChar(c);
			buf.append(c);
			pos++;
			
			while (pos < src.length())
			{
				c = src.charAt(pos);
				if (ident != isIdentChar(c))
					break;
				if (isSpace(c))
					break;
				buf.append(c);
				pos++;
			}
			
			if (ident == false)
			{
				// match longest token
				StringBuffer remains = new StringBuffer();
				while (buf.length() >= 2)
				{
					if (findToken(buf.toString()) != null)
						break;
					int len = buf.length() - 1;
					remains.insert(0, buf.charAt(len));
					buf.setLength(len);
					pos--;
				}
			}
			String word = buf.toString();
			currentToken.set(word);
			
			if (word.equals("[") || word.equals("{"))
				nestingLevel++;
			if (word.equals("]") || word.equals("}"))
				nestingLevel--;
			if (nestingLevel == 0)
				context = CONTEXT_NORMAL;
			else
				context = CONTEXT_BRACKET;
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
			currentToken.set(buf.toString());
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
				currentToken.set("");
				return false;
			}
			return true;
		}
		public void modifySpace(int before, int after)
		{
			beforeSpace = before;
			afterSpace = after;
		}

		public String getSpace()
		{
			return space;
		}
		public String getWord()
		{
			return currentToken.toString();
		}
		public int getBeforeSpace()
		{
			if (beforeSpace == -1)
				return getToken().getBeforeSpace();
			else
				return beforeSpace;
		}
		public int getAfterSpace()
		{
			if (afterSpace == -1)
				return getToken().getAfterSpace();
			else
				return afterSpace;
		}
		public boolean isEos()
		{
			return (pos >= src.length());
		}
		public boolean equalsToCurrent(Object obj)
		{
			return getWord().equals(obj);
		}
		
		private Token getToken()
		{
			Token token = findToken(getWord());
			if (token == null)
				return currentToken;
			else
				return token;
		}
		private Token findToken(String word)
		{
			// workKey is used for avoiding "new"
			workKey.set(word, context);
			return tokens.get(workKey);
		}
		
		public void appendToken(String word, int before, int after, int context)
		{
			TokenKey key = new TokenKey(word, context);
			tokens.put(key, new Token(key, before, after));
		}
		public void appendToken(String word, int before, int after)
		{
			appendToken(word, before, after, CONTEXT_ALL);
		}
	}
	
	/**
	 * Key for Token database
	 * It can be used by HashMap
	 */
	private static class TokenKey
	{
		private String word;
		private int context;
		
		public TokenKey(String word, int context)
		{
			this.word = word;
			this.context = context;
		}
		public TokenKey(String word)
		{
			this(word, CONTEXT_ALL);
		}
		
		public void set(String word, int context)
		{
			this.word = word;
			this.context = context;
		}
		public void set(String word)
		{
			set(word, CONTEXT_ALL);
		}

		public String toString()
		{
			return word;
		}
		
		public boolean equals(Object o)
		{
			if (o instanceof TokenKey)
			{
				TokenKey key = (TokenKey) o;
				if (word.equals(key.word))
				{
					if ((context & key.context) != 0)
						return true;
					else
						return false;
				}
				else
					return false;
			}
			return false;
		}

		public int hashCode()
		{
			return word.hashCode();
		}
	}
	
	/**
	 * Token for inserting/deleting white space
	 */
	private static class Token
	{
		private TokenKey key;
		private int beforeSpace;
		private int afterSpace;

		public Token(TokenKey key, int before, int after)
		{
			this.key = key;
			this.beforeSpace = before;
			this.afterSpace = after;
		}
		public Token()
		{
			this(new TokenKey(""), SP_NOCARE, SP_NOCARE);
		}

		public void set(String word)
		{
			key.set(word);
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
			return key.toString();
		}
	}
}
