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
package net.sourceforge.veditor.actions;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.parser.vhdl.SimpleCharStream;
import net.sourceforge.veditor.parser.vhdl.Token;
import net.sourceforge.veditor.parser.vhdl.VhdlParserCoreTokenManager;
import net.sourceforge.veditor.preference.VhdlCodeStylePreferencePage;
import net.sourceforge.veditor.editor.scanner.HdlScanner;

import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

public class VhdlFormatAction extends AbstractAction {
	private boolean m_UseSpaceForTab;
	private int     m_IndentSize=0;
	private boolean m_PadOperators=true;
	private boolean m_IndentLibrary=true;
	private boolean m_KeywordsLowercase = true;
	private boolean m_AlignOnArrowRight = true;
	private boolean m_AlignOnArrowLeft = true;
	private boolean m_AlignOnColon = true;
	private String m_eol;
	
	public VhdlFormatAction(){
		super("VhdlFormatAction");
	}

	@Override
	public void run() {		
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
		String selectedText = widget.getText(begin, end - 1);		
		//get the user preferences
		getPreferences();
		
		selectedText = selectedText.replaceAll("\r\n","\n");
		selectedText = selectedText.replaceAll("\r","\n");
	
		selectedText=fixIndentation(selectedText);	
		selectedText=addSpacePadding(selectedText);
		selectedText=convertTabs(selectedText);		
		
		//replace the text
		widget.replaceTextRange(begin, end - begin, selectedText);
	}
	
	/**
	 * Gets the code style preferences
	 */
	private void getPreferences(){
		String indent = VerilogPlugin.getPreferenceString("Style.indent");
		if (indent.equals("Tab"))
			m_UseSpaceForTab=false;
		else
		{
			m_UseSpaceForTab=true;
			String size = VerilogPlugin.getPreferenceString("Style.indentSize");
			m_IndentSize= Integer.parseInt(size);			
		}
		
		m_PadOperators=VerilogPlugin.getPreferenceBoolean(VhdlCodeStylePreferencePage.PAD_OPERATORS);
		m_IndentLibrary=VerilogPlugin.getPreferenceBoolean(VhdlCodeStylePreferencePage.INDENT_LIBRARY);
		m_KeywordsLowercase = VerilogPlugin.getPreferenceBoolean(VhdlCodeStylePreferencePage.KEYWORDS_LOWERCASE);
		m_AlignOnColon = VerilogPlugin.getPreferenceBoolean(VhdlCodeStylePreferencePage.ALIGNONCOLON);
		m_AlignOnArrowRight = VerilogPlugin	.getPreferenceBoolean(VhdlCodeStylePreferencePage.ALIGNONARROWRIGHT);
		m_AlignOnArrowLeft = VerilogPlugin	.getPreferenceBoolean(VhdlCodeStylePreferencePage.ALIGNONARROWLEFT);
		m_eol = TextUtilities.getDefaultLineDelimiter(getViewer().getDocument());
	}
	
	/**
	 * Increases or decreases the indent value of a given line by the specified adjustment
	 * @param map The line to indent value mapping
	 * @param line the Line to adjust
	 * @param adjustment the amount of adjustment needed
	 */
	private void adjustLineIndentValue(HashMap<Integer,Integer> map,int line,int adjustment){
		//if the line exists, we need to change it
		if(map.containsKey(line)){
			int value=map.get(line)+adjustment;			
			map.put(line, value);
		}
		else{
			//just add the adjustment
			map.put(line, adjustment);
		}
	}
	
	/**
	 * Scans the token list for the next occurance of the given image 
	 * @param image Token image
	 * @param tokens Token list
	 * @param startIdx starting index
	 * @return the index of the next token with the given image. tokens.size() if not found
	 */
	private int skipTo(String image,ArrayList<Token> tokens,int startIdx){		
		while(startIdx < tokens.size()){
			if (tokens.get(startIdx).image.toUpperCase().equals(image.toUpperCase())){
				break;
			}
			startIdx++;
		}
		return startIdx;
	}	
	/**
	 * Scans the token list for the next matching close parenthesis 
	 * @param tokens Token list
	 * @param startIdx starting index
	 * @return the index of the next token with the given image. tokens.size() if not found
	 */
	private int skipToCloseParan(ArrayList<Token> tokens,int startIdx){
		int numOpen=1;
		while(startIdx < tokens.size()){
			if (tokens.get(startIdx).image.equals("(")){
				numOpen++;
			}else if (tokens.get(startIdx).image.equals(")")){
				numOpen--;
				if(numOpen == 0){
					break;
				}
			}
			
			startIdx++;
		}
		return startIdx;
	}
	/**
	 * Computes the amount of indentation that needs to be applied to a set of
	 * lines based on the tokens
	 * @param tokens Tokens in the lines
	 * @return A map between lines numbers and the indentation adjustment
	 */
	private HashMap<Integer,Integer> computeIndentation(ArrayList<Token> tokens){
		HashMap<Integer,Integer> results=new HashMap<Integer,Integer>();
		//compute indentation
		
		// when has another meaning when it is used inside or outside a case
		// we keep a counter whether we are in a case structure or not
		// (cases can be nested)
		int incase = 0;
		int use;
		int EOS;
		for(int i=0;i < tokens.size();i++){
			Token token=tokens.get(i);
			if(token.image.equals("(")) {
				int openParan1=i;
				int closeParan1=skipToCloseParan(tokens, openParan1+1);

				// skip if there are no parameters
				int semicolon=skipTo(";", tokens, i+1);
				
				//did we find what we were looking for?
				if(openParan1>=semicolon || closeParan1 >= tokens.size()) {
					break;
				}

				//if the open and close parentheses are on different lines
				if(tokens.get(closeParan1).beginLine > tokens.get(openParan1).beginLine ){
					adjustLineIndentValue(results,tokens.get(openParan1).beginLine+1, +1);
					//move the line with close parenthesis only if it is the only token on that line
					if(tokens.get(closeParan1-1).beginLine != tokens.get(closeParan1).beginLine){
						adjustLineIndentValue(results,tokens.get(closeParan1).beginLine, -1);
					}
					else{
						//otherwise, just adjust the following line
						adjustLineIndentValue(results,tokens.get(closeParan1).beginLine+1, -1);
					}
				}
				i=closeParan1;
			}
			switch(token.kind){
			case VhdlParserCoreTokenManager.CASE:
				adjustLineIndentValue(results,token.beginLine+1, +2);
				incase++;
				i = skipTo("is", tokens,i+1);
				break;
			case VhdlParserCoreTokenManager.LIBRARY:
				i = skipTo(";", tokens, i + 1);
				if (m_IndentLibrary) {
					// skip to eos
					use = skipTo("use", tokens, i + 1);
					EOS = skipTo(";", tokens, i + 1);
					if (use == tokens.size() || EOS == tokens.size()) {
						break;
					}
					if (use < EOS) {
						adjustLineIndentValue(results,tokens.get(use).beginLine, +1);
					}
					while (use < EOS) {
						i = EOS;
						use = skipTo("use", tokens, i + 1);
						EOS = skipTo(";", tokens, i + 1);
					}
					adjustLineIndentValue(results, tokens.get(i).beginLine + 1,	-1);
				}
				break;
			case VhdlParserCoreTokenManager.TYPE:
				EOS=skipTo(";", tokens, i+1);
				int record=skipTo("record", tokens, i+1);
				//if a record was found before EOF, then indent
				if(EOS > record){
					i=record;
					adjustLineIndentValue(results,tokens.get(record).beginLine+1, +1);
				} else {
					int semicolon=skipTo(";", tokens, i+1);
					int isToken=skipTo("is", tokens, i+1);
					if(isToken<semicolon) {
						//breaks the "indent after 'is'" rule. 
						i=isToken;
					}
				}
				break;
			case VhdlParserCoreTokenManager.ATTRIBUTE:
			case VhdlParserCoreTokenManager.ALIAS:
			case VhdlParserCoreTokenManager.FILE:
			case VhdlParserCoreTokenManager.SUBTYPE:
				//These tokens break the "indent after 'is'" rule. If
				//One is encountered, just skip to EOS
				i=skipTo(";", tokens, i+1);
				break;
			case VhdlParserCoreTokenManager.COMPONENT:
				// component can be defined without an "is"
				int isTokenPos=skipTo("is", tokens, i+1);
				EOS = skipTo(";", tokens, i+1);
				if(isTokenPos > EOS) {
					results.put(token.beginLine+1, +1);
				}
				break;
			case VhdlParserCoreTokenManager.PROCESS:
				// process can be defined without an "is"
				if(tokens.get(i+1).image.equals("(")) {
					// process has sensitivity list, indent after ")"
					int closeParan = skipToCloseParan(tokens, i+2);
					if(!tokens.get(closeParan+1).image.equals("is")) {
						adjustLineIndentValue(results,tokens.get(closeParan).beginLine+1, +1);
					}
				} else {
					// no sensitivity list indent next line:
					if(!tokens.get(i+1).image.equals("is")) {
						adjustLineIndentValue(results,token.beginLine+1, +1);
					}
				}
				break;
			case VhdlParserCoreTokenManager.BLOCK:
			case VhdlParserCoreTokenManager.GENERATE:
			case VhdlParserCoreTokenManager.IS:
			case VhdlParserCoreTokenManager.LOOP:
			case VhdlParserCoreTokenManager.THEN:
				adjustLineIndentValue(results,token.beginLine+1, +1);
				break;			
			case VhdlParserCoreTokenManager.ELSIF:
				adjustLineIndentValue(results,token.beginLine, -1);
				break;
			case VhdlParserCoreTokenManager.BEGIN:
			case VhdlParserCoreTokenManager.ELSE:
				if (token.beginLine != tokens.get(i-1).beginLine) {
					adjustLineIndentValue(results,token.beginLine, -1);
					adjustLineIndentValue(results,token.beginLine+1 , +1);
				}
				break;
			case VhdlParserCoreTokenManager.END:
				if(i<tokens.size()-1 && tokens.get(i+1).kind==VhdlParserCoreTokenManager.CASE) {
					adjustLineIndentValue(results,token.beginLine, -2);
					if(incase>0) incase--;
				} else {
					if (token.beginLine == tokens.get(i-1).beginLine) {
						// end is not the first token on this line
						adjustLineIndentValue(results, token.beginLine + 1, -1);
					} else {
						adjustLineIndentValue(results, token.beginLine, -1);
					}
				}
				//skip to eos
				i=skipTo(";", tokens, i+1);				
				break;
			case VhdlParserCoreTokenManager.WITH:
				int startToken=i;				
				i=skipTo(";", tokens, i+1);
				//if the with and the semicolon are on different lines
				//indent the lines between;
				if(tokens.get(i).beginLine > tokens.get(startToken).beginLine){
					results.put(tokens.get(startToken).beginLine+1, +1);
					results.put(tokens.get(i).beginLine+1, -1);
				}
				break;
			case VhdlParserCoreTokenManager.PROCEDURE:
			case VhdlParserCoreTokenManager.FUNCTION:
			case VhdlParserCoreTokenManager.PORT:
			case VhdlParserCoreTokenManager.GENERIC:
			case VhdlParserCoreTokenManager.SIGNAL:
			case VhdlParserCoreTokenManager.CONSTANT:
			case VhdlParserCoreTokenManager.VARIABLE:
				adjustLineIndentValue(results,tokens.get(i).beginLine, 0);
				// code has been moved to "("
				break;
			case VhdlParserCoreTokenManager.WHEN:
				if(incase>0) {
					adjustLineIndentValue(results,token.beginLine, -1);
					adjustLineIndentValue(results,token.beginLine+1, +1);
				} else {
					// ... <= ... when ... else ...;
					i=skipTo(";", tokens, i+1);
				}
				break;
			default:				
				break;
			}
		}
		return results;
	}
	/**
	 * Fixes the line indentation for the given text block
	 * @param text text block to examine
	 * @return properly indented string
	 */
	private String fixIndentation(String text){
		//get a list of lines
		String[] lines=text.split("\n");
		//if there is only 1 line, bail
		if(lines.length < 2){
			return text;
		}
		ArrayList<Token> tokens=TokenizeText(text);
		HashMap<Integer,Integer> lineIndentation=computeIndentation(tokens);

		//adjust the lines
		String indentString=getIndentString();		
		String currentIndent=getLineIndent(lines[0]);
		
		if( m_KeywordsLowercase) {
			putToLowercase(lines,tokens);
		}
		
		for(int lineNum=0; lineNum < lines.length; lineNum++){			
			//adjust the indentation. Need to add 1 since the token lines are zero based			
			if(lineIndentation.containsKey(lineNum+1)){
				int indentValue=lineIndentation.get(lineNum+1);
				if(indentValue > 0){
					for(int i=0;i<indentValue;i++){
						currentIndent+=indentString;
					}
				}
				else if (indentValue < 0){
					for(int i=indentValue;i<0;i++){
						//back up by one indent
						int endLocation=currentIndent.length()-indentString.length();
						if(endLocation >= 0){
							currentIndent=currentIndent.substring(0, endLocation);
						}
					}									
				}
			}
			
			String prevline = lineNum>0?lines[lineNum-1]:"";
			// trim comment at the end of the line:
			int indexcomment=prevline.indexOf("--");
			if(indexcomment>=0) prevline = prevline.substring(0,indexcomment).trim();
			
			// sometimes statements are written over multiple lines
			// in that case, only adjust the furst line.			
			boolean linecontinuation =
				!lineIndentation.containsKey(lineNum+1) &&//lineIndentation starts counting at 0!
				!prevline.endsWith(",") &&
				!prevline.endsWith(";") &&
				prevline.trim().length()!=0;
			if(!lines[lineNum].trim().startsWith("--") && !linecontinuation)
				lines[lineNum]=currentIndent+lines[lineNum].trim();
			else {
				// remove <CR>
				lines[lineNum] = lines[lineNum].replaceAll("\r", "");
			}

		}
		if (m_AlignOnColon) {
			align(lines, ":", lineIndentation);
		}
		if (m_AlignOnArrowRight) {
			align(lines, "=>", lineIndentation);
		}
		if (m_AlignOnArrowLeft) {
			align(lines, "<=", lineIndentation);
		}

		//reassemble the lines
		StringBuffer buffer=new StringBuffer();
		for(int lineNum=0; lineNum < lines.length; lineNum++){
			if (lines[lineNum].trim().length() != 0)
			buffer.append(lines[lineNum]);
			buffer.append(m_eol);
		}
		return buffer.toString();
	}
	
	/**
	 * Converts the tabs to spaces 
	 * @param text Text block to examine
	 * @return Converted text
	 */
	private String convertTabs(String text){
		String results=text;
		if(m_UseSpaceForTab){
			String indentString=getIndentString();
			results=text.replaceAll("\t", indentString);
		}
		return results;
	}
	
	/**
	 * Adds a space location to a list of lines
	 * @param list
	 * @param line
	 * @param col
	 */
	private void addSpace(HashMap<Integer,ArrayList<Integer>> list,int line,int col){
		ArrayList<Integer> intList;
		if(list.containsKey(line)){
			intList=list.get(line);			
		}
		else{
			intList=new ArrayList<Integer>();			
			list.put(line, intList);
		}
		intList.add(col);
	}
	/**
	 * Adds padding for spaces around the necessary elements
	 * @param text Text block to examine
	 * @return Padded text
	 */
	private String addSpacePadding(String text) {
		
		if(!m_PadOperators){
			return text;
		}
		// get a list of lines
		String[] lines = text.split("\n");
		ArrayList<Token> tokens=TokenizeText(text);
		HashMap<Integer,ArrayList<Integer>> spaceLocations=new HashMap<Integer,ArrayList<Integer>>();		
		for(int i=0;i<tokens.size();i++){
			Token token=tokens.get(i);
			switch (token.kind) {			
			case VhdlParserCoreTokenManager.CONCAT:
			case VhdlParserCoreTokenManager.EQ:
			case VhdlParserCoreTokenManager.NEQ:
			case VhdlParserCoreTokenManager.GE:
			case VhdlParserCoreTokenManager.LE:
			case VhdlParserCoreTokenManager.GT:
			case VhdlParserCoreTokenManager.LO:
			case VhdlParserCoreTokenManager.COLON:
			case VhdlParserCoreTokenManager.ASSIGN:
			case VhdlParserCoreTokenManager.RARROW:
				//remember, columns and lines are 1 based
				addSpace(spaceLocations,token.beginLine-1,token.beginColumn-1);
				addSpace(spaceLocations,token.beginLine-1,token.endColumn);
				break;
			case VhdlParserCoreTokenManager.SEMICOLON:
				//remember, columns and lines are 1 based
				addSpace(spaceLocations,token.beginLine-1,token.endColumn);
				break;
			default:
				break;
			}
			
		}
		// reassemble the lines
		StringBuffer buffer = new StringBuffer();
		for (int lineNum = 0; lineNum < lines.length; lineNum++) {
			String line;
			//do we need to fix this line?
			if(spaceLocations.containsKey(lineNum)){
				ArrayList<Integer> spaces=spaceLocations.get(lineNum);
				int spaceIdx=0;
				StringBuffer buff=new StringBuffer();
				//copy the line and add space when necessary
				for(int i=0;i<lines[lineNum].length();i++){
					if(spaceIdx < spaces.size() && i == spaces.get(spaceIdx)){
						spaceIdx++;
						if(Character.isWhitespace(lines[lineNum].charAt(i-1))==false &&
						   Character.isWhitespace(lines[lineNum].charAt(i))==false){
							buff.append(" ");
						}
					}
					buff.append(lines[lineNum].charAt(i));					
				}
				line=buff.toString();
			}
			else{
				line=lines[lineNum];
			}
			// remove <CR>
			line = line.replaceAll("\r", "");
			buffer.append(line);			
			buffer.append(m_eol);
		}
		return buffer.toString();
	}
	
	/**
	 * A derived class to override the tab size
	 *
	 */
	private class CharStream extends SimpleCharStream{

		public CharStream(java.io.Reader dstream) {
			super(dstream);			
		}		
		public void setTabSize(int i){
			super.setTabSize(i);
		}		
	}
	/**
	 * Tokenizes the given text block using VHDL rules
	 * 
	 * @param text
	 * @return A list of tokens in the given text
	 */
	private ArrayList<Token> TokenizeText(String text){
		StringReader stringReader=new StringReader(text);
		CharStream stream=new CharStream(stringReader);
		stream.setTabSize(1); //set the tab size to 1 in order to match string index
		VhdlParserCoreTokenManager tokenManager=new VhdlParserCoreTokenManager(stream);
		Token token=null;
		ArrayList <Token> results=new ArrayList<Token>();
		
		do{
			token=tokenManager.getNextToken();
			if(token.kind != VhdlParserCoreTokenManager.EOF){
				results.add(token);
			}
		}while(token.kind != VhdlParserCoreTokenManager.EOF);
		
		return results;
	}
	
	/**
	 * Returns the string that is to be used for indenting
	 * @return
	 */
	private String getIndentString(){
		String results="";
		if(m_UseSpaceForTab){			
			for(int i=0;i<m_IndentSize;i++){
				results+=" ";
			}			
		}
		else{
			results="\t";
		}
		return results;
	}
	
	/**
	 * Returns the indent string used before the first non white character
	 * @param line line to be examined
	 * @return
	 */
	private String getLineIndent(String line){
		//find the indent of the first line
		int firstNonSpace=0;
		for(firstNonSpace=0;firstNonSpace < line.length();firstNonSpace++){
			if(Character.isWhitespace(line.charAt(firstNonSpace))==false){
				break;
			}
		}
		return line.substring(0, firstNonSpace);
	}

	/**
	 * align for symbol like "=>"":"
	 * 
	 * @param String
	 *            [] lines
	 * @param String
	 *            regex
	 * @return Converted text
	 */
	private void align(String[] lines, String regex, HashMap<Integer,Integer> lineIndentation) {

		for (int lineNum = 0; lineNum < lines.length; lineNum++) {

			int index = indexOfAlignSymbol(lines, regex, lineNum);
			if (index == -1)
				continue;
			int startLine = lineNum;
			int stopLine = lineNum;
			int maxIndexassign = 0;
			while (index != -1 && stopLine+1 < lines.length) {
				if (index > maxIndexassign)
					maxIndexassign = index;
				stopLine++;
				index = indexOfAlignSymbol(lines, regex, stopLine);
				if(lineIndentation.containsKey(stopLine+1)) {
					int indentValue=lineIndentation.get(stopLine+1);
					if(indentValue!=0) break;
				}
			}
			for (int LineNum2 = startLine; LineNum2 < stopLine; LineNum2++) {
				index = indexOfAlignSymbol(lines, regex, LineNum2);
				if(index==-1) {
					VerilogPlugin.println("Format: error on line "+LineNum2);
				} else {
					String substring3 = trimRight(lines[LineNum2].substring(0, index));
					String substring4 = lines[LineNum2].substring(index).trim();
					for (int i = 0; i < maxIndexassign - index + 1; i++) {
						if (!inString(lines[lineNum], regex)) {
							substring3 = substring3 + " ";
						}
					}
					lines[LineNum2] = "" + substring3 + substring4;
				}
			}

			lineNum = stopLine;

		}
	}

	/**
	 * get the index of the regex in particular
	 * 
	 * @param lines
	 * @param regex
	 * @param array
	 * @param lineNum
	 * @return indexAssign
	 */
	private int indexOfAlignSymbol(String[] lines, String regex, int lineNum) {
		if (!lines[lineNum].contains(regex))
			return -1;
		if (lines[lineNum].endsWith(regex))
			return -1;
		if (lines[lineNum].trim().startsWith("--"))
			return -1;
		if (!closebracket(lines[lineNum], regex))
			return -1;
		int indexAssign = lines[lineNum].indexOf(regex);
		if(indexAssign==-1) { // thought it contained regex????
			return -1;
		}
		int index1 = lines[lineNum].indexOf("--");
		if(index1>=0 && index1<indexAssign) return -1;
		int index2 = lines[lineNum].indexOf("=>");
		if(index2>=0 && index2<indexAssign) return -1;
		int index3 = lines[lineNum].indexOf("<=");
		if(index3>=0 && index3<indexAssign) return -1;
		int index4 = lines[lineNum].indexOf(":");
		if(index4>=0 && index4<indexAssign) return -1;
		
		String substring1 = lines[lineNum].substring(0, indexAssign);
		if (substring1.trim().length() == 0)
			return -1;

		String substring2 = lines[lineNum].substring(indexAssign + regex.length());
		String substring1_trimmed = trimRight(substring1);
		String substring2_trimmed = substring2.trim();
		lines[lineNum] = "" + substring1_trimmed + regex + substring2_trimmed;
		indexAssign = lines[lineNum].indexOf(regex);

		return indexAssign;
	}
	

	/**
	 * to judge if the regex is in the close bracket
	 * 
	 * @param stringc
	 * @param regex
	 * @return close bracket
	 */
	private boolean closebracket(String stringc, String regex) {
		boolean closebracket = true;

		if (stringc.contains(regex) && stringc.contains("(")) {
			int indexAssign0 = stringc.indexOf(regex);
			String substring0 = stringc.substring(0, indexAssign0);
			int leftbracket = -1;
			int rightbracket = -1;
			int c = 1;
			int d = 1;
			do {
				leftbracket++;
				c = substring0.indexOf("(");
				String substrin = substring0.substring(c + 1);
				substring0 = substrin;
			} while (c != -1);
			substring0 = stringc.substring(0, indexAssign0);
			do {
				rightbracket++;
				d = substring0.indexOf(")");
				String substri = substring0.substring(d + 1);
				substring0 = substri;

			} while (d != -1);
			if (leftbracket == rightbracket) {
				closebracket = true;
			} else
				closebracket = false;

		}
		return closebracket;
	}
	

	/**
	 * trim only at right side of the string
	 * 
	 * @param s
	 * @return String been trim
	 */
	public String trimRight(String s) {
		String _s = s;

		while (_s.length() > 0 && _s.charAt(_s.length() - 1) == ' ') {
			_s = _s.substring(0, _s.length() - 1);
		}

		return _s;
	}

	/**
	 * to see if the regex is within a string.if it is,no align.
	 * @param stringc
	 * @param regex
	 * @return if regex in a string or not 
	 */
	private boolean inString(String stringc, String regex) {
		boolean inString = false;
		if (stringc.contains(regex) && stringc.contains("\"")) {
			int indexAssign0 = stringc.indexOf(regex);
			String substring0 = stringc.substring(0, indexAssign0);
			int quotation = -1;
			int c = 1;
			do {
				quotation++;
				c = substring0.indexOf("\"");
				String substrin = substring0.substring(c + 1);
				substring0 = substrin;
			} while (c != -1);
			if (quotation % 2 == 1) {
				inString = true;
			} else {
				inString = false;
			}
		}
		return inString;
	}

	/**
	 * put tokens in lowercase
	 * @param lines
	 * @param tokens
	 */
	private void putToLowercase(String[] lines, ArrayList<Token> tokens) {

		for (int tokennr = 0; tokennr < tokens.size(); tokennr++) {
			Token token = tokens.get(tokennr);
			for (int s = 0; s < HdlScanner.vhdlWords.length; s++) {

				if (token.image.equalsIgnoreCase(HdlScanner.vhdlWords[s])) {

					token.image = token.image.toLowerCase();
					int l = token.beginLine - 1;
					String curline = lines[l];

					String sub1 = curline.substring(0, token.beginColumn - 1);
					if (token.endColumn == lines[l].length()) {
						;
						lines[l] = "" + sub1 + HdlScanner.vhdlWords[s];
					} else {
						String sub2 = curline.substring(token.endColumn);
						lines[l] = "" + sub1 + HdlScanner.vhdlWords[s] + sub2;
					}
				}
			}
		}
	}

}
