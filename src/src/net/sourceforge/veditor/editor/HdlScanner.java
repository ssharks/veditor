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

package net.sourceforge.veditor.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * find reserved word
 */
public class HdlScanner extends RuleBasedScanner
{
	public static HdlScanner createForVerilog(ColorManager manager)
	{
		return new HdlScanner(manager, true);
	}
	public static HdlScanner createForVhdl(ColorManager manager)
	{
		return new HdlScanner(manager, false);
	}

	private static final String[] verilogWords = {"always", "and", "assign",
			"attribute", "begin", "buf", "bufif0", "bufif1", "case", "casex",
			"casez", "cmos", "deassign", "default", "defparam", "disable",
			"edge", "else", "end", "endcase", "endfunction", "endmodule",
			"endprimitive", "endspecify", "endtable", "endtask", "event",
			"for", "force", "forever", "fork", "function", "highz0", "highz1",
			"if", "ifnone", "initial", "inout", "input", "integer", "join",
			"medium", "module", "large", "macromodule", "nand", "negedge",
			"nmos", "nor", "not", "notif0", "notif1", "or", "output",
			"parameter", "pmos", "posedge", "primitive", "pull0", "pull1",
			"pulldown", "pullup", "rcmos", "real", "realtime", "reg",
			"release", "repeat", "rnmos", "rpmos", "rtran", "rtranif0",
			"rtranif1", "scalared", "signed", "small", "specify", "specparam",
			"strenght", "strong0", "strong1", "supply0", "supply1", "table",
			"task", "time", "tran", "tranif0", "tranif1", "tri", "tri0",
			"tri1", "triand", "trior", "trireg", "unsigned", "vectoryd",
			"wait", "wand", "weak0", "weak1", "while", "wire", "wor", "xnor",
			"xor",
			"generate", "endgenerate", "genvar", "localparam"
			};

	private static final String[] vhdlWords = { "abs", "access", "after",
			"alias", "all", "and", "architecture", "array", "assert",
			"attribute", "begin", "block", "body", "buffer", "bus", "case",
			"component", "configuration", "constant", "disconnect", "downto",
			"else", "elsif", "end", "entity", "exit", "file", "for",
			"function", "generate", "generic", "guarded", "if", "in", "inout",
			"is", "label", "library", "linkage", "loop", "map", "mod", "nand",
			"new", "next", "nor", "not", "null", "of", "on", "open", "or",
			"others", "out", "package", "port", "procedure", "process", "range",
			"record", "register", "rem", "report", "return", "select",
			"severity", "signal", "subtype", "then", "to", "transport", "type",
			"units", "until", "use", "variable ", "wait", "when", "while",
			"with", "xor" };

	private HdlScanner(ColorManager manager, boolean isVerilog)
	{
		IToken keyword = new Token(HdlTextAttribute.KEY_WORD
				.getTextAttribute(manager));
		IToken other = new Token(HdlTextAttribute.DEFAULT
				.getTextAttribute(manager));

		List rules = new ArrayList();

		WordRule wordRule = new WordRule(new IWordDetector()
		{
			public boolean isWordPart(char character)
			{
				return Character.isJavaIdentifierPart(character);
			}
			public boolean isWordStart(char character)
			{
				return Character.isJavaIdentifierStart(character);
			}
		}, other);

		if (isVerilog)
		{
			for (int i = 0; i < verilogWords.length; i++)
				wordRule.addWord(verilogWords[i], keyword);
		}
		else
		{
			for (int i = 0; i < vhdlWords.length; i++)
				wordRule.addWord(vhdlWords[i], keyword);
			// it is possible to use upper case in VHDL
			for (int i = 0; i < vhdlWords.length; i++)
				wordRule.addWord(vhdlWords[i].toUpperCase(), keyword);
		}

		rules.add(wordRule);

		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
}

