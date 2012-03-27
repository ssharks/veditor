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

package net.sourceforge.veditor.builder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class ErrorParser
{
	private static final String DEFAULT_ERROR_PARSER_REGEX =
		"ModelSim"
		+ "\n" + "[#|\\*].*Error: ([^\\(]*)\\(([0-9]*)\\): (.*)"		
		+ "\n" + "[#|\\*].*Warning: ([^\\(]*)\\(([0-9]*)\\): (.*)"		
		+ "\n" + ""
		+ "\n" + "ModelSimSimulation"
		+ "\n" + "[#|\\*] Break in ([^\\#]*) at ([^\\(#]*) line ([0-9]*)"
		+ "\n" + ""
		+ "\n" + "[#|\\*](.*)File: ([^\\#]*) Line: ([0-9]*)"
		+ "\n" + "Cver"
		+ "\n" + "\\*\\*(.*)\\(([0-9]+)\\) ERROR\\*\\* (.*)"
		+ "\n" + "\\*\\*(.*)\\(([0-9]+)\\) WARN\\*\\* (.*)"
		+ "\n" + "--(.*)\\(([0-9]+)\\) INFORM-- (.*)" 
		+ "\n" + "Icarus Verilog"
		+ "\n" + "(.*):([0-9]+): [a-z ]*error: (.*)"
		+ "\n" + "(.*):([0-9]+): warning: (.*)"
		+ "\n" + ""
		+ "\n" + "FreeHDL"
		+ "\n" + "(.*):([0-9]+): error: (.*)" 
		+ "\n" + "(.*):([0-9]+): warning: (.*)" 
		+ "\n" + "";
	
	
	private static final String PREFERENCE_NAME = "ErrorParser";
	private static ErrorParser[] parsers = null;
	private static String previousCompiler = "";
	
	public static ErrorParser[] getParsers()
	{
		if(parsers==null) {
			// first add the default parsers:
			String[] stringsdefault = DEFAULT_ERROR_PARSER_REGEX.split("\n",-1);
			List<String> stringsuser = VerilogPlugin.getPreferenceStrings(PREFERENCE_NAME);
			
			// remove user parsers with the same name as a default parser:
			for (int i = 0; i < stringsuser.size(); i += 4) {
				String username = stringsuser.get(i);
				boolean found=false;
				for (int j = 0; j < stringsdefault.length; j += 4) {
					String defaultname = stringsdefault[j];
					if(defaultname.equals(username)) found=true;
				}
				if(found) {
					stringsuser.remove(i+3);
					stringsuser.remove(i+2);
					stringsuser.remove(i+1);
					stringsuser.remove(i);
					i-=4;
				}
			}
		
			parsers = new ErrorParser[stringsdefault.length/4  + stringsuser.size() / 4];
			
			for (int i = 0; i < stringsdefault.length; i += 4) {
				String name = stringsdefault[i];
				String err = stringsdefault[i + 1];
				String warn = stringsdefault[i + 2];
				String info = stringsdefault[i + 3];
				ErrorParser parser = new ErrorParser(name);
				parser.setRegex(err, warn, info);
				parser.editable = false;
				parsers[i/4] = parser;
			}
			
			for (int i = 0; i < stringsuser.size(); i += 4)
		{
				String name = stringsuser.get(i).toString();
				String err = stringsuser.get(i + 1).toString();
				String warn = stringsuser.get(i + 2).toString();
				String info = stringsuser.get(i + 3).toString();
				ErrorParser parser = new ErrorParser(name);
				parser.setRegex(err, warn, info);
				parser.editable = true;
				parsers[stringsdefault.length/4 + i/4] = parser;
			}
		}
		return parsers;
	}
	
	public static List<ErrorParser> getParserList()
	{
		ErrorParser[] parsers = getParsers();
		List<ErrorParser> list = new ArrayList<ErrorParser>();
		for (ErrorParser parser:parsers)
			{
			list.add(parser);
		}
		return list;
	}
		
	public static void installParser(BuildConfig buildconfig, IProject proj)
		{
		String compiler = buildconfig.getParser();
		ErrorParser[] parsers = getParsers();
		for (ErrorParser parse:parsers)
			{
			if (parse.getCompilerName().equals(compiler))
			{
				parse.project = proj;
				parse.buildConfig=buildconfig;
			}
		}
		
		if(!compiler.equals(previousCompiler)) {
			for (ErrorParser parse:parsers)
			{
				if (parse.getCompilerName().equals(compiler))
				{
					VerilogPlugin.addPatternMatchListener(parse.errParser);
					VerilogPlugin.addPatternMatchListener(parse.warnParser);
					VerilogPlugin.addPatternMatchListener(parse.infoParser);
	}
				if (parse.getCompilerName().equals(previousCompiler))
	{
					VerilogPlugin.removePatternMatchListener(parse.errParser);
					VerilogPlugin.removePatternMatchListener(parse.warnParser);
					VerilogPlugin.removePatternMatchListener(parse.infoParser);
				}
			}
			previousCompiler = compiler;
		}
	}
	
	public static void setParsers(ErrorParser[] parsers)
	{
		List<String> strings = new ArrayList<String>();
		for (int i = 0; i < parsers.length; i++)
		{
			if(!parsers[i].isEditable()) continue;
			strings.add(parsers[i].compilerName);
			strings.add(parsers[i].errRegex);
			strings.add(parsers[i].warnRegex);
			strings.add(parsers[i].infoRegex);
		}
		VerilogPlugin.setPreference(PREFERENCE_NAME, strings);
		parsers = null;
	}
	
	public static void setParserList(List<ErrorParser> list)
	{
		List<String> strings = new ArrayList<String>();
		Iterator<ErrorParser> i = list.iterator();
		while(i.hasNext())
		{
			ErrorParser parser = (ErrorParser)i.next();
			if(!parser.isEditable()) continue;
			strings.add(parser.compilerName);
			strings.add(parser.errRegex);
			strings.add(parser.warnRegex);
			strings.add(parser.infoRegex);
		}
		VerilogPlugin.setPreference(PREFERENCE_NAME, strings);
		parsers = null;
	}
	
	public static void setDefaultParsers()
	{
		VerilogPlugin.setDefaultPreference(PREFERENCE_NAME);
		parsers = null;
	}

	private IProject project;
	private BuildConfig buildConfig;
	private String compilerName;
	private String errRegex;
	private String warnRegex;
	private String infoRegex;
	private boolean editable;
	ConsoleParser errParser;
	ConsoleParser warnParser;
	ConsoleParser infoParser;

	public ErrorParser(String compilerName)
	{
		this.compilerName = compilerName;
		editable = true;
	}
	
	public String getCompilerName()
	{
		return compilerName;
	}
	
	public boolean isEditable()
	{
		return editable;
	}
	
	public void setRegex(String errRegex, String warnRegex, String infoRegex)
	{
		this.errRegex = errRegex;
		this.warnRegex = warnRegex;
		this.infoRegex = infoRegex;
		errParser = new ConsoleParser(errRegex, IMarker.SEVERITY_ERROR);
		warnParser = new ConsoleParser(warnRegex, IMarker.SEVERITY_WARNING);
		infoParser = new ConsoleParser(infoRegex, IMarker.SEVERITY_INFO);
	}
	public void setRegex(int num, String regex)
	{
		switch(num)
		{
			case 0:
				errRegex = regex;
				break;
			case 1:
				warnRegex = regex;
				break;
			case 2:
				infoRegex = regex;
				break;
		}
	}
	public String getErrorRegex()
	{
		return errRegex;
	}
	public String getWarningRegex()
	{
		return warnRegex;
	}
	public String getInfoRegex()
	{
		return infoRegex;
	}
	
	private void reportMissingFile(String filename){
		String message = new String();
		message=String.format("\"%s\" is not found in the project. MS Windows users, check filename case!!!", filename);			
		try{
			IMarker marker=project.createMarker("net.sourceforge.veditor.builderproblemmarker");
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);			
			marker.setAttribute(IMarker.MESSAGE, message);
		}
		catch (CoreException e)
		{
		}		
	}

	private void setProblemMarker(String filename, int level, int lineNumber, String msg)
	{
		IResource file = getFile(filename);
		if (file != null && lineNumber > 0){
			VerilogPlugin.setExternalProblemMarker(file, level, lineNumber, msg);
		}
		else{
			reportMissingFile(filename);
		}
	}

	private IFile getFileRecursive(IContainer cont, IPath path) {
		try {
			for(IResource res: cont.members()) {
				if(res instanceof IContainer) {
					IFile result = getFileRecursive((IContainer)res,path);
					if(result!=null) return result;
				} else if(res instanceof IFile) {					
					IPath res_path = ((IFile)res).getLocation();					
					if(res_path.equals(path)) 
						return (IFile)res;
		}
		}
		} catch (CoreException e) {
		}
		return null;
	}
	
	
	private IResource getFile(String filename)
	{
		IPath projectPath = project.getLocation().append(buildConfig.getWorkFolder());
		projectPath = projectPath.append(filename);	
		IResource test = getFileRecursive(project,projectPath);
		return test;
	}
	
	public static class ParseErrorString {
		private String regex;
		// results of parse(String string);
		public String filename;
		public int linenr;
		public String message;
		public int startinmatchedstring;
		public int endinmatchedstring;
		
		public ParseErrorString(String regexpr) {
			regex = regexpr;
		}

		/**
		 * Tries to parse string using regex
		 * @return boolean: parse succeeded
		 */
		public boolean parse(String string) {
			Pattern errPattern = Pattern.compile(regex);
			Matcher m = errPattern.matcher(string);
			if (!m.matches()) return false;

			int groupCount=m.groupCount();
			if(groupCount < 3) return false;
			
			int linenrindex = -1;
		
			for(int i=2;i<=groupCount;i++) {
				String group = m.group(i);
				try {
					linenr = Integer.parseInt(group);
					linenrindex = i;
				}
				catch (NumberFormatException e) {
				}
			}
			if(linenrindex==-1) return false;
			
			// filename is now at linenrindex-1
			filename = m.group(linenrindex-1);
			
			// now search for the longest string to capture the message:
			int length_win=-1;
			int messageindex=-1;
			for(int i=1;i<=groupCount;i++) {
				if(i==linenrindex-1) continue;
				if(i==linenrindex) continue;
				String group = m.group(i);
				if(group.length()>length_win) {
					length_win = group.length();
					messageindex = i;
				}
			}
			if(messageindex==-1) return false;
			
			message = m.group(messageindex);
			startinmatchedstring = m.start(linenrindex-1);
			endinmatchedstring = m.end(linenrindex);
			
			return true;
		}
	}
	
	public class ConsoleParser implements IPatternMatchListener {
		private String regex;
		private int problemlevel;
		
		ConsoleParser(String regexpr, int level) {
			regex = regexpr;
			problemlevel = level;
		}
		
		public int getCompilerFlags() {
			return 0;
		}
	
		public String getLineQualifier() {
			return null;
		}
	
		public String getPattern() {
			return regex;
		}

		public void connect(TextConsole console) {}
		public void disconnect() {}
	
		public void matchFound(PatternMatchEvent event) {
			int offset = event.getOffset();
			int length = event.getLength();
			
			Object object = event.getSource();
			if(! (object instanceof TextConsole)) return;
			TextConsole console = (TextConsole)object;
			
			String consolecontent = console.getDocument().get();
			String matchedstring = consolecontent.substring(offset, offset+length);
			
			ParseErrorString parser = new ParseErrorString(regex);
			boolean success = parser.parse(matchedstring);
			if (!success) return;
				
			setProblemMarker(parser.filename, problemlevel, parser.linenr, parser.message);			
			
			IResource resource = getFile(parser.filename);
			if(resource instanceof IFile) {
				IFile file = (IFile) resource;
				FileLink hyperlink = new FileLink(file,null,-1,-1,parser.linenr);
				try {
					console.addHyperlink(hyperlink, offset+parser.startinmatchedstring,
							parser.endinmatchedstring-parser.startinmatchedstring+1);
				} catch (BadLocationException e) {
				}
			} else {
				//VerilogPlugin.println("Not a filename!");
			}
		}
	}

}


