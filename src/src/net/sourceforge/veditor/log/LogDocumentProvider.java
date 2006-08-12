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
package net.sourceforge.veditor.log;

import java.util.Map;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.builder.ErrorParser;
import net.sourceforge.veditor.builder.HdlNature;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class LogDocumentProvider extends FileDocumentProvider
{
	protected IDocument createDocument(Object element) throws CoreException
	{
		IDocument document = super.createDocument(element);
		
		parseDocument(element);

		return document;
	}

	protected void doSynchronize(Object element, IProgressMonitor monitor)
			throws CoreException
	{
		super.doSynchronize(element, monitor);
		parseDocument(element);
	}

	private void parseDocument(Object element) throws CoreException
	{
		IDocument document = super.createDocument(element);

		if (document == null)
			return;

		if (element instanceof IFileEditorInput)
		{
			// find project
			IFileEditorInput input = (IFileEditorInput) element;
			IFile file = input.getFile();

			IContainer folder = file.getParent();
			IContainer project = folder;
			while (project instanceof IFolder)
			{
				project = project.getParent();
			}
			if (project instanceof IProject)
			{
				parse((IProject)project, folder, document.get());
			}
		}
	}

	private void parse(IProject project, IContainer folder, String message)
	{
		HdlNature nature = new HdlNature(project);
		ICommand command = nature.getSimulateCommand();
		if (command == null)
			return;
		Map args = command.getArguments();
		Object parserName = args.get("parser");
		if (parserName == null)
			return;

		VerilogPlugin.clearProblemMarker(project);
		ErrorParser parser;
		parser = ErrorParser.getParser(parserName.toString());
		if (parser != null)
			parser.parse(folder, message);
	}
}





