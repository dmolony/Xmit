package com.bytezone.xmit.gui;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import com.bytezone.xmit.Utility;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class XmitApp extends Application implements CodePageSelectedListener
{
  private static final String PREFS_ROOT_FOLDER = "RootFolder";
  private static final String PREFS_WINDOW_LOCATION = "WindowLocation";
  private static final String PREFS_DIVIDER_POSITION_1 = "DividerPosition1";
  private static final String PREFS_DIVIDER_POSITION_2 = "DividerPosition2";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private String rootFolderName;
  private Path rootFolderPath;

  private Stage primaryStage;
  private final OutputPane outputPane = new OutputPane ();

  private final MenuBar menuBar = new MenuBar ();
  private FileMenu fileMenu;
  private ViewMenu viewMenu;

  SplitPane splitPane = new SplitPane ();
  private double dividerPosition1;
  private double dividerPosition2;

  XmitTree xmitTree;
  XmitTable xmitTable;

  // add a code page menu to allow selectable code pages

  // ---------------------------------------------------------------------------------//
  // createContent
  // ---------------------------------------------------------------------------------//

  private Parent createContent ()
  {
    // place menubar
    final String os = System.getProperty ("os.name");
    if (os != null && os.startsWith ("Mac"))
      menuBar.setUseSystemMenuBar (true);

    // get root folder
    validateRootFolderOrExit ();
    rootFolderPath = Paths.get (rootFolderName);

    xmitTree = new XmitTree (new FileTreeItem (new XmitFile (new File (rootFolderName))));
    xmitTree.addListener (outputPane);

    xmitTable = new XmitTable ();
    xmitTree.addListener (xmitTable);

    splitPane.getItems ().addAll (xmitTree, xmitTable, outputPane);

    fileMenu = new FileMenu (this, xmitTree);
    viewMenu = new ViewMenu (this, xmitTree);
    xmitTree.addListener (fileMenu);
    xmitTable.addListener (fileMenu);
    xmitTable.addListener (outputPane);
    viewMenu.addShowLinesListener (outputPane);
    viewMenu.addCodePageListener (this);

    BorderPane mainPane = new BorderPane ();
    mainPane.setCenter (splitPane);

    // add menus
    mainPane.setTop (menuBar);
    ObservableList<Menu> menus = menuBar.getMenus ();
    menus.addAll (fileMenu.getMenu (), viewMenu.getMenu ());

    // exit action
    primaryStage.setOnCloseRequest (e -> exit ());

    restore ();

    return mainPane;
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  private void restore ()
  {
    xmitTree.restore ();
    xmitTable.restore ();
    viewMenu.restore ();

    restoreWindowLocation ();
  }

  // ---------------------------------------------------------------------------------//
  // start
  // ---------------------------------------------------------------------------------//

  @Override
  public void start (Stage primaryStage) throws Exception
  {
    this.primaryStage = primaryStage;
    primaryStage.setTitle ("XmitApp");
    primaryStage.setScene (new Scene (createContent ()));
    primaryStage.show ();

    splitPane.setDividerPosition (0, dividerPosition1);      // must happen after show()
    splitPane.setDividerPosition (1, dividerPosition2);
  }

  // ---------------------------------------------------------------------------------//
  // restoreWindowLocation
  // ---------------------------------------------------------------------------------//

  private void restoreWindowLocation ()
  {
    dividerPosition1 = prefs.getDouble (PREFS_DIVIDER_POSITION_1, .33);
    dividerPosition2 = prefs.getDouble (PREFS_DIVIDER_POSITION_2, .67);
    String windowLocation = prefs.get (PREFS_WINDOW_LOCATION, "");

    if (windowLocation.isEmpty ())
      setWindow ();
    else
    {
      String[] chunks = windowLocation.split (",");
      double width = Double.parseDouble (chunks[0]);
      double height = Double.parseDouble (chunks[1]);
      double x = Double.parseDouble (chunks[2]);
      double y = Double.parseDouble (chunks[3]);

      if (width <= 0 || height <= 22 || x < 0 || y < 0)
        setWindow ();
      else
      {
        primaryStage.setWidth (width);
        primaryStage.setHeight (height);
        primaryStage.setX (x);
        primaryStage.setY (y);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // setWindow
  // ---------------------------------------------------------------------------------//

  private void setWindow ()
  {
    primaryStage.setWidth (1200);
    primaryStage.setHeight (800);
    primaryStage.centerOnScreen ();
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  private void exit ()
  {
    double width = primaryStage.getWidth ();
    double height = primaryStage.getHeight ();
    double x = primaryStage.getX ();
    double y = primaryStage.getY ();

    if (width > 100 && height > 100)
    {
      String text = String.format ("%f,%f,%f,%f", width, height, x, y);
      prefs.put (PREFS_WINDOW_LOCATION, text);
    }

    double[] positions = splitPane.getDividerPositions ();
    prefs.putDouble (PREFS_DIVIDER_POSITION_1, positions[0]);
    prefs.putDouble (PREFS_DIVIDER_POSITION_2, positions[1]);

    xmitTree.exit ();
    xmitTable.exit ();

    fileMenu.exit ();
    viewMenu.exit ();
    outputPane.exit ();

    Platform.exit ();
  }

  // ---------------------------------------------------------------------------------//
  // changeRootFolder
  // ---------------------------------------------------------------------------------//

  void changeRootFolder ()
  {
    if (setRootFolder ())
    {
      rootFolderPath = Paths.get (rootFolderName);
      xmitTree
          .setRootFolder (new FileTreeItem (new XmitFile (new File (rootFolderName))));
    }
  }

  // ---------------------------------------------------------------------------------//
  // validateRootFolderOrExit
  // ---------------------------------------------------------------------------------//

  private void validateRootFolderOrExit ()
  {
    rootFolderName = prefs.get (PREFS_ROOT_FOLDER, "");
    if (!rootFolderName.isEmpty ())
    {
      File file = new File (rootFolderName);
      if (!file.exists ())
        rootFolderName = "";
    }

    if (rootFolderName.isEmpty () && !setRootFolder ())
      Platform.exit ();
  }

  // ---------------------------------------------------------------------------------//
  // setRootFolder
  // ---------------------------------------------------------------------------------//

  private boolean setRootFolder ()
  {
    DirectoryChooser directoryChooser = new DirectoryChooser ();
    directoryChooser.setTitle ("Set root folder");
    directoryChooser.setInitialDirectory (new File (System.getProperty ("user.home")));

    File file = directoryChooser.showDialog (null);
    if (file != null && file.isDirectory ())
    {
      rootFolderName = file.getAbsolutePath ();
      prefs.put (PREFS_ROOT_FOLDER, rootFolderName);
      return true;
    }

    return false;
  }

  // ---------------------------------------------------------------------------------//
  // setTabVisible
  // ---------------------------------------------------------------------------------//

  void setTabVisible (boolean metaVisible, boolean debugVisible)
  {
    outputPane.setTabVisible (metaVisible, debugVisible);
  }

  // ---------------------------------------------------------------------------------//
  // selectCodePage
  // ---------------------------------------------------------------------------------//

  @Override
  public void selectCodePage (String codePageName)
  {
    Utility.setCodePage (codePageName);
    outputPane.selectCodePage ();
  }

  // ---------------------------------------------------------------------------------//
  // main
  // ---------------------------------------------------------------------------------//

  public static void main (String[] args)
  {
    Application.launch (args);
  }
}
