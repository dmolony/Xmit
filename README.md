# Xmit
### Xmit File Viewer
View the contents of XMIT files.

### Sample screens
![Basic layout](resources/xmit.png?raw=true "Basic layout")

### Installation
#### Download and install Java and JavaFX
Java and JavaFX are now separate downloads. 

#### Download XmitApp.jar
Download XmitApp.jar from the [releases](https://github.com/dmolony/xmit/releases) page.

#### Create executable run file
##### MacOs or Unix
Create a shell file. Don't forget to make it executable.

```/path/to/jdk-11.0.1.jdk/Contents/Home/bin/java --module-path /path/to/javafx-sdk-11/lib --add-modules=javafx.controls,javafx.fxml -Dfile.encoding=UTF-8 -jar /path/to/XmitApp.jar```


##### Windows
Create a batch file.
```C:\path\to\jdk-11.0.1\bin\java.exe --module-path C:\path\to\javafx-sdk-11.0.1\lib --add-modules=javafx.controls,javafx.fxml -Dfile.encoding=UTF-8 -jar C:\path\to\XmitApp.jar```

