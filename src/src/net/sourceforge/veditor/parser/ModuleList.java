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
import java.util.HashSet;
import java.util.Iterator;
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
	private static ModuleList current ;

	public static void setCurrent(IProject proj)
	{
		if (current != null && current.toString().equals(proj.toString()))
			return ;

		// System.out.println( "set current project " + proj );
		ModuleList mods = find(proj);
		if (mods != null)
		{
			current = mods ;
			return ;
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
				return mods ;
		}
		return null;
	}

	private ModuleList(IProject project)
	{
		this.project = project ;
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
						Module mod = new Module(name);
						replaceModule(mod);

//						InputStreamReader reader = new InputStreamReader(file.getContents());
//						VerilogParser parser = new VerilogParser(reader);
//						parser.parse(project);
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
	private IProject project ;

	/**
	 * Module database
	 */
	private Set list = new HashSet();

	/**
	 * find from module database
	 */
	public Module findModule(String name)
	{
		Module mod = findModuleSub(name);
		if (mod == null)
			return null;
		else if (mod.isDoneParse())
			return mod ;
		else
		{
			readModule(name);
			return findModuleSub(name);
		}
	}
	private Module findModuleSub(String name)
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
			parser.parse(project);
		}
		catch (CoreException e)
		{
		}
	}

	public IFile findFile(String fileName)
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
		int n = 0 ;
		while (i.hasNext())
		{
			strs[n++] = i.next().toString();
		}
		return strs;
	}

	/**
	 * This is called when module is parsed
	 */
	public void replaceModule(Module mod)
	{
		list.remove(mod);
		list.add(mod);
		// System.out.println( "number of modules : " + list.size());
	}
}

