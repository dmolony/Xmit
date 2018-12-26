# Xmit
- view the contents of xmit files
- view xmit files stored as PDS members
- view xmit files inside .zip files
- extract individual PDS members

## Sample screens
### MacOS
![Mac](resources/xmitosx.png?raw=true "Mac")
### Windows
![Windows](resources/xmitwin.png?raw=true "Windows")
### Linux
![Linux](resources/xmitlinux.png?raw=true "Linux")

## Installation
- Download and install Java and JavaFX, which are now separate downloads. You can get them
[here](https://jdk.java.net/11/) and
[here](https://gluonhq.com/products/javafx/).
- Download XmitApp.jar from the [releases](https://github.com/dmolony/xmit/releases) page.
- Create executable run file.  
  - MacOs or Unix shell file  
```/path/to/jdk-11.0.1.jdk/Contents/Home/bin/java \
--module-path /path/to/javafx-sdk-11/lib          \
--add-modules=javafx.controls,javafx.fxml         \
-Dfile.encoding=UTF-8                             \
-jar /path/to/XmitApp.jar```  

  - Windows batch file  
```C:\path\to\jdk-11.0.1\bin\java.exe            \
--module-path C:\path\to\javafx-sdk-11.0.1\lib   \
--add-modules=javafx.controls,javafx.fxml        \
-Dfile.encoding=UTF-8                            \
-jar C:\path\to\XmitApp.jar```

