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

import java.io.Reader;

import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.ParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class VerilogDocument extends HdlDocument
{
	public VerilogDocument(IProject project, IFile file)
	{
		super(project, file);
	}

	public HdlPartitionScanner createPartitionScanner()
	{
		return HdlPartitionScanner.createVerilogPartitionScanner();
	}
	
	public IParser createParser(Reader reader)
	{
		return ParserFactory.createVerilogParser(reader, getFile());
	}
}
