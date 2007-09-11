package net.sourceforge.veditor.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HdlParserException extends Exception {
	/** This should match (something) Number (something) number */
	static private final String TOKEN_MGR_ERROR_PATTERN=".*line ([0-9]*).*column ([0-9]*).*";
	private String m_Message;
	private int m_StartLine;
	private int m_Column;	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default constructor
	 */
	public HdlParserException(){
		m_Message="";
		m_StartLine=0;
	}
	
	/**
	 * Attempts to parse a token error message
	 * @Note: I hope the token message format does not chage
	 * @param msg The message to parse
	 */
	protected void parseTokenMessage(String msg){
		//Do the easy part
		m_Message=msg;
		//get the line from the message
		
		Pattern pattern = Pattern.compile(TOKEN_MGR_ERROR_PATTERN);
		Matcher m = pattern.matcher(msg);
		if (m.matches() && m.groupCount()>1)
		{
			String[] segments=new String[] {m.group(1),m.group(2)};
			m_StartLine=Integer.parseInt(segments[0]);
			m_Column=Integer.parseInt(segments[1]);
		}
		else{
			m_StartLine=0;
			m_Column=0;
		}
	}
	
	public HdlParserException(int beginLine,int beginColumn, String message){
		m_Message=message;
		m_StartLine=beginLine;
		m_Column=beginColumn;
	}
	/**
	 * Generic exception converter
	 * @param e
	 */
	public HdlParserException(Object e){
		if (e instanceof net.sourceforge.veditor.parser.vhdl.ParseException){
			net.sourceforge.veditor.parser.vhdl.ParseException parseException=
				(net.sourceforge.veditor.parser.vhdl.ParseException)e;
			m_Message=parseException.getMessage();
			m_StartLine=parseException.currentToken.beginLine;
			m_Column=parseException.currentToken.beginColumn;
		}
		else if (e instanceof net.sourceforge.veditor.parser.verilog.ParseException){
			net.sourceforge.veditor.parser.verilog.ParseException parseException=
				(net.sourceforge.veditor.parser.verilog.ParseException)e;
			m_Message=parseException.getMessage();
			m_StartLine=parseException.currentToken.beginLine;
			m_Column=parseException.currentToken.beginColumn;
		}
		else if (e instanceof net.sourceforge.veditor.parser.verilog.TokenMgrError){
			net.sourceforge.veditor.parser.verilog.TokenMgrError tokenMgrError=
				(net.sourceforge.veditor.parser.verilog.TokenMgrError)e;
			parseTokenMessage(tokenMgrError.toString());			
		}
		else if (e instanceof net.sourceforge.veditor.parser.vhdl.TokenMgrError){
			net.sourceforge.veditor.parser.vhdl.TokenMgrError tokenMgrError=
				(net.sourceforge.veditor.parser.vhdl.TokenMgrError)e;
			parseTokenMessage(tokenMgrError.toString());			
		}
		else{
			m_Message="";
			m_StartLine=0;
			m_Column=0;
		}
	}
	
	public String getMessage(){
		return m_Message;
	}
	
	public int getStartLine(){
		return m_StartLine;
	}
	
	public int getColumn(){
		return m_Column;
	}
}
