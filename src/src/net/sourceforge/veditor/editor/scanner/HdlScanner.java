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

package net.sourceforge.veditor.editor.scanner;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.veditor.editor.ColorManager;
import net.sourceforge.veditor.editor.HdlTextAttribute;
import org.eclipse.jface.text.rules.*;


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
			"generate", "endgenerate", "genvar", "localparam"};

	private static final String[] verilogDirectives = { "`ifdef", "`else",
			"`endif", "`if", "`define", "`undef", "`timescale", "`include" };

	public static final String[] vhdlWords = { "abs", "access", "after",
			"alias", "all", "and", "architecture", "array", "assert",
			"attribute", "begin", "block", "body", "buffer", "bus", "case",
			"component", "configuration", "constant", "disconnect", "downto",
			"else", "elsif", "end", "entity", "exit", "file", "for",
			"function", "generate", "generic", "guarded", "if", "impure", "in",
			"inertial", "inout", "is", "label", "library", "linkage",
			"literal", "loop", "map", "mod", "nand", "new", "next", "nor",
			"not", "null", "of", "on", "open", "or", "others", "out",
			"package", "port", "postponed", "procedure", "process", "pure",
			"range", "record", "register", "reject", "rem", "report", "return",
			"rol", "ror", "select", "severity", "shared", "signal", "sla",
			"sll", "sra", "srl", "subtype", "then", "to", "transport", "type",
			"unaffected", "units", "until", "use", "variable", "wait", "when",
			"while", "with", "xnor", "xor" };
	
	private static final String[] vhdlTypes = { "bit", "bit_vector", "character", 
			"boolean", "integer", "real", "time", "string",	"severity_level", 
			"positive", "natural", "signed", "unsigned", "line", "text",
			"std_logic", "std_logic_vector", "std_ulogic", "std_ulogic_vector", 
			"qsim_state", "qsim_state_vector", "qsim_12state",
			"qsim_12state_vector", "qsim_strength", "mux_bit", "mux_vector", 
			"reg_bit", "reg_vector", "wor_bit",	"wor_vector"};

	private HdlScanner(ColorManager manager, boolean isVerilog)
	{
		IToken keyword = new Token(HdlTextAttribute.KEY_WORD
				.getTextAttribute(manager));
		IToken directive = new Token(HdlTextAttribute.DIRECTIVE
				.getTextAttribute(manager));
		IToken other = new Token(HdlTextAttribute.DEFAULT
				.getTextAttribute(manager));
		IToken types = new Token(HdlTextAttribute.TYPES.getTextAttribute(manager));

		List<IRule> rules = new ArrayList<IRule>();

		WordRule wordRule;

		if (isVerilog)
		{
		    wordRule= new WordRule(new net.sourceforge.veditor.editor.scanner.verilog.WordDetector(), other);
			for (int i = 0; i < verilogDirectives.length; i++)
				wordRule.addWord(verilogDirectives[i], directive);
			for (int i = 0; i < verilogWords.length; i++)
				wordRule.addWord(verilogWords[i], keyword);
		}
		else
		{
		    wordRule= new WordRule(new  net.sourceforge.veditor.editor.scanner.vhdl.WordDetector(), other);
			for (int i = 0; i < vhdlWords.length; i++)
				wordRule.addWord(vhdlWords[i], keyword);
			// it is possible to use upper case in VHDL
			for (int i = 0; i < vhdlWords.length; i++)
				wordRule.addWord(vhdlWords[i].toUpperCase(), keyword);
			
			for (int i = 0; i < vhdlTypes.length; i++)
				wordRule.addWord(vhdlTypes[i], types);
			// it is possible to use upper case in VHDL
			for (int i = 0; i < vhdlTypes.length; i++)
				wordRule.addWord(vhdlTypes[i].toUpperCase(), types);
		}
		
		rules.add(wordRule);

		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}	
}

