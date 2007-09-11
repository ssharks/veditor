/**
 * 
 * This file is based on the VHDL parser originally developed by
 * (c) 1997 Christoph Grimm,
 * J.W. Goethe-University Frankfurt
 * Department for computer engineering
 *
 **/
package net.sourceforge.veditor.parser.vhdl;

import java.util.ArrayList;

/**
 * (c) 1997 Christoph Grimm, J. W. Goethe-University.
 * Beginning of an error handler, should organize different
 * error messages, depending on a chosen level of error reporting. 
 * Still very simple.
 */

class ErrorHandler {
	private VhdlParserCore m_Parser;
	private ArrayList<Error> m_Warnings = new ArrayList<Error>();
	private ArrayList<Error> m_Errors = new ArrayList<Error>();

	public ErrorHandler(VhdlParserCore parser) {
		m_Parser = parser;
	}

	/**
	 * Print a warning, that a construct is not supported in SIWG Level 1,
	 * if this is switched on by command line.
	 */
	void WarnLevel1(String w) {
		Token t = m_Parser.getToken(0);
		Error error = new Error(
				"line (" + t.beginLine + "): " + w,
				t.beginLine,
				null);		
		m_Warnings.add(error);
	}

	/**
	 * Print a warning, that a semantic/syntactic error has occurred.
	 */
	void Error(String w,Exception e) {
		Token t = m_Parser.getToken(0);
		Error error = new Error(
						"line (" + t.beginLine + "): " + w,
						t.beginLine,
						e);
		m_Errors.add(error);
	}

	/**
	 * Print a summary, consisting of all numbers of errors and warnings
	 * detected.
	 */
	void Summary() {
		if(getErrors().length > 0){
			System.err.println("Errors:");
		}
		for (Error error : getErrors()) {
			System.err.println(error.m_Message);
		}
		if(getWarnings().length > 0){
			System.err.println("Warnings:");
		}
		for (Error error : getWarnings()) {
			System.err.println(error.m_Message);
		}
	}

	public Error[] getErrors() {
		return m_Errors.toArray(new Error[0]);
	}

	public Error[] getWarnings() {
		return m_Warnings.toArray(new Error[0]);
	}

	/**
	 * Class used to hold information about the message
	 * @author gho18481
	 *
	 */
	public class Error {
		private String m_Message;
		private int m_Line;
		private Exception m_Exception;
		
		public Error(String msg,int line,Exception e){
			m_Message=msg;
			m_Line=line;
			m_Exception=e;
		}
		public String getMessage(){
			StringBuffer results=new StringBuffer(m_Message);
			
			if(m_Exception!=null){
				results.append("\nPossible Cause:\n");
				String exceptionMesg=m_Exception.toString();
				//clean up the message
				results.append(exceptionMesg.substring(exceptionMesg.indexOf(':')+1));				
			}
			
			return results.toString();
		}
		public int getLine(){return m_Line;}
		public Exception getException(){return m_Exception;}
	}
}
