# ModbusPal #

## Prerequisites ##

- Apache Netbeans

### Setup Project ###

1. Download and install netbeans https://netbeans.apache.org/download/index.html
2. Download ivy http://ant.apache.org/ivy/download.cgi
3. Open netbeans and select tools->options->ant and add the ivy jar to the classpath
4. Run project

### Alternatively, you can build using Docker ###

* docker build -t modbuspal-builder .
* docker run --name modbuspal-builder modbuspal-builder
* docker cp modbuspal-builder:/usr/src/app/dist/ModbusPal.jar .
* docker cp modbuspal-builder:/usr/src/app/dist/modbuspal-javadoc.zip .