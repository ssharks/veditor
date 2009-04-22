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
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.veditor.editor.scanner.HdlPartitionScanner;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.ide.FileStoreEditorInput;

public abstract class HdlDocumentProvider extends FileDocumentProvider
{
	private Map<Object, IFileInfo> fileInfoMap = new HashMap<Object, IFileInfo>();
	
	protected IDocument createDocument(Object element) throws CoreException {
		HdlDocument document = null;

		if (element instanceof IFileEditorInput) {

			// a file is in the current workspace
			IFileEditorInput input = (IFileEditorInput) element;
			IFile file = input.getFile();
			IContainer parent = file.getParent();
			
			// find project
			while (parent instanceof IFolder) {
				parent = parent.getParent();
			}

			if (parent instanceof IProject) {
				document = createHdlDocument((IProject) parent, file);
				if (!setDocumentContent(document, input, getEncoding(element)))
					document = null;
			}

		} else if (element instanceof IStorageEditorInput) {

			// a file is in Repository Exploring Perspective (or others?)
			IStorageEditorInput input = (IStorageEditorInput) element;
			document = createHdlDocument(null, null);
			InputStream stream = input.getStorage().getContents();
			setDocumentContent(document, stream, getEncoding(element));

		} else if (element instanceof IPathEditorInput) {

			// Maybe this is not executed on Eclipse 3.3 or later
			// Eclipse 3.2 executed for a file outside of workspace
			IPathEditorInput input = (IPathEditorInput) element;
			document = createHdlDocument(null, null);
			FileInputStream contentStream = null;
			try {
				contentStream = new FileInputStream(input.getPath().toFile());
				setDocumentContent(document, contentStream,
						getEncoding(element));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				document = null;
			}

		} else if (element instanceof FileStoreEditorInput) {

			// a file that is not part of the current workspace
			FileStoreEditorInput fileStoreEditorInput = (FileStoreEditorInput) element;
			document = createHdlDocument(null, null);
			FileInputStream contentStream = null;
			URI uri = fileStoreEditorInput.getURI();
			if (uri != null && uri.getScheme().equals("file")) {
				String filename = uri.getPath();
				try {
					contentStream = new FileInputStream(filename);
					setDocumentContent(document, contentStream,
							getEncoding(element));
					fileInfoMap.put(element, EFS.getStore(uri).fetchInfo());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					document = null;
				}
			}

		}
		if (document != null) {
			HdlPartitionScanner scanner = document.createPartitionScanner();
			IDocumentPartitioner partitioner = new FastPartitioner(scanner,
					HdlPartitionScanner.getContentTypes());
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
	
	abstract HdlDocument createHdlDocument(IProject project, IFile file);

	public boolean isModifiable(Object element) {
		if (element instanceof FileStoreEditorInput)
			return !isReadOnly(element);
		else
			return super.isModifiable(element);
	}

	public boolean isReadOnly(Object element) {
		if (element instanceof FileStoreEditorInput) {
			IFileInfo info = fileInfoMap.get(element);
			if (info == null)
				return true;	// fail safe
			return info.getAttribute(EFS.ATTRIBUTE_READ_ONLY);
		} else
			return super.isReadOnly(element);
	}

	protected void doSaveDocument(IProgressMonitor monitor, Object element,
			IDocument document, boolean overwrite) throws CoreException {

		if (element instanceof FileStoreEditorInput) {

			// This is same as TextFileDocumentProvider#saveDocument
			// I don't know exactly :-)
			FileStoreEditorInput input = (FileStoreEditorInput) element;
			IFileStore fileStore = EFS.getStore(input.getURI());
			FileBuffers.getTextFileBufferManager().connectFileStore(fileStore,
					monitor);
			ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager()
					.getFileStoreTextFileBuffer(fileStore);
			buffer.getDocument().set(document.get());
			buffer.commit(monitor, true);
			FileBuffers.getTextFileBufferManager().disconnectFileStore(
					fileStore, monitor);
		}
		else
		{
			super.doSaveDocument(monitor, element, document, overwrite);
		}
	}

	
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

