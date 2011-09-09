package net.sourceforge.veditor.parser.vhdl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.ParserReader;
import net.sourceforge.veditor.preference.PreferenceStrings;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class VHDLParserThread extends VhdlParserCore implements Runnable {
	private IFile m_File;
	private ASTdesign_file result;
	private HdlParserException hdlParserException;
	
	public VHDLParserThread(ParserReader reader, IFile file) {
		super(reader);
		m_File = file;
		result = null;
		hdlParserException = null;
	}
	
	public void run() {
		boolean ignorefile = false;
		int file_lines=0;
		{
			InputStreamReader reader;
			try {
				reader = new InputStreamReader(m_File.getContents());
				BufferedReader breader = new BufferedReader(reader);
				
				try {
					while(true) {
						String line = breader.readLine();
						if(line==null) {
							break;
						}
						file_lines++;
						if(line.contains("-- turn off superfluous VHDL processor warnings") ||
						   line.contains("-- Skip Parsing")) {
							ignorefile=true;
						}
					}
				} catch(IOException e) {
					ignorefile=true;	
				}
			} catch (CoreException e1) {
				ignorefile=true;
			}
		}
		
		
		int nMaxFileLines;
		try{
			String s = VerilogPlugin.getPreferenceString(PreferenceStrings.MAX_PARSE_LINES);
			nMaxFileLines = Integer.parseInt(s);			
		}catch (NumberFormatException e) {
			nMaxFileLines =50000;
		}
		
		
		if(!ignorefile && file_lines < nMaxFileLines) {
			try {
				result=design_file();
			}
			catch (ParseException e){
				//convert the exception to a generic one			
				hdlParserException =new HdlParserException(e);
				errs.Error("Parser cannot recover from previous error(s).", hdlParserException);
			}
			catch (TokenMgrError e){			
				hdlParserException =new HdlParserException(e);
				errs.Error("Parser cannot recover from previous error(s).Unexpected character", hdlParserException);
			}
			catch (ThreadDeath e) {
				hdlParserException =new HdlParserException(e);
				errs.Error("Parser thread hanging...", hdlParserException);
			}
		}
	}
	
	public ASTdesign_file getResult() throws HdlParserException {
		if(hdlParserException!=null) throw hdlParserException;
		return result;
	}
}
