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
package net.sourceforge.veditor.parser;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * module database
 */
public final class ModuleList
{
	private static Set projectList = new HashSet();
	private static ModuleList current;

	public static void setCurrent(IProject proj)
	{
		if (current != null && current.toString().equals(proj.toString()))
			return;

		// System.out.println( "set current project " + proj );
		ModuleList mods = find(proj);
		if (mods != null)
		{
			current = mods;
			return;
		}

		// System.out.println( "new project " + proj );
		current = new ModuleList(proj);
		projectList.add(current);
		current.construct(proj, proj);
	}
	public static ModuleList getCurrent()
	{
		return current;
	}
	public static ModuleList find(IProject proj)
	{
		Iterator i = projectList.iterator();
		while (i.hasNext())
		{
			ModuleList mods = (ModuleList)i.next();
			if (mods.toString().equals(proj.toString()))
				return mods;
		}
		return null;
	}

	private ModuleList(IProject project)
	{
		this.project = project;
	}

	/**
	 * construct module database by name<p/>
	 * It doesn't read file
	 */
	private void construct(IProject project, IContainer parent)
	{
		try
		{
			IResource[] members;
			members = parent.members();
			for (int i = 0; i < members.length; i++)
			{
				if (members[i] instanceof IContainer)
					construct(project, (IContainer)members[i]);
				if (members[i] instanceof IFile)
				{
					IFile file = (IFile)members[i];
					String name = file.getName();
					int len = name.length();
					if (len >= 3 && name.substring(len - 2).equals(".v"))
					{
						name = name.substring(0, len - 2);
						Module mod = new ModuleReference(name);
						replaceModule(mod);
					}
				}
			}
		}
		catch (CoreException e)
		{
		}
	}


	// for projectList
	public boolean equals(Object obj)
	{
		if (obj instanceof ModuleList)
			return project.toString().equals(obj.toString());  // project name is system unique
		else
			return false;
	}
	public int hashCode()
	{
		return project.toString().hashCode();
	}
	public String toString()
	{
		return project.toString();
	}


	/**
	 * refered project
	 */
	private IProject project;

	/**
	 * Module database
	 */
	private Set list = new HashSet();

	public Module newModule(int line, String name, IFile file)
	{
		Module module = new ModuleDefinition(line, name, file);
		replaceModule(module);
		return module;
	}

	/**
	 * find from module database
	 */
	public Module findModule(String name)
	{
		Module mod = findModuleImmediate(name);
		if (mod == null)
			return null;
		else if (mod instanceof ModuleDefinition)
			return mod;
		else
		{
			readModule(name);
			mod = findModuleImmediate(name);
			if (mod instanceof ModuleDefinition)
				return mod;
			else
				return null;
		}
	}
	private Module findModuleImmediate(String name)
	{
		Iterator i = list.iterator();
		while (i.hasNext())
		{
			Module mod = (Module)i.next();
			if (mod.toString().equals(name))
			{
				return mod;
			}
		}
		return null;
	}

	private void readModule(String name)
	{
		IFile file = findFile(name + ".v");
		// System.out.println( "file : " + file );

		try
		{
			InputStreamReader reader = new InputStreamReader(file.getContents());
			VerilogParser parser = new VerilogParser(reader);
			parser.parse(project, file);
		}
		catch (CoreException e)
		{
		}
	}

	private IFile findFile(String fileName)
	{
		return findFile(project, fileName);
	}

	private IFile findFile(IContainer parent, String fileName)
	{
		try
		{
			IResource[] members;
			members = parent.members();
			for (int i = 0; i < members.length; i++)
			{
				if (members[i] instanceof IContainer)
				{
					IFile file = findFile((IContainer)members[i], fileName);
					if (file != null)
						return file;
				}
				if (members[i] instanceof IFile)
				{
					IFile file = (IFile)members[i];
					if (fileName.equals(file.getName()))
						return file;
				}
			}
		}
		catch (CoreException e)
		{
		}
		return null;
	}

	public String[] getModuleNames()
	{
		String[] strs = new String[list.size()];
		Iterator i = list.iterator();
		int n = 0;
		while (i.hasNext())
		{
			strs[n++] = i.next().toString();
		}
		return strs;
	}

	/**
	 * This is called when module is parsed
	 */
	private void replaceModule(Module mod)
	{
		list.remove(mod);
		list.add(mod);
		// System.out.println( "number of modules : " + list.size());
	}

	/**
	 * reference of module<p/>
	 * It has only file name (*.v)
	 */
	private class ModuleReference extends Module
	{
		public ModuleReference(String name)
		{
			super(name);
		}
	}

	/**
	 * definition of module<p/>
	 * it has file name, line number, ports and elements etc.
	 */
	private class ModuleDefinition extends Module
	{
		public ModuleDefinition(int line, String name, IFile file)
		{
			super(line, name);
			this.file = file;
		}
		private IFile file;
		public IFile getFile()
		{
			return file;
		}

		/**
		 * instance, function, task, comment
		 */
		private List elements = new ArrayList();
		public Object[] getElements()
		{
			int size = elements.size();
			if (size == 0)
				return null;
			else
			{
				Collections.sort(elements);
				Object[] eary = new Object[size];
				for (int i = 0; i < size; i++)
					eary[i] = elements.get(i);
				return eary;
			}
		}

		/**
		 * input/output prots
		 */
		private List ports = new ArrayList();
		public Iterator getPortIterator()
		{
			return ports.iterator();
		}

		//  called by parser
		public void addPort(String name)
		{
			ports.add(name);
		}
		public void addElement(int begin, int end, String typeName, String name)
		{
			Element child = new Element(begin, this, typeName, name);
			child.setEndLine(end);
			elements.add(child);
		}
		public void addComment(int begin, String str)
		{
			if (!isValidComment(str))
				return;

			//  コメント行が連続している場合は最初で代表させる
			if (begin == lastCommentLine + 1)
			{
				lastCommentLine = begin;
				return;
			}

			//  行の最初の"//"と無駄な文字を削除する
			for (int i = 0; i < str.length(); i++)
			{
				char ch = str.charAt(i);
				if (Character.isLetterOrDigit(ch))
				{
					str = str.substring(i);
					break;
				}
			}
			//  行の最後の無駄な文字を削除する
			for (int i = str.length() - 1; i >= 0; i--)
			{
				char ch = str.charAt(i);
				if (Character.isLetterOrDigit(ch))
				{
					str = str.substring(0, i + 1);
					break;
				}
			}

			Element comment = new Element(begin, this, "//", str);
			elements.add(comment);

			lastCommentLine = begin;
		}
		private int lastCommentLine;
		private boolean isValidComment(String str)
		{
			for (int i = 0; i < str.length(); i++)
			{
				char ch = str.charAt(i);
				if (Character.isLetterOrDigit(ch))
					return true;
			}
			return false;
		}

	}
}



