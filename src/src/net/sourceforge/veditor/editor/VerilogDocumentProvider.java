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
	private static IProject currentProject ;

	protected IDocument createDocument(Object element) throws CoreException
	{
		IDocument document = super.createDocument(element);

		if ( element instanceof IFileEditorInput )
		{
			// カレントのプロジェクトを検索する
			IFile file = ((IFileEditorInput)element).getFile();
			IContainer parent = file.getParent();
			while( parent instanceof IFolder )
			{
				parent = parent.getParent();
			}
			if ( parent instanceof IProject )
				currentProject = (IProject)parent ;
		}
		if (document != null)
		{
			IDocumentPartitioner partitioner =
				new DefaultPartitioner(
					new VerilogPartitionScanner(),
					new String[] {
						VerilogPartitionScanner.VERILOG_SINGLE_LINE_COMMENT,
						VerilogPartitionScanner.VERILOG_MULTI_LINE_COMMENT,
						VerilogPartitionScanner.VERILOG_STRING });
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}

	/**
	 * @return	カレントのプロジェクト
	 */
	public static IProject getCurrentProject()
	{
		return currentProject;
	}

}