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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class VerilogDocumentProvider extends FileDocumentProvider
{
	protected IDocument createDocument(Object element) throws CoreException
	{
		IDocument document = null;

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
				document = new VerilogDocument((IProject)parent, file);
				if (!setDocumentContent(document, input, getEncoding(element)))
					document = null;
			}
		}
		if (document != null)
		{
			IDocumentPartitioner partitioner =
				new DefaultPartitioner(
					new VerilogPartitionScanner(),
					VerilogPartitionScanner.getContentTypes());
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
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

