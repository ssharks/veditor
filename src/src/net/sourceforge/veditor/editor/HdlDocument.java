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
package net.sourceforge.veditor.editor;

import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.ParserManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.Document;

abstract public class HdlDocument extends Document
{
	/**
	 * project which has this verilog source file
	 */
	private IProject project;
	private IFile file;

	public HdlDocument(IProject project, IFile file)
	{
		super();
		this.project = project;
		this.file = file;
	}

	public IProject getProject()
	{
		return project;
	}

	public IFile getFile()
	{
		return file;
	}

	public ParserManager createParserManager(String text)
	{
		IParser parser = createParser(text);
		return parser.getManager();
	}

	public ParserManager createParserManager()
	{
		return createParserManager(get());
	}

	abstract public HdlPartitionScanner createPartitionScanner();
	abstract public IParser createParser(String text);
}



