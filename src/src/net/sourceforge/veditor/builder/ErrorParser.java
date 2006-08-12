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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ErrorParser
{
	private static final String PREFERENCE_NAME = "ErrorParser";
	public static ErrorParser[] getParsers()
	{
		List strings = VerilogPlugin.getPreferenceStrings(PREFERENCE_NAME);
		
		ErrorParser[] parsers = new ErrorParser[strings.size() / 4];
		for (int i = 0; i < strings.size(); i += 4)
		{
			parsers[i/4] = createParser(strings, i);
		}
		return parsers;
	}
	public static List getParserList()
	{
		List list = new ArrayList();
		List strings = VerilogPlugin.getPreferenceStrings(PREFERENCE_NAME);

		if (strings != null)
		{
			for (int i = 0; i < strings.size(); i += 4)
			{
				list.add(createParser(strings, i));
			}
		}
		return list;
	}
	public static ErrorParser getParser(String compiler)
	{
		List strings = VerilogPlugin.getPreferenceStrings(PREFERENCE_NAME);
		
		for (int i = 0; i < strings.size(); i += 4)
		{
			if (strings.get(i).equals(compiler))
			{
				return createParser(strings, i);
			}
		}
		return null;
	}
	private static ErrorParser createParser(List strings, int top)
	{
		String name = strings.get(top).toString();
		String err = strings.get(top + 1).toString();
		String warn = strings.get(top + 2).toString();
		String info = strings.get(top + 3).toString();
		ErrorParser parser = new ErrorParser(name);
		parser.setRegex(err, warn, info);

		return parser;
	}
	public static void setParsers(ErrorParser[] parsers)
	{
		List strings = new ArrayList();
		for (int i = 0; i < parsers.length; i++)
		{
			strings.add(parsers[i].compilerName);
			strings.add(parsers[i].errRegex);
			strings.add(parsers[i].warnRegex);
			strings.add(parsers[i].infoRegex);
		}
		VerilogPlugin.setPreference(PREFERENCE_NAME, strings);
	}
	public static void setParserList(List list)
	{
		List strings = new ArrayList();
		Iterator i = list.iterator();
		while(i.hasNext())
		{
			ErrorParser parser = (ErrorParser)i.next();
			strings.add(parser.compilerName);
			strings.add(parser.errRegex);
			strings.add(parser.warnRegex);
			strings.add(parser.infoRegex);
		}
		VerilogPlugin.setPreference(PREFERENCE_NAME, strings);
	}
	public static void setDefaultParsers()
	{
		VerilogPlugin.setDefaultPreference(PREFERENCE_NAME);
	}

	private IProject project;
	private IContainer folder;
	private String message;
	private int msgIdx;
	private String compilerName;
	private String errRegex;
	private String warnRegex;
	private String infoRegex;

	public ErrorParser(String compilerName)
	{
		this.compilerName = compilerName;
	}
	
	public String getCompilerName()
	{
		return compilerName;
	}
	
	public void setRegex(String errRegex, String warnRegex, String infoRegex)
	{
		this.errRegex = errRegex;
		this.warnRegex = warnRegex;
		this.infoRegex = infoRegex;
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
	
	/**
	 * parse compiler message.
	 */
	public void parse(IContainer folder, String message)
	{
		this.folder = folder;

		IContainer parent = folder.getParent();
		while (parent instanceof IFolder)
		{
			parent = parent.getParent();
		}
		if (parent instanceof IProject)
		{
			project = (IProject)parent;
		}
		else
		{
			// maybe never execute
			return;
		}

		this.message = message;
		msgIdx = 0;

		Pattern errPattern = Pattern.compile(errRegex);
		Pattern warnPattern = Pattern.compile(warnRegex);
		Pattern infoPattern = Pattern.compile(infoRegex);
		
		String[] segs = new String[3]; 
		String lineStr;
		for (lineStr = getLine(); lineStr != null; lineStr = getLine())
		{
			if (!lineStr.equals(""))
			{
				Matcher m;
				m = errPattern.matcher(lineStr);
				if (m.matches())
				{
					getSegment(m, segs);
					setErrorMarker(segs[0], segs[1], segs[2]);
				}
				m = warnPattern.matcher(lineStr);
				if (m.matches())
				{
					getSegment(m, segs);
					setWarningMarker(segs[0], segs[1], segs[2]);
				}
				m = infoPattern.matcher(lineStr);
				if (m.matches())
				{
					getSegment(m, segs);
					setInfoMarker(segs[0], segs[1], segs[2]);
				}
			}
		}
	}
	
	private void getSegment(Matcher m, String[] segs)
	{
		segs[0] = m.group(1);
		segs[1] = m.group(2);
		segs[2] = m.group(3);
	}
	
	private String getLine()
	{
		int next = message.indexOf("\n", msgIdx);
		if (next >= 0)
		{
			String line;
			if (next > 0 && message.charAt(next - 1) == '\r')
			{
				line = message.substring(msgIdx, next-1);
			}
			else
			{
				line = message.substring(msgIdx, next);
			}
			msgIdx = next + 1;
			return line;
		}
		else
			return null;
	}
	
	private int parseLineNumber(String str)
	{
		try
		{
			return Integer.parseInt(str);
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}
	
	private void setErrorMarker(String filename, String line, String msg)
	{
		IResource file = getFile(filename);
		int lineNumber = parseLineNumber(line);
		if (file != null && lineNumber > 0)
			VerilogPlugin.setErrorMarker(file, lineNumber, msg);
	}

	private void setWarningMarker(String filename, String line, String msg)
	{
		IResource file = getFile(filename);
		int lineNumber = parseLineNumber(line);
		if (file != null && lineNumber > 0)
			VerilogPlugin.setWarningMarker(file, lineNumber, msg);
	}

	private void setInfoMarker(String filename, String line, String msg)
	{
		IResource file = getFile(filename);
		int lineNumber = parseLineNumber(line);
		if (file != null && lineNumber > 0)
			VerilogPlugin.setInfoMarker(file, lineNumber, msg);
	}
	
	private IResource getFile(String filename)
	{
		IPath projPath = project.getLocation();
		IPath filePath = new Path(filename);
		int count = projPath.segmentCount();
		if (count < filePath.segmentCount())
		{
			for(int i = 0; i < count; i++)
			{
				if (!projPath.segment(i).equals(filePath.segment(i)))
				{
					return folder.findMember(filename);
				}
			}
			StringBuffer refPath = new StringBuffer();
			for(int i = count; i < filePath.segmentCount(); i++)
			{
				refPath.append('/');
				refPath.append(filePath.segment(i));
			}
			return project.findMember(refPath.toString());
		}
		else
		{
			return folder.findMember(filename);
		}
	}
}


