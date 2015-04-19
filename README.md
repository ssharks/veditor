# Veditor

Veditor is a plugin for Eclipse for Verilog and VHDL. Some of the most important features:
  * Syntax highlighting
  * Auto completion
  * Some syntax checking
  * Code formatting
  
## Installation
First you need an Eclipse installation. This is a .zip file, that you only have to extract. Make sure that when you have installed a 32-bit Java, you download the 32-bit Eclipse! 
I prefer starting off with Eclipse CDT, since you will more likely do C as Java.

There are numerous ways to install a plugin in Eclipse. The easiest method I find is taking the .jar file from the .zip file and placing it in the dropins folder of your Eclipse install. After a reboot of Eclipse, you must be able to make a VHDL/Verilog project. 
Congrats, your Veditor is installed successfully, happy editing.

## Usage

### Creating a project
Veditor is a plugin that aids your development, therefore the usage way I like the most is to make a VHDL/Verilog project at the root of the sources of my project instead of the workspace of Eclipse:
In Eclipse choose: File -> New -> Project...
Select VHDL/Verilog -> VHDL/Verilog Project
Choose a project name, and instead of using the default project location. Select the location of your VHDL files.

## Building Veditor from sources
To develop Veditor, you need the following elements:
  * Eclipse PDE (Plugin Development Edition):  https://eclipse.org/pde/
  * Java JDK
  * JavaCC: https://javacc.java.net/
  * JavaCC plugin (optional): http://marketplace.eclipse.org/content/javacc-eclipse-plug

### Steps
* After installation of Eclipse and the Java JDK, select it as JRE in Eclipse: Preferences -> Java -> Installed JRE.
* Checkout the source
* Import the projects from the sources: File -> Import... -> Existing projects into workspace, choose at least the veditor project
* Choose Project -> Build Project (Note: you need two build passes from a clean archive)

### Debugging
Using the normal debug function you can debug the veditor plugin, Eclipse will start a new instance in debugging mode
### Releasing
Open plugin.xml in the veditor project and select the Overview page. Using the Export Wizard, you can make a .zip file distribution with your version of Veditor
