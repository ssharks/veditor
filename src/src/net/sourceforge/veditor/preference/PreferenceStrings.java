/*******************************************************************************
 * Copyright (c) 2011 Ali Ghorashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.sourceforge.veditor.preference;

/** 
 *  This class is meant to be used as the repository for preference
 *  strings. 
 * @author gho18481
 *
 */
public interface PreferenceStrings {
    public static final String INDENT_TYPE = "Style.indent";
    public static final String INDENT_SPACE = "Space";
    public static final String INDENT_TAB = "Tab";
    public static final String INDENT_SIZE = "Style.indentSize";
    public static final String INDENT_SIZE_2 = "2";
    public static final String INDENT_SIZE_3 = "3";
    public static final String INDENT_SIZE_4 = "4";
    public static final String INDENT_SIZE_8 = "8";
    
    public static final String SCAN_ENABLE = "ScanProject.Enable";
    public static final String CONTENT_ASSIST_MODULE_PARAM = "ContentAssist.ModuleParameter";
    public static final String MAX_PARSE_LINES = "ScanProject.MaxFileLines";
    public static final String MAX_PARSE_TIME  = "ScanProject.MaxScanTime";
    
   public static final String NO_SPACE_IN_BRACKET="Style.noSpaceInBracket";     
   public static final String SPACE_BEFORE_OPERATOR_2="Style.spaceBeforeOperator2"; 
   public static final String SPACE_AFTER_OPERATOR_2="Style.spaceAfterOperator2";  
   public static final String SPACE_BEFORE_OPERATOR_1="Style.spaceBeforeOperator1"; 
   public static final String SPACE_AFTER_OPERATOR_1="Style.spaceAfterOperator1";  
   public static final String SPACE_BEFORE_COMMA="Style.spaceBeforeComma";     
   public static final String SPACE_AFTER_COMMA="Style.spaceAfterComma";      
   public static final String SPACE_BEFORE_SEMICOLON="Style.spaceBeforeSemicolon"; 
   public static final String SPACE_BEFORE_OPEN_PAREN="Style.spaceBeforeOpenParen"; 
   public static final String SPACE_AFTER_OPEN_PAREN="Style.spaceAfterOpenParen";  
   public static final String SPACE_BEFORE_CLOSE_PAREN = "Style.spaceBeforeCloseParen";
   public static final String SPACE_BEFORE_OPEN_BRACKET= "Style.spaceBeforeOpenBracket";
   public static final String SPACE_AFTER_OPEN_BRACKET="Style.spaceAfterOpenBracket";
   public static final String SPACE_BEFORE_CLOSE_BRACKET="Style.spaceBeforeCloseBracket";
   public static final String SPACE_BEFORE_OPEN_BRACE="Style.spaceBeforeOpenBrace"; 
   public static final String SPACE_AFTER_OPEN_BRACE="Style.spaceAfterOpenBrace";  
   public static final String SPACE_BEFORE_CLOSE_BRACE="Style.spaceBeforeCloseBrace";
   public static final String SPACE_BEFORE_CASE_COLON="Style.spaceBeforeCaseColon"; 
   public static final String SPACE_AFTER_CASE_COLON="Style.spaceAfterCaseColon";  
   public static final String SPACE_AFTER_IF="Style.spaceAfterIf";         
   public static final String SPACE_AFTER_FOR="Style.spaceAfterFor";        
   public static final String SPACE_AFTER_WHILE="Style.spaceAfterWhile";      
   public static final String SPACE_AFTER_REPEAT="Style.spaceAfterRepeat";     
    
   public static final String PAD_OPERATORS="Style.Vhdl.PadOperators";
   public static final String INDENT_LIBRARY="Style.Vhdl.IndentLibrary";
   public static final String KEYWORDS_LOWERCASE="Style.Vhdl.KeywordsLowercase";
   public static final String ALIGNONARROWRIGHT="Style.Vhdl.AlignOnArrowRight";
   public static final String ALIGNONARROWLEFT="Style.Vhdl.AlignOnArrowLeft";
   public static final String ALIGNONCOLON="Style.Vhdl.AlignOnColon";
   
   public static final String DOXGEN_COMMENT =  "DoxygenComment";
   public static final String SINGLE_LINE_COMMENT = "SingleLineComment";
   public static final String MULTI_LINE_COMMENT = "MultiLineComment";
   public static final String STRING = "String";
   public static final String DEFAULT = "Default";
   public static final String KEYWORD = "KeyWord";
   public static final String DIRECTIVE = "Directive";
   public static final String TYPES = "Types";
   public static final String AUTO_TASKS = "AutoTasks";
   
   public static final String MODULE_PARAMETERS= "ContentAssist.ModuleParameter";
   public static final String ENABLE_SCAN_PROJECT= "ScanProject.Enable";
   public static final String SORT_OUTLINE = "Outline.Sort";
   public static final String FILTER_SINGALS_IN_OUTLINE="Outline.FilterSignals";
   public static final String FILTER_PORTS_IN_OUTLINE="Outline.FilterPorts";
   public static final String SAVE_BEFORE_COMPILE="Compile.SaveBeforeCompile";
   public static final String COMPILE_COMMAND="Compile.command";
   public static final String SYNTH_COMMAND="Synthesize.command";
   public static final String COMPILE_FOLDER="Compile.Folder";
   public static final String ERROR_PARSER="ErrorParser";
    
	public static final String SEMANTIC_WARNING = "Warning";
	public static final String WARNING_UNRESOLVED = "Warning.unresolvedSignal";
	public static final String WARNING_NO_USED_ASIGNED = "Warning.noUsedAsigned";
	public static final String WARNING_BIT_WIDTH = "Warning.bitWidth";
	public static final String WARNING_INT_CONSTANT = "Warning.integerConstant";
	public static final String WARNING_BLOCKING_ASSIGNMENT = "Warning.blockingAssignment";
	public static final String WARNING_BLOCKING_ASSIGNMENT_IN_ALWAYS = "Warning.blockingAssignmentInAlways";
	public static final String WARNING_UNRESOLVED_MODULE = "Warning.unresolvedModule";
	public static final String WARNING_MODULE_CONNECTION = "Warning.moduleConnection";
}
