/*******************************************************************************
 * Copyright (c) 2012 VEditor Team
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    KOBAYASHI Tadashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.parser.verilog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import net.sourceforge.veditor.parser.ParserReader;

public class VerilogParserReader extends ParserReader {
	private final static int CONTINUED = 0;
	private final static int ELSE = 1;
	private final static int ELSIF = 2;
	private final static int ENDIF = 3;

	private CharReader reader;
	private File directory;
	private StringBuilder buffer;
	private Map<String, String> defines;
	private boolean isInclude;

	public VerilogParserReader(InputStream in, IFile file) {
		reader = new CharReader(new InputStreamReader(in));
		process(file);
		reader.close();
		// System.out.println(buffer);
		initialize(buffer.toString());
	}

	public VerilogParserReader(String text, IFile file) {
		reader = new CharReader(new StringReader(text));
		process(file);
		reader.close();
		// System.out.println(buffer);
		initialize(buffer.toString());
	}

	private void process(IFile file) {
		directory = file.getLocation().removeLastSegments(1).toFile();
		buffer = new StringBuilder();
		defines = new HashMap<String, String>();
		isInclude = false;

		for (;;) {
			if (reader.isEof())
				break;
			// Normally it runs just once. It allows accidental `else or `endif
			parseRegion(true);
		}
	}
	
	private static final int CODE = 0;
	private static final int BLOCK_COMMENT = 1;
	private static final int LINE_COMMENT = 2;
	private static final int STRING = 3;
	private static final int PROTECTED = 4;
	
	private String lastCode = "";

	private int parseRegion(boolean enable) {
		int context = CODE;
		lastCode = "";

		for (;;) {
			if (reader.isEof()) {
				return CONTINUED;
			}
			
			char c = reader.read();
			lastCode += c;
			
			while (lastCode.length() > "pragma protect begin_protected".length()) {
				lastCode = lastCode.substring(1); // remove the first element from the string
			}
			char next;
			switch (c) {
			case '\n':
				if (context == LINE_COMMENT) {
					context = CODE;
				}
				break;
			case '"':
				if (context == CODE) {
					context = STRING;
				} else if (context == STRING) {
					context = CODE;
				}
				break;
			case '/':
				next = reader.read();
				if (next == '/' && context == CODE)
					context = LINE_COMMENT;
				else if (next == '*' && context == CODE)
					context = BLOCK_COMMENT;
				else
					reader.pushBack(next);
				break;
			// detect the start of fully encrypted Verilog files
			case '\u00e2' : 
				next = reader.read();
				if (next == '%' || next == '\u0013') {
					context = PROTECTED;
					
					// stop parsing any further, read to the end of the file
					reader.readForward();
					// stop parsing further
					return CONTINUED;
				} else {
					reader.pushBack(next);
				}
			case '*':
				next = reader.read();
				if (next == '/' && context == BLOCK_COMMENT) {
					context = CODE;
					c = ' ';
				} else {
					if (context != PROTECTED) {
						reader.pushBack(next);
					}
				}
				break;
			}
			if (c == '`' && context == CODE) {
				int status = directive(enable);
				if (status != CONTINUED) {
					// if get `else `endif, return
					return status;
				}
			} else {
				if (c == '\n') {
					if (isInclude)
						buffer.append(' '); // for keeping line number
					else
						buffer.append('\n');
				} else if (enable && context != BLOCK_COMMENT
						&& context != LINE_COMMENT && context != PROTECTED) {
					buffer.append(c);
				}
			}
			
			// search for the start marker
			if (lastCode.contains("protect begin_protected") && (context != PROTECTED)) {
				context = PROTECTED;
			}
			
			// search for then end marker
			if (lastCode.contains("protect end_protected") && (context == PROTECTED)) {
				buffer.append("//pragma protect end_protected");
				context = CODE;
			}
		}
	}

	private int directive(boolean enable) {
		String cmd = getIdent();

		String skipToEol[] = { "timescale", "default_nettype", "celldefine", "endcelldefine" };
		for (int i = 0; i < skipToEol.length; i++) {
			if (cmd.equals(skipToEol[i])) {
				getToEndOfLine();
				return CONTINUED;
			}
		}
		
		if (cmd.equals("define")) {
			String def = getNextIdent();
			int line = reader.getLine();
			skipSpace();
			String value;
			if (line == reader.getLine()) {
				value = getToEndOfLine();
				int idx = value.indexOf("//");
				if (idx >= 0)
					value = value.substring(0, idx);
			} else {
				value = "";
			}
			if (enable)
				defines.put(def, value);
		} else if (cmd.equals("undef")) {
			String def = getNextIdent();
			if (enable)
				defines.remove(def);
		} else if (cmd.equals("ifdef")) {
			directiveIf(enable, true);
		} else if (cmd.equals("ifndef")) {
			directiveIf(enable, false);
		} else if (cmd.equals("else")) {
			return ELSE;
		} else if (cmd.equals("elsif")) {
			// parse next word on upper level
			return ELSIF;
		} else if (cmd.equals("endif")) {
			return ENDIF;
		} else if (cmd.equals("include")) {
			String filename = getNextString();
			// System.out.println("include:" + filename);
			directiveInclude(enable, filename);
		} else {
			// macro replace
			if (enable) {
				String value = defines.get(cmd);
				if (value != null) {
					buffer.append(value);
				} else {
					// not match
					buffer.append('`');
					buffer.append(cmd);
					// store in the lastcode
					lastCode += cmd;
				}
			}
			return CONTINUED;
		}
		return CONTINUED;
	}

	private void directiveIf(boolean enable, boolean ifdef) {
		String def = getNextIdent();
		boolean cond;
		if (ifdef)
			cond = (defines.get(def) != null);
		else
			cond = (defines.get(def) == null);
		int status = parseRegion(enable && cond);
		boolean done = cond;
		while (status == ELSIF) {
			def = getNextIdent();
			cond = (defines.get(def) != null);
			status = parseRegion(enable && done == false && cond);
			done = done || cond;
		}
		if (status == ELSE) {
			parseRegion(enable && done == false);
		}
	}

	private void directiveInclude(boolean enable, String filename) {
		if (enable == false)
			return;
		boolean storeIsInclude = isInclude;
		CharReader storeReader = reader;
		isInclude = true;
		File file = new File(directory, filename);
		try {
			FileInputStream in = new FileInputStream(file);
			reader = new CharReader(new InputStreamReader(in));
			parseRegion(true);
			reader.close();
		} catch (IOException e) {
		}
		isInclude = storeIsInclude;
		reader = storeReader;
	}

	private String getIdent() {
		StringBuffer word = new StringBuffer();
		char next = reader.read();
		while (Character.isJavaIdentifierPart(next)) {
			word.append(next);
			next = reader.read();
		}
		reader.pushBack(next);
		return word.toString();
	}

	private String getNextIdent() {
		skipSpace();
		return getIdent();
	}

	private String getNextString() {
		skipSpace();
		char next = reader.read();
		if (next != '"')
			return "";
		StringBuffer word = new StringBuffer();
		next = reader.read();
		while (next != '"' && reader.isEof() == false) {
			word.append(next);
			next = reader.read();
		}
		// consume last '"'
		return word.toString();
	}

	private void skipSpace() {
		char next = reader.read();
		while (Character.isWhitespace(next)) {
			if (reader.isEof())
				return;
			if (next == '\n' && isInclude == false) {
				buffer.append('\n'); // for keeping line number
			}
			next = reader.read();
		}
		reader.pushBack(next);
	}

	private String getToEndOfLine() {
		StringBuffer str = new StringBuffer();
		char next = reader.read();
		while (next != '\n' && reader.isEof() == false) {
			str.append(next);
			next = reader.read();
		}
		if (isInclude == false)
			buffer.append('\n'); // for keeping line number
		return str.toString();
	}

	private static class CharReader {
		private char cbuf[] = new char[1024];
		private int offset;
		private int length;
		private Reader reader;
		private boolean eof;
		private boolean hasNext;
		private boolean isCR;
		private char next;
		private int line;

		public CharReader(Reader reader) {
			this.reader = reader;
			eof = false;
			hasNext = false;
			isCR = false;
			line = 1;
		}

		public char read() {
			if (hasNext) {
				hasNext = false;
				return next;
			}

			if (offset >= length)
				readForward();
			if (eof)
				return ' ';
			else {
				char c = cbuf[offset++];
				switch (c) {
				case '\r':
					isCR = true;
					line++;
					return '\n';
				case '\n':
					if (isCR) {
						isCR = false;
						return read();
					} else {
						line++;
						return '\n';
					}
				default:
					isCR = false;
					return c;
				}
			}
		}

		public boolean isEof() {
			return eof;
		}

		public int getLine() {
			if (hasNext && next == '\n')
				return line - 1;
			else
				return line;
		}

		public void close() {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}

		public void pushBack(char c) {
			hasNext = true;
			next = c;
		}

		private void readForward() {
			if (eof)
				return;
			try {
				length = reader.read(cbuf);
				offset = 0;
				if (length <= 0)
					eof = true;
			} catch (IOException e) {
				eof = true;
			}
		}
	}
}
