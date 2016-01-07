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

package net.sourceforge.veditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.veditor.preference.PreferenceStrings;
import net.sourceforge.veditor.templates.VerilogInModuleContextType;
import net.sourceforge.veditor.templates.VerilogInStatementContextType;
import net.sourceforge.veditor.templates.VerilogNewFileContext;
import net.sourceforge.veditor.templates.VerilogOutModuleContextType;
import net.sourceforge.veditor.templates.VhdlGlobalContext;
import net.sourceforge.veditor.templates.VhdlNewFileContext;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class
 */
public class VerilogPlugin extends AbstractUIPlugin
{
	private static final String CONSOLE_NAME = "veditor";
	private static VerilogPlugin plugin;
	private static final String INTERNALMARKER_TYPE = "org.eclipse.core.resources.problemmarker";
	private static final String EXTERNALMARKER_TYPE = "net.sourceforge.veditor.builderproblemmarker";
	private static final String AUTO_TASK_MARKER = "net.sourceforge.veditor.autotaskmarker";
	private static final String OUTLINE_DATABASE_ID = "OutlineDatabase";
	private static final String COLLAPSIBLE_PROPERTY_ID = "collapsible";
	private static final String HIERARCHY_ID = "Hierarchy";
	private static final String CUSTTOM_TEMPLATES_PREFERENCE_NAME = "net.sourceforge.veditor.templatesStore";	
	protected TemplateStore templateStore;
	protected ContributionContextTypeRegistry contextTypeRegistry;
	public static final String ID="net.sourceforge.veditor";

	public VerilogPlugin()
	{
		super();
		plugin = this;		
		templateStore=null;
		contextTypeRegistry=null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static VerilogPlugin getPlugin()
	{
		return plugin;
	}
	/**
	 * 
	 * @return Database Outline id
	 */
	public static QualifiedName getOutlineDatabaseId(){
		return new QualifiedName(ID,OUTLINE_DATABASE_ID);
	}
	public static QualifiedName getCollapsibleId(){
		return new QualifiedName(ID,COLLAPSIBLE_PROPERTY_ID);
	}
	public static QualifiedName getHierarchyId(){
		return new QualifiedName(ID,HIERARCHY_ID);
	}

	/**
	 * Returns PreferenceStore
	 */
	public static IPreferenceStore getStore()
	{
		return getPlugin().getPreferenceStore();
	}
	
	/**
	 * Gets the image descriptor for a given image name
	 * @param imageName image file name
	 * @return Image descriptor 
	 */
	public ImageDescriptor getImageDescriptor(String imageName){
		return VerilogPlugin.imageDescriptorFromPlugin(	VerilogPlugin.ID, imageName);
	}
	/** 
	 * Returns an image from the registery
	 * @param imageName the name of the image file
	 */
	public Image getImage(String imageName) {
		VerilogPlugin plugin = VerilogPlugin.getPlugin();
		ImageRegistry registry = plugin.getImageRegistry();
		Image results = null;

		results = registry.get(imageName);
		results = registry.get(imageName);
		if (results == null) {
			ImageDescriptor desc = getImageDescriptor(imageName);
			registry.put(imageName, desc);
			results = registry.get(imageName);
		}		
		return results;
	}
	
	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace()
	{
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin preferences
	 */
	public static String getPreferenceString(String key)
	{		
		if ( getStore().contains(key) ){
			return getStore().getString(key);
		}
		else{
			return getStore().getDefaultString(key);
		}
	}

	/**
	 * Returns the string from the plugin preferences
	 */
	public static boolean getPreferenceBoolean(String key)
	{
		if ( getStore().contains(key) ){
			return getStore().getBoolean(key);
		}
		else{
			return getStore().getDefaultBoolean(key);
		}
	}
	
	/**
	 * Returns the RGB from the plugin preferences
	 */
	public static RGB getPreferenceColor(String key)
	{
		return PreferenceConverter.getColor(getStore(), key);
	}
	
	/**
	 * Returns the string list separated by "\n" from the plugin preferences
	 */
	public static List<String> getPreferenceStrings(String key)
	{
		String string = getPreferenceString(key);
		if (string == null)
			return null;

		int index = string.indexOf('\n');
		if (index >= 0)
		{
			// check version number
			if (!string.substring(0, index).equals("1"))
				return null;
		}
	
		List<String> list = new ArrayList<String>();
		int length = string.length();
		while(index >= 0 && index < length - 1)
		{
			int next = string.indexOf('\n', index + 1);
			if (next >= 0)
			{
				list.add(string.substring(index + 1, next));
			}
			index = next;
		}
		return list;
	}
	
	/**
	 * set the string to the plugin preferences
	 */
	public static void setPreference(String key, String value)
	{
		getStore().setValue(key, value);
	}

	/**
	 * set the string to the plugin preferences
	 */
	public static void setPreference(String key, boolean value)
	{
		getStore().setValue(key, value);
	}
	
	/**
	 * set the RGB to the plugin preferences
	 */
	public static void setPreference(String key, RGB rgb)
	{
		PreferenceConverter.setValue(getStore(), key, rgb);
	}
	
	/**
	 * set the string list separated by "\n"
	 */
	public static void setPreference(String key, List<String> list)
	{
		StringBuffer value = new StringBuffer("1\n");
		Iterator<String> i = list.iterator();
		while(i.hasNext())
		{
			value.append(i.next().toString());
			value.append("\n");
		}
		setPreference(key, value.toString());
	}
	
	/**
	 * initialize default
	 */
	public static void setDefaultPreference(String key)
	{
		getStore().setToDefault(key);
	}

	/**
	 * Show message in console view
	 */
	public static void println(String msg)
	{
		MessageConsoleStream out = findConsole(CONSOLE_NAME).newMessageStream();
		out.println(msg);
	}
	
	/**
	 * FIXME:
	 * I cannot use clearConsole!
	 * When clearConsole is called, println is ignored
	 */
	public static void clear()
	{
		findConsole(CONSOLE_NAME).clearConsole();
	}
	
	private static MessageConsole findConsole(String name)
	{
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] consoles = man.getConsoles();
		for (int i = 0; i < consoles.length; i++)
		{
			if (consoles[i].getName().equals(name))
				return (MessageConsole)consoles[i];
		}

		// if not exists, add new console
		MessageConsole newConsole = new MessageConsole(name, null);
		man.addConsoles(new IConsole[]{newConsole});
		return newConsole;
	}
	
	public static void removePatternMatchListener(IPatternMatchListener list) {
		findConsole(CONSOLE_NAME).removePatternMatchListener(list);
	}
	
	public static void addPatternMatchListener(IPatternMatchListener list) {
		findConsole(CONSOLE_NAME).addPatternMatchListener(list);
	}
	

	public static void setErrorMarker(IResource file, int lineNumber, String msg)
	{
		setProblemMarker(file, IMarker.SEVERITY_ERROR, lineNumber, msg);
	}

	public static void setWarningMarker(IResource file, int lineNumber,
			String msg)
	{
		if (getPreferenceBoolean("Warning"))
			setProblemMarker(file, IMarker.SEVERITY_WARNING, lineNumber, msg);
	}

	public static void setInfoMarker(IResource file, int lineNumber, String msg)
	{
		setProblemMarker(file, IMarker.SEVERITY_INFO, lineNumber, msg);
	}

	/**
	 * Creates a task marker
	 * @param file The resource that needs the task added
	 * @param lineNumber The line number of the task
	 * @param msg Task message
	 */
	public static void setTaskMarker(IResource file,int lineNumber,String msg,int priority){
	    //if there's already a marker at this location, remove it
	    clearAutoTaskMarker(file,lineNumber);
	    try {
            IMarker marker = createAutoMarker(file);
            marker.setAttribute(IMarker.MESSAGE, msg);
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
            marker.setAttribute(IMarker.PRIORITY,priority);
            marker.setAttribute(IMarker.USER_EDITABLE, false);
        } catch (CoreException e) {           
            e.printStackTrace();
        }
	    
	}
	
	public static IMarker createAutoMarker(IResource file){
	    try {
	         IMarker marker = file.createMarker(AUTO_TASK_MARKER);
	         return marker;
	      } catch (CoreException e) {
	         // You need to handle the cases where attribute value is rejected
	          e.printStackTrace();
	      }
	      return null;
	}
	/**
	 * Removes a task marker from the given file and line number
	 * @param file The resource that has the task in it
	 * @param lineNumber The line number of the task
	 */
	public static void clearAutoTaskMarker(IResource file,int lineNumber){
	    IMarker[] markers;
        try {
            markers = file.findMarkers(AUTO_TASK_MARKER, true, IResource.DEPTH_INFINITE);
            for (int i = 0; i < markers.length; i++){                
                Integer tempLineNumber = (Integer)markers[i].getAttribute(IMarker.LINE_NUMBER);
                //if the line numbers match, remove the marker
                if(tempLineNumber == lineNumber){
                    markers[i].delete();
                    break;
                }
            }
                
        } catch (CoreException e) {           
            e.printStackTrace();
        }
        
	}
	
	/**
	 * Removes all the tasks from the file
	 * @param file The file to remove the tasks from
	 */
	public static void clearAllAutoTaskMarkers(IResource file){
	    IMarker [] markers;
        try {
            markers = file.findMarkers(AUTO_TASK_MARKER, false, IResource.DEPTH_INFINITE);
            for (int i = 0; i < markers.length; i++){        
                markers[i].delete();       
                
            }                
        } catch (CoreException e) {           
            e.printStackTrace();
        }
	}

	public static void setProblemMarker(IResource file, int level,
			int lineNumber, String msg)
	{
		try
		{
			IMarker marker = file.createMarker(INTERNALMARKER_TYPE);
			marker.setAttribute(IMarker.SEVERITY, level);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.MESSAGE, msg);
		}
		catch (CoreException e)
		{
		}
	}

	public static void setExternalProblemMarker(IResource file, int level,
			int lineNumber, String msg)
	{
		try
		{
			IMarker marker = file.createMarker(EXTERNALMARKER_TYPE);
			marker.setAttribute(IMarker.SEVERITY, level);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.MESSAGE, msg);
		}
		catch (CoreException e)
		{
		}
	}
	
	public static void deleteMarkers(IResource project) {
		try {
			project.deleteMarkers(INTERNALMARKER_TYPE, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
		}
	}

	public static void deleteExternalMarkers(IResource project) {
		try {
			project.deleteMarkers(EXTERNALMARKER_TYPE, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
		}
	}
	/**
	 * Returns this plug-in's template store.
	 * 
	 * @return the template store of this plug-in instance
	 */
	public TemplateStore getTemplateStore() {
		if (templateStore == null) {
			templateStore= new ContributionTemplateStore(getContextTypeRegistry(), getPlugin().getPreferenceStore(), CUSTTOM_TEMPLATES_PREFERENCE_NAME);
			try {
				templateStore.load();
			} catch (IOException e) {
				getPlugin().getLog().log(new Status(IStatus.ERROR, "net.sourceforge.veditor", IStatus.OK, "", e)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return templateStore;
	}

	/**
	 * Returns this plug-in's context type registry.
	 * 
	 * @return the context type registry for this plug-in instance
	 */
	public ContextTypeRegistry getContextTypeRegistry() {
		if (contextTypeRegistry == null) {
			// create an configure the contexts available in the template editor
			contextTypeRegistry= new ContributionContextTypeRegistry();

			contextTypeRegistry.addContextType( VhdlNewFileContext.CONTEXT_TYPE );
			contextTypeRegistry.addContextType( VerilogNewFileContext.CONTEXT_TYPE );
			contextTypeRegistry.addContextType(VhdlGlobalContext.CONTEXT_TYPE);
			contextTypeRegistry.addContextType(VerilogInStatementContextType.CONTEXT_TYPE);
			contextTypeRegistry.addContextType(VerilogInModuleContextType.CONTEXT_TYPE);
			contextTypeRegistry.addContextType(VerilogOutModuleContextType.CONTEXT_TYPE);
		}
		return contextTypeRegistry;
	}
	
	/**
	 * Aligns a string on the given character
	 * @param s The string to align
	 * @param c Character to align on
	 * @param count nth occurrence of the character
	 * @return The aligned string
	 */
	public static String alignOnChar(String s,char c,int count){
		
		String lines[]=s.split("\n");
		String results="";
		int maxOffset=0,index,index_count;
		boolean useSpaceForTab;
		boolean alignOnTab = VerilogPlugin.getPreferenceBoolean( PreferenceStrings.ALIGNONTAB );
		int indentSize= Integer.parseInt(VerilogPlugin.getPreferenceString("Style.indentSize"));
		String indent = VerilogPlugin.getPreferenceString("Style.indent");
		if (indent.equals("Tab"))
			useSpaceForTab=false;
		else
		{
			useSpaceForTab=true;
		}
		
		//find the offset the of character
		for(int nLine=0; nLine < lines.length;nLine++){
			index=0;
			index_count=0;
			//search for the Nth occurrence
			while(index !=-1 && index_count < count){
				index=lines[nLine].indexOf(c, index);
				index_count++;
			}
			if(index > maxOffset){
				maxOffset=index;
			}
		}
		
		// when alignment on tabs is activated, together with the space requirement, recalculate the maxOffset
		if (alignOnTab && useSpaceForTab) {
			maxOffset = (((maxOffset - 2)/indentSize) + 1) * indentSize + 1; 
		}
		
		//now align
		for(int nLine=0; nLine < lines.length;nLine++){				
			index=0;
			index_count=0;
			//search for the Nth occurrence
			while(index !=-1 && index_count < count){
				index=lines[nLine].indexOf(c, index);
				index_count++;
			}
			if(index != -1){
				String padding="";
				for(int j=index;j<maxOffset;j++){
					padding+=" ";
				}
				lines[nLine]=lines[nLine].substring(0, index) + 
						     padding + 
						     lines[nLine].substring(index, lines[nLine].length());
			}
		}
		//assemble the lines
		for(int nLine=0; nLine < lines.length;nLine++){
			results+=lines[nLine]+"\n";
		}
		return results;
	}

	public static String getIndentationString() {
		String indentationstring = "";
		if (VerilogPlugin.getPreferenceString("Style.indent").equals("Tab"))
			indentationstring = "\t";
		else
		{
			int size = Integer.parseInt(VerilogPlugin.getPreferenceString("Style.indentSize"));
			for(int i=0;i<size;i++){
				indentationstring+=" ";
			}		
		}
		return indentationstring;
	}
}

