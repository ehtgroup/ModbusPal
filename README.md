# ModbusPal #

## Prerequisites ##

- Eclipse for Java Enterprise
- Jython library from https://www.jython.org/download

## Setup ##

### Setup jfreechart ###
* Fork and clone jfreechart library and checkout v1.5.x
* Open Eclipse and add it to project workspace
* Right click project, select Build Path > Configure Build Path
  - Click Modulepath
    - Add Library > Server Runtime > Tomcat 5.5
    - Add Library > JRE System Library > JRE 16
    - Add Library > JUnit > JUnit 5
* Build project
* Export jfreechart.jar

### Setup ModbusPal ###

* Fork and clone ModbusPal library
* Add ModbusPal to Eclipse workspace
* Right click the project, select Properties > Java Compiler
  - Make sure Java Compiler is set to 1.8
* Right click project, select Build Path > Configure Build Path
  - Add Library > JRE System Library > JRE 8
  - Add External Jar > jfreechart.jar exported from step 1
  - Add External Jar > jython.jar
* Remove RXTX libraries and re-add them from the src directory

### Alternatively, you can build using Docker ###

* docker build -t modbuspal-builder .
* docker run --name modbuspal-builder modbuspal-builder
* docker cp modbuspal-builder:/usr/src/app/dist/ModbusPal.jar .
* docker cp modbuspal-builder:/usr/src/app/dist/modbuspal-help.zip .
* docker cp modbuspal-builder:/usr/src/app/dist/modbuspal-javadoc.zip .
