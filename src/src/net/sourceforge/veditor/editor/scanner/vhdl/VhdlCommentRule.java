/*******************************************************************************
 * Copyright (c) 2007 Ali Ghorashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali Ghorashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.editor.scanner.vhdl;


import net.sourceforge.veditor.editor.scanner.HdlPartitionScanner;
import net.sourceforge.veditor.parser.IParser;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


/**
 * Rule used to separate task tags embedded within comments
 * 
 * @author gho18481
 * 
 */
public class VhdlCommentRule implements IPredicateRule {
    private IToken successToken;
    private IToken lastCommentStyle = null;
    private int lastOffset=0;
    
    /** States to keep track of what we're doing */
    protected enum ProcessingState {NotInComment,TaskToken,ContinueComment}
    
    private ProcessingState state;
    

    public VhdlCommentRule(IToken token) {
        successToken = token;
        state=ProcessingState.NotInComment;        
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        // TODO Auto-generated method stub
        return Token.UNDEFINED;
    }

    @Override
    public IToken getSuccessToken() {
        return successToken;
    }
    /**
     * Gets a character from the scanner and prevents reading past the end
     * @param scanner
     * @return The read character or eof
     */
    private int getChar(VhdlPartitionScanner scanner){
        int c=scanner.read();
        if(c == VhdlPartitionScanner.EOF){
            scanner.unread();
        }
        return c;
    }
    /**
     * Checks to see if the passed string is a task token
     * @param str The string to check
     * @return True if the string is a task token. false otherwise
     */
    private boolean isTaskToken(String str){
        for(int i=0;i < IParser.taskCommentTokens.length;i++){
            if(IParser.taskCommentTokens[i].equals(str)){
                return true;
            }            
        }  
        return false;
    }
    /**
     * Consumes characters out of the scanner until the next token
     * 
     * @param scanner
     *            Character scanner to use
     * @return true if the line ended without encountering a task token       
     */
    private boolean consume(VhdlPartitionScanner scanner) {
        int c;
        StringBuffer strBuffer=new StringBuffer();
        do{
            c=getChar(scanner);            
            //if we hit a whitespace
            if(Character.isWhitespace(c) || c == ICharacterScanner.EOF){                
                String word=strBuffer.toString();
                strBuffer.delete(0, strBuffer.length());
                //do we have a task token?
                if(isTaskToken(word)){
                    //back up to the beginning of the token
                    for(int i=0;i<word.length();i++){
                        scanner.unread();
                    }
                  //put back the white space we read at after the word
                    if(Character.isWhitespace(c)){
                        scanner.unread();
                    }
                    return false;
                }
            }
            else{
                strBuffer.append((char)c);
            }
            
           //if we hit the end of line before seeing any task tokens
           if(c == '\n'  || c == ICharacterScanner.EOF){              
                return true;                
            }
        }while(true);       
    }
    
    /**
     * Gets a word from the canner
     * @param scanner The word (separated by white spaces)
     * @return e if the end of scanner was reached
     */
    private String getWord(VhdlPartitionScanner scanner){
        StringBuffer strBuffer=new StringBuffer();
        int c;
        do{
            c=getChar(scanner);            
            //if we hit a whitespace
            if(Character.isWhitespace(c) || c == ICharacterScanner.EOF){                
                return strBuffer.toString();
            }
            strBuffer.append((char)c);
        }while(true);
    }

    /**
     * Consumes characters out of the scanner until the end of the line or
     * EOF
     * 
     * @param scanner
     *            Character scanner to use
     * @return The string that was consumed before reaching the end of line           
     */
    private String consumeToEOL(VhdlPartitionScanner scanner) {
        StringBuffer strBuffer=new StringBuffer();
        do {
            int c = getChar(scanner);
            if (c == ICharacterScanner.EOF || c == '\n') {               
                break;
            }
            strBuffer.append((char)c);
        } while (true);
        
        return strBuffer.toString().trim();
    }

    /**
     * Checks the scanner to see if it is a comment
     * @param scanner
     * @return Comment,Doxygen Comment or Undefined if not in either
     */
    private IToken getCommentType(ICharacterScanner scanner){
        //Note these reads do not need to check for EOF since
        //they always do an unread if the match fails
        if (scanner.read() != '-') {
            scanner.unread();
            return Token.UNDEFINED;
        }
        if (scanner.read() != '-') {
            scanner.unread();
            scanner.unread();
            return Token.UNDEFINED;
        }
        // if we get here we have two dashes in a row       
        // do we have a doxygen comment?
        if (scanner.read() == '!') {
            return HdlPartitionScanner.doxygenCommentToken;
        } else {
            // if not put the character back
            scanner.unread();            
        }
        return HdlPartitionScanner.singleLineCommentToken;
    }
    
    @Override
    public IToken evaluate(ICharacterScanner charScanner) {
        IToken results = Token.UNDEFINED;
        VhdlPartitionScanner scanner= (VhdlPartitionScanner)charScanner;
        //If we have restarted the scanner (i.e we are not were we left off)
        if(scanner.getOffset() != lastOffset){
            state=ProcessingState.NotInComment;
        }
        switch(state){        
        case NotInComment:
            results=getCommentType(scanner);
            lastCommentStyle=results;
            //if we are in a comment
            if(results != Token.UNDEFINED){
                if(consume(scanner) == true){
                    //no task token found
                    state=ProcessingState.NotInComment;
                    //System.out.println("Going to Not In Comment");
                }
                else{
                    state=ProcessingState.TaskToken;
                    //System.out.println("Going to TaskToken");
                }                       
            }          
            break;
        case TaskToken:
            String word=getWord(scanner);
            if(isTaskToken(word) == false){
                //error
                System.out.println("Error encountered getting task token");
                state=ProcessingState.NotInComment;
                //System.out.println("Going to Not In Comment");
                results=Token.UNDEFINED;
                //back up to the beginning of the token
                for(int i=0;i<word.length();i++){
                    scanner.unread();
                }
                //put back the white space we read at after the word
                scanner.unread();
            }
            else{
                state=ProcessingState.ContinueComment;
                //System.out.println("Going to ContinueComment");
                results=HdlPartitionScanner.taskTagToken;
            }
            break;
        case ContinueComment:
            results=lastCommentStyle;
            consumeToEOL(scanner);
            state=ProcessingState.NotInComment;
            //System.out.println("Going to Not In Comment");
            break;
        default:
            return Token.UNDEFINED;            
        }
        
        lastOffset = scanner.getOffset();
        return results;
    }   
   
}
