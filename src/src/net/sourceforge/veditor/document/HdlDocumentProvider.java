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

package net.sourceforge.veditor.document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;

import net.sourceforge.veditor.editor.HdlPartitionScanner;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.ide.FileStoreEditorInput;

public abstract class HdlDocumentProvider extends FileDocumentProvider
{
	protected IDocument createDocument(Object element) throws CoreException
	{
		HdlDocument document = null;

		if (element instanceof IFileEditorInput)
		{
			// find project
			IFileEditorInput input = (IFileEditorInput)element;
			IFile file = input.getFile();

			IContainer parent = file.getParent();
			while (parent instanceof IFolder)
			{
				parent = parent.getParent();
			}
			if (parent instanceof IProject)
			{
				document = createHdlDocument((IProject)parent, file);
				if (!setDocumentContent(document, input, getEncoding(element)))
					document = null;
			}
		}
		else if (element instanceof IPathEditorInput)
		{
			IPathEditorInput input = (IPathEditorInput) element;
			document = createHdlDocument(null, null);
			FileInputStream contentStream = null;
			try
			{
				contentStream = new FileInputStream(input.getPath().toFile());
				setDocumentContent(document, contentStream, getEncoding(element));
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
				document = null;
			}
		}
		else if (element instanceof FileStoreEditorInput){
			// a file that is not part of the current workspace
			FileStoreEditorInput fileStoreEditorInput=(FileStoreEditorInput) element;
			document = createHdlDocument(null, null);
			FileInputStream contentStream = null;
			URI uri=fileStoreEditorInput.getURI();
			if(uri !=null && uri.getScheme().equals("file")){
				String filename=uri.getPath();
				try
				{
					contentStream = new FileInputStream(filename);
					setDocumentContent(document, contentStream, getEncoding(element));
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
					document = null;
				}
			}
			
		}
		if (document != null)
		{
			HdlPartitionScanner scanner = document.createPartitionScanner();
			IDocumentPartitioner partitioner = new FastPartitioner(scanner,
					HdlPartitionScanner.getContentTypes());
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
	
	abstract HdlDocument createHdlDocument(IProject project, IFile file);
	

//	Don't use special encoding.
//	Set encoding in Project Properties page
//	protected String getPersistedEncoding(Object element)
//	{
//		String encoding = VerilogPlugin.getPreferenceString("Encoding");
//		if (encoding == null || encoding.equals(""))
//			return super.getPersistedEncoding(element);
//		else
//			return encoding;
//	}

}

