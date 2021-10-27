# ModbusPal #

## Prerequisites ##

- Eclipse for Java Enterprise
- JDK 8 (https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)

## Setup ##

* Fork and clone ModbusPal library
* Open Eclipse and import existing project
  - Select MobbusPal directory
* Click Project > Properties > Java Compiler
  - Make sure Java Compiler is set to 1.8
* Click Project > Properties > Java Build Path > Libraries
  - Add Library > JRE System Library > JRE 8
  - You may setup the JRE here by clicking "Installed JREs..." and selecting the JDK installed in prereqs
    - Typically after installing the JDK on Windows the JRE will be located under `C:\Program Files\Java\jdk1.8.0_202`
* By now you should be good to compile by clicking Project > Build Project (Or if Automatically build is checked, it should have built already)
* You can run the project by clicking Run > Run
  - The first time you do this, you'll need to specify modbuspal.main.ModbusPalGui as the entrypoint in the Run Configuration

## Running ##
Assuming the correct java version is installed and you have the ModBusPal and Jython Jar files, you can start modbuspal by double clicking start_modbuspal.bat
