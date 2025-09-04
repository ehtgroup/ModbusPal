## Prerequisites ##

- Apache Netbeans (https://netbeans.apache.org/)
- JDK 17 (https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- Download ivy http://ant.apache.org/ivy/download.cgi (suggest extracting to C:\jars)

### Setup Project ###

1. Download and install netbeans https://netbeans.apache.org/download/index.html
1. Open Netbeans and select `tools > options > Java > ant` and add the ivy jar to the classpath
1. Run project

### Build Jar ###

1. Build Project (click hammer or F11)
1. Jar will be in [ProjectFolder]/dist

### Run without UI ###

You can start ModbusPal without showing the UI elements so it can run in a headless environment,
or if you just don't care to open the window with the -hide flag. You can use it in conjuction
with a project file to make modbuspal start all automations and start listening for requests using the command:
```
java -jar <<pathtojar>>/ModbusPal.jar -loadFile=<<projectfile>> -hide
```

Alternatively, if you want to leverage python scripts, you will need to ensure the dependencies are included as such:

```
java -cp "/usr/src/app/dist/ModbusPal.jar:/usr/src/app/lib/*" modbuspal.main.ModbusPalGui -loadFile=<<projectfile>> -hide

```
all slaves on headless mode will run without localhost.
