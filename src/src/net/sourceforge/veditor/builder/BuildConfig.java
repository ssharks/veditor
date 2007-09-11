/*******************************************************************************
 * Copyright (c) 2006 Ali Ghorashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali Ghorashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.sourceforge.veditor.HdlNature;


/**
 * Class encapsulating command arguments
 */
public class BuildConfig extends Object{
    static final String ENABLE_STR     = HdlNature.SIMULATOR_ID+".%s.enable";
    static final String COMMAND_STR    = HdlNature.SIMULATOR_ID+".%s.command";
    static final String CLEAN_CMD_STR  = HdlNature.SIMULATOR_ID+".%s.CleanCommand";
    static final String PARSER_STR     = HdlNature.SIMULATOR_ID+".%s.parser";
    static final String WORK_FOLDER_STR= HdlNature.SIMULATOR_ID+".%s.workFolder";
    static final String BUILD_ORDER_STR= HdlNature.SIMULATOR_ID+".%s.buildOrder";
    static final String NAME_STR       = HdlNature.SIMULATOR_ID+".%s.name";
	
	Boolean m_Enabled;
	String  m_Name;
	String  m_Command;
	String  m_CleanCommand;
	String  m_Parser;
	String  m_WorkFolder;
	Integer m_BuildOrder;		
	
	/**
	 * Creates a copy of this class
	 */
	public BuildConfig clone(){
		return new BuildConfig(m_Name,m_Enabled,m_BuildOrder,m_Command,m_CleanCommand,m_Parser,m_WorkFolder);
	}
	/**
	 * Default constructor
	 */		
	public BuildConfig(){
		m_Enabled=true;
		m_Command="echo 'No Build Configuration Specified'";
		m_CleanCommand="echo 'Clean'";
		m_Parser="";
		m_WorkFolder="";
		m_BuildOrder=0;		
		m_Name="Default";
	}
	/**
	 * Constructor
	 * @param enable Enable
	 * @param buildOrder build order
	 * @param cmd    Command
	 * @param args   Command arguments
	 * @param parser Error parser
	 * @param folder Work folder
	 */
	public BuildConfig(String name,boolean enable,int buildOrder,String cmd, String cleanCmd, String parser,String folder){
		m_Enabled=enable;
		m_Command=cmd;
		m_CleanCommand=cleanCmd;
		m_Parser=parser;
		m_WorkFolder=folder;
		m_BuildOrder=buildOrder;
		m_Name=name;
	}
	/**
	 * Converts the values into a map of name-value pair
	 * @return
	 */
	public void addValuesToMap(HashMap<String,String> map){
		
		//put code here to format the name like 0000000NAME
		
		map.put(String.format(ENABLE_STR,m_BuildOrder+m_Name), m_Enabled.toString());
		map.put(String.format(COMMAND_STR,m_BuildOrder+m_Name), m_Command);
		map.put(String.format(CLEAN_CMD_STR,m_BuildOrder+m_Name), m_CleanCommand);
		map.put(String.format(PARSER_STR,m_BuildOrder+m_Name), m_Parser);
		map.put(String.format(WORK_FOLDER_STR,m_BuildOrder+m_Name), m_WorkFolder);
		map.put(String.format(BUILD_ORDER_STR,m_BuildOrder+m_Name), m_BuildOrder.toString());
		map.put(String.format(NAME_STR,m_BuildOrder+m_Name), m_Name);

	}		
	/**
	 * Sets the internal parameters using the name-value pair
	 * @param map
	 * @param name the name of this element
	 */
	public void setValues(Map<String,String> map,String name){
		m_Enabled=Boolean.parseBoolean(map.get(String.format(ENABLE_STR,name)));
		m_Command=map.get(String.format(COMMAND_STR,name));
		m_CleanCommand=map.get(String.format(CLEAN_CMD_STR,name));
		m_Parser=map.get(String.format(PARSER_STR,name));
		m_WorkFolder=map.get(String.format(WORK_FOLDER_STR,name));
		m_BuildOrder=Integer.parseInt(map.get(String.format(BUILD_ORDER_STR,name)));
		m_Name=map.get(String.format(NAME_STR,name));
	}

	public boolean isEnabled() {return m_Enabled;}
	public String getCommand() {return m_Command; }
	public String getCleanCommand() {return m_CleanCommand; }
	public String getParser() {return m_Parser;	}
	public String getWorkFolder() {	return m_WorkFolder;	}		
	public int getBuildOrder(){ return m_BuildOrder;}
	public String getName(){return m_Name;}
	public void setEnabled(Boolean enabled) {m_Enabled = enabled;}
	public void setName(String name) {m_Name = name;	}
	public void setCommand(String command) {	m_Command = command;}
	public void setCleanCommand(String command) {	m_CleanCommand = command;}
	public void setParser(String parser) {m_Parser = parser;	}
	public void setWorkFolder(String workFolder) {m_WorkFolder = workFolder;	}
	public void setBuildOrder(Integer buildOrder) {m_BuildOrder = buildOrder;}
	
	
	/**
	 * Parses a projects argument list to commands
	 * @param args
	 * @return A map between configuration names and build configs
	 */
	public static Map<String,BuildConfig> parseCommandArguments(Map<String,String> args){
		HashSet<String> names=new HashSet<String>();
		Map<String,BuildConfig> configs=new HashMap<String,BuildConfig>();
		
		//loop though all the keys and pick out the names
		for(String argName:args.keySet().toArray(new String[0])){
			if(argName.startsWith(HdlNature.SIMULATOR_ID)){
				String []fields=argName.split("\\.");
				String name=fields[fields.length-2];
				//have we seen this name?
				if(names.contains(name) == false){
					names.add(name);
				}
			}
		}
		//loop though all the names and create the items
		for(String name:names){
			BuildConfig command=new BuildConfig();
			command.setValues(args, name);
			configs.put(name, command);			
		}
		
		return configs;
	}
	
	/**
	 * Encodes the list commands into one argument list
	 * @param commands
	 * @return
	 */
	public static Map<String,String> encodeArgs(BuildConfig []commands){
		HashMap<String,String> results=new HashMap<String,String>();
		
		for(BuildConfig command:commands){
			command.addValuesToMap(results);
		}
		
		return results;
	}
}
