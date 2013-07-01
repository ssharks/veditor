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
import net.sourceforge.veditor.editor.scanner.HdlScanner;
import net.sourceforge.veditor.parser.vhdl.SimpleCharStream;
import net.sourceforge.veditor.parser.vhdl.Token;
import net.sourceforge.veditor.parser.vhdl.VhdlParserCoreTokenManager;
import net.sourceforge.veditor.preference.PreferenceStrings;

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
	private boolean	m_AlignOnAssignment	= true;
	private boolean	m_AlignOnComment	= true;
	private boolean	m_AlignInOut		= true;
	private String m_eol;
	
	/**
	 * Class used to store stop and start points for 
	 * fixing indents
	 */
	private class StartStop{
		public int start;
		public int indentAmount;
	}
	
	public VhdlFormatAction(){
		super("VhdlFormatAction");
	}

	@Override
	public void run() {		
		StyledText widget = getViewer().getTextWidget();
		//mg
		final int topLineIndex = widget.getTopIndex();
		//mg------------------------

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
		// adjust the case of key words
		if(m_KeywordsLowercase){
			selectedText=fixCase(selectedText);
		}
		// align on colons
		if (m_AlignOnColon) {
			selectedText=alignBefore(selectedText,new int[]{VhdlParserCoreTokenManager.COLON});
		}
		// do we need to align after direction assignments
		if ( m_AlignInOut ) {			
			selectedText=alignAfter(selectedText, 
					new int[]{ VhdlParserCoreTokenManager.IN, 
				               VhdlParserCoreTokenManager.OUT,
				               VhdlParserCoreTokenManager.INOUT,
				               VhdlParserCoreTokenManager.BUFFER	
					          }
			        );
		}
		if (m_AlignOnArrowRight) {
			selectedText=alignBefore(selectedText,new int[]{VhdlParserCoreTokenManager.RARROW});
		}
		if (m_AlignOnArrowLeft) {
			selectedText=alignBefore(selectedText,new int[]{VhdlParserCoreTokenManager.LE});
		}
		if ( m_AlignOnAssignment ) {
			selectedText=alignBefore(selectedText,new int[]{VhdlParserCoreTokenManager.ASSIGN});
		}		
		if ( m_AlignOnComment ) {
			selectedText=alignBefore(selectedText,new int[]{VhdlParserCoreTokenManager.COMMENT});
		}
		
		
		//replace the text
		widget.replaceTextRange(begin, end - begin, selectedText);
		
		//set caret and the part shown in the editor
		//mg
		if ( topLineIndex <= widget.getLineCount() ) {
			widget.setCaretOffset( widget.getOffsetAtLine( topLineIndex ) );
			widget.setTopIndex( topLineIndex );
		}
		//mg-------------------------------
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
		
		m_PadOperators=VerilogPlugin.getPreferenceBoolean(PreferenceStrings.PAD_OPERATORS);
		m_IndentLibrary=VerilogPlugin.getPreferenceBoolean(PreferenceStrings.INDENT_LIBRARY);
		m_KeywordsLowercase = VerilogPlugin.getPreferenceBoolean(PreferenceStrings.KEYWORDS_LOWERCASE);
		m_AlignOnColon = VerilogPlugin.getPreferenceBoolean(PreferenceStrings.ALIGNONCOLON);
		m_AlignOnArrowRight = VerilogPlugin	.getPreferenceBoolean(PreferenceStrings.ALIGNONARROWRIGHT);
		m_AlignOnArrowLeft = VerilogPlugin	.getPreferenceBoolean(PreferenceStrings.ALIGNONARROWLEFT);
		m_AlignOnAssignment = VerilogPlugin.getPreferenceBoolean( PreferenceStrings.ALIGNONASSIGNMENT );
		m_AlignOnComment = VerilogPlugin.getPreferenceBoolean( PreferenceStrings.ALIGNONCOMMENT );
		m_AlignInOut = VerilogPlugin.getPreferenceBoolean( PreferenceStrings.ALIGNINOUT );
		
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
	 * Scans the token list for the next occurrence of the given image 
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
		
		// 'when' has another meaning when it is used inside or outside a case
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
		String[] lines = text.split("\n");
		//if there is only 1 line, bail
		if(lines.length < 2){
			return text;
		}
		ArrayList<Token> tokens=TokenizeText(text);
		HashMap<Integer,Integer> lineIndentation=computeIndentation(tokens);

		//adjust the lines
		String indentString=getIndentString();		
		String currentIndent=getLineIndent(lines[0]);
		
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
			// in that case, only adjust the first line.			
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

		StringBuffer buffer=new StringBuffer();
		for(int lineNum=0; lineNum < lines.length; lineNum++){
			if (lines[lineNum].trim().length() != 0){
				buffer.append(lines[lineNum]);
			}
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
	 * Create a string made up of the repetition of str
	 * @param str The String to repeat
	 * @param repeat Number of times to repeat
	 * @return new String
	 */
	private String fillString(String str,int repeat){
		StringBuffer results=new StringBuffer();
		for(int i=0; i < repeat; i++){
			results.append(str);
		}
		return results.toString();
	}
	
	/**
	 * Checks to see if the passed token is on list of tokens
	 * @return true if the token is on the list, false if not
	 */
	private boolean isTokenOnList(int tokenId, int valid_tokens[]){
		for(int i: valid_tokens){
			if (tokenId == i){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Aligns the text before one or more tokens
	 * @param text
	 * @param valid_tokens An array of tokens to align on
	 * @return String after alignment is done
	 * @note The alignment value is only valid for a consecutive set of lines. If
	 * a line without a token is encountered, the alignment value gets reset
	 */
	private String alignBefore(String text,int valid_tokens[]) {
		// list of lines that need to be indented and the location of indent
		HashMap<Integer,StartStop> indentSet = new HashMap<Integer,StartStop>();		
		//grouping of indent level
		ArrayList <HashMap<Integer,StartStop>> indentGroups = new ArrayList <HashMap<Integer,StartStop>>() ;	
		ArrayList<Token> tokens=TokenizeText(text);
		String[] lines=text.split(m_eol);
		//if there is only 1 line, bail
		if(lines.length < 2){
			return text;
		}
		
		//find the indent position following the direction directive
		int maxPosAfterDirection=0;
		int lastLineWithToken=0;		
		for(int i=0;i < tokens.size();i++){
			Token token=tokens.get(i);
			if(isTokenOnList(token.kind,valid_tokens) ){		
				lastLineWithToken =  token.beginLine;
				//record the info about the tokens
				StartStop indentInfo = new StartStop();
				indentInfo.start = token.beginColumn;				
				//token lines are one based
				indentSet.put(token.beginLine -1, indentInfo);
				//record the max indentation after the directive
				if(tokens.get(i).beginColumn  > maxPosAfterDirection){
					maxPosAfterDirection=tokens.get(i).beginColumn;
				}				
				
			}
			else{
				//token not found
				if (  token.beginLine > (lastLineWithToken+1) && lastLineWithToken!=0){
					// we encounter a whole line without a valid token 
					for( Integer line: indentSet.keySet()){
						//set all the indent values to max encountered in this block
						indentSet.get(line).indentAmount = maxPosAfterDirection;					
					}
					//file away the current group
					indentGroups.add(indentSet);
					//make a new group
					indentSet = new HashMap<Integer,StartStop>();				
					maxPosAfterDirection=0;
					lastLineWithToken   =0;
				}
			}
		}	
		//add the last alignment set to the list
		for( Integer line: indentSet.keySet()){
			//set all the indent values to max encountered in this block
			indentSet.get(line).indentAmount = maxPosAfterDirection;					
		}
		indentGroups.add(indentSet);
		
		
		for(HashMap<Integer,StartStop> indentInfoSet : indentGroups){
			//adjust alignment
			for( Integer line: indentInfoSet.keySet()){
				StartStop indentInfo=indentInfoSet.get(line);
				String indentString = fillString(" ", indentInfo.indentAmount - indentInfo.start+1);
				String str1 =  lines[line].substring(0,indentInfo.start-2);
				String str2 =  lines[line].substring(indentInfo.start-1);
				lines[line] = str1 + indentString + str2;
			}
		}
		
		//reassemble the lines
		StringBuffer buffer=new StringBuffer();
		for(int lineNum=0; lineNum < lines.length; lineNum++){
			//skip the lines that consist only of white space 
			if (lines[lineNum].trim().length() != 0){
				buffer.append(lines[lineNum]);				
			}
			//do not append a new line to the last line
			if(lineNum +1 !=  lines.length ){
				buffer.append(m_eol);
			}
		}
		return buffer.toString(); 
	}

	/**
	 * Aligns the text after one or more tokens
	 * @param text
	 * @param valid_tokens An array of tokens to align on
	 * @return String after alignment is done
	 * @note The alignment value is only valid for a consecutive set of lines. If
	 * a line without a token is encountered, the alignment value gets reset
	 */
	private String alignAfter(String text,int valid_tokens[]) {
		// list of lines that need to be indented and the location of indent
		HashMap<Integer,StartStop> indentSet = new HashMap<Integer,StartStop>();
		//grouping of indent level
		ArrayList <HashMap<Integer,StartStop>> indentGroups = new ArrayList <HashMap<Integer,StartStop>>() ;		
		ArrayList<Token> tokens=TokenizeText(text);
		String[] lines=text.split(m_eol);
		//if there is only 1 line, bail
		if(lines.length < 2){
			return text;
		}
		
		//find the indent position following the direction directive
		int maxPosAfterDirection=0;
		int lastLineWithToken=0;		
		for(int i=0;i < tokens.size();i++){
			Token token=tokens.get(i);					
			 
			if(isTokenOnList(token.kind,valid_tokens) ){				
				lastLineWithToken =  token.beginLine;
				//make sure there is another token on the same line
				if( (i+1) < tokens.size() && token.beginLine == tokens.get(i+1).beginLine){
					//record the info about the tokens
					StartStop indentInfo = new StartStop();				
					indentInfo.start  = tokens.get(i+1).beginColumn;
					//token lines are one based
					indentSet.put(token.beginLine -1, indentInfo);
					//record the max indentation after the directive
					if(tokens.get(i+1).beginColumn  > maxPosAfterDirection){
						maxPosAfterDirection=tokens.get(i+1).beginColumn;
					}					
				}
			}			
			else{
				//token not found
				if (  token.beginLine > (lastLineWithToken+1) && lastLineWithToken!=0){
					// we encounter a whole line without a valid token 
					for( Integer line: indentSet.keySet()){
						//set all the indent values to max encountered in this block
						indentSet.get(line).indentAmount = maxPosAfterDirection;					
					}
					//file away the current group
					indentGroups.add(indentSet);
					//make a new group
					indentSet = new HashMap<Integer,StartStop>();				
					maxPosAfterDirection=0;
					lastLineWithToken   =0;
				}
			}
		}	
		//add the last alignment set to the list
		for( Integer line: indentSet.keySet()){
			//set all the indent values to max encountered in this block
			indentSet.get(line).indentAmount = maxPosAfterDirection;					
		}
		indentGroups.add(indentSet);
		
		for(HashMap<Integer,StartStop> indentInfoSet : indentGroups){
			//adjust alignment
			for( Integer line: indentInfoSet.keySet()){
				StartStop indentInfo=indentInfoSet.get(line);
				String indentString = fillString(" ", indentInfo.indentAmount - indentInfo.start+1);
				String str1 =  lines[line].substring(0,indentInfo.start-2);
				String str2 =  lines[line].substring(indentInfo.start-1);
				lines[line] = str1 + indentString + str2;
			}
		}
		
		//reassemble the lines
		StringBuffer buffer=new StringBuffer();
		for(int lineNum=0; lineNum < lines.length; lineNum++){
			//skip the lines that consist only of white space 
			if (lines[lineNum].trim().length() != 0){
				buffer.append(lines[lineNum]);				
			}
			buffer.append(m_eol);
		}
		return buffer.toString(); 
	}
	
	/**
	 * Converts all the keywords in the passed text to lower case
	 * @param text Text to be formatted
	 */
	private String fixCase(String text) {
		ArrayList<Token> tokens=TokenizeText(text);
		String[] lines=text.split(m_eol);
		//if there is only 1 line, bail
		if(lines.length < 2){
			return text;
		}
		
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
		//reassemble the lines
		StringBuffer buffer=new StringBuffer();
		for(int lineNum=0; lineNum < lines.length; lineNum++){
			//skip the lines that consist only of white space 
			if (lines[lineNum].trim().length() != 0){
				buffer.append(lines[lineNum]);				
			}
			//do not append a new line to the last line
			if(lineNum +1 !=  lines.length ){
				buffer.append(m_eol);
			}
		}
		return buffer.toString(); 
	}

}
