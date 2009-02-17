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

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

public class VhdlFormatAction extends AbstractAction {
	private boolean m_UseSpaceForTab;
	private int     m_IndentSize=0;
	private boolean m_PadOperators=true;
	
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
		
		selectedText=addSpacePadding(selectedText);
		selectedText=convertTabs(selectedText);		
		selectedText=fixIndentation(selectedText);				
		
		
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
		for(int i=0;i < tokens.size();i++){
			Token token=tokens.get(i);
			switch(token.kind){
			case VhdlParserCoreTokenManager.CASE:
				adjustLineIndentValue(results,token.beginLine+1, +2);
				incase++;
				i = skipTo("is", tokens,i+1);
				break;
			case VhdlParserCoreTokenManager.TYPE:
				int EOS=skipTo(";", tokens, i+1);
				int record=skipTo("record", tokens, i+1);
				//if a record was found before EOF, then indent
				if(EOS > record){
					i=record;
					adjustLineIndentValue(results,tokens.get(record).beginLine+1, +1);
				}
				else{
					i=EOS;
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
			case VhdlParserCoreTokenManager.BLOCK:
			case VhdlParserCoreTokenManager.PROCESS:
			case VhdlParserCoreTokenManager.GENERATE:
			case VhdlParserCoreTokenManager.IS:
			case VhdlParserCoreTokenManager.LOOP:
			case VhdlParserCoreTokenManager.THEN:
				results.put(token.beginLine+1, +1);
				break;			
			case VhdlParserCoreTokenManager.ELSIF:
				adjustLineIndentValue(results,token.beginLine, -1);
				break;
			case VhdlParserCoreTokenManager.BEGIN:
			case VhdlParserCoreTokenManager.ELSE:
				adjustLineIndentValue(results,token.beginLine, -1);
				results.put(token.beginLine+1, +1);
				break;
			case VhdlParserCoreTokenManager.END:
				if(i<tokens.size()-1 && tokens.get(i+1).kind==VhdlParserCoreTokenManager.CASE) {
					if(incase>0) incase--;
				}
				adjustLineIndentValue(results,token.beginLine, -1);
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
				adjustLineIndentValue(results,tokens.get(i).beginLine, 0);
				int openParan=skipTo("(", tokens, i+1);
				int closeParan=skipToCloseParan(tokens, openParan+1);
				//did we find what we were looking for?
				if(openParan==tokens.size() || closeParan==tokens.size()){
					break;
				}
				//if the open and close parentheses are on different lines
				if(tokens.get(closeParan).beginLine > tokens.get(openParan).beginLine ){					
					adjustLineIndentValue(results,tokens.get(openParan).beginLine+1, +1);
					//move the line with close parenthesis only if it is the only token on that line
					if(tokens.get(closeParan-1).beginLine != tokens.get(closeParan).beginLine){
						adjustLineIndentValue(results,tokens.get(closeParan).beginLine, -1);
					}
					else{
						//otherwise, just adjust the following line
						adjustLineIndentValue(results,tokens.get(closeParan).beginLine+1, -1);
					}
				}
				i=closeParan;
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
		}
		//reassemble the lines
		StringBuffer buffer=new StringBuffer();
		for(int lineNum=0; lineNum < lines.length; lineNum++){
			buffer.append(lines[lineNum]);
			//if not the last line
			if(lineNum < lines.length-1){
				buffer.append("\n");
			}			
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
			buffer.append(line);			
			// if not the last line
			if (lineNum < lines.length - 1) {
				buffer.append("\n");
			}
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

}
