/*******************************************************************************
 * Copyright (c) 2004, 2013 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    KOBAYASHI Tadashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.parser.verilog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InstanceStore {
	
	public static class Port {
		private String name;
		private int line;
		private Expression signal;
		
		public Port(String name, int line, Expression signal) {
			this.name = name;
			this.line = line;
			this.signal = signal;
		}

		public String getName() {
			return name;
		}

		public int getLine() {
			return line;
		}

		public Expression getSignal() {
			return signal;
		}
	}

	public static class Instance {
		private String name; // module name
		private int line;
		List<Port> ports = new ArrayList<Port>();
		boolean isNamedMap = false;

		public Instance(String name, int line) {
			this.name = name;
			this.line = line;
		}
		public void addPort(Port port) {
			ports.add(port);
		}
		public Collection<Port> getPorts() {
			return ports;
		}
		public String getName() {
			return name;
		}
		public int getLine() {
			return line;
		}
		public void setNamedMap() {
			isNamedMap = true;
		}
		public boolean isNamedMap() {
			return isNamedMap;
		}
		public Port findPort(String name) {
			for(Port port : ports) {
				if (port.getName().equals(name)) {
					return port;
				}
			}
			return null;
		}
	}
	
	private List<Instance> instances = new ArrayList<Instance>();
	private Instance current = null;
	
	public InstanceStore() {
	}
	
	public void addInstance(String name, int line) {
		// The name is module name. A instance name is not needed.
		current = new Instance(name, line);
		instances.add(current);
	}
	
	public void addPort(String name, int line, Expression signal) {
		Port port = new Port(name, line, signal);
		current.addPort(port);
		if (Character.isDigit(name.charAt(0)) == false) {
			current.setNamedMap();
		}
	}
	
	public Collection<Instance> collection() {
		return instances;
	}
}
