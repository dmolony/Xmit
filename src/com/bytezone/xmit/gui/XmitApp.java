package com.bytezone.xmit.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.xmit.Utility;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

// ---------------------------------------------------------------------------------//
public class XmitApp extends Application implements CodePageSelectedListener
//---------------------------------------------------------------------------------//
{
  private static String[] args;

  private static final String PREFS_ROOT_FOLDER = "RootFolder";
  private static final String PREFS_WINDOW_LOCATION = "WindowLocation";
  private static final String PREFS_DIVIDER_POSITION_1 = "DividerPosition1";
  private static final String PREFS_DIVIDER_POSITION_2 = "DividerPosition2";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private Stage primaryStage;
  private String rootFolderName;

  private XmitTree xmitTree;
  private final XmitTable xmitTable = new XmitTable ();

  private TreePane treePane;
  private final TablePane tablePane = new TablePane (xmitTable);
  private final OutputPane outputPane = new OutputPane ();

  private final FontManager fontManager = new FontManager ();
  private final FilterManager filterManager = new FilterManager ();

  private final MenuBar menuBar = new MenuBar ();
  private FileMenu fileMenu;
  private ViewMenu viewMenu;

  private final SplitPane splitPane = new SplitPane ();
  private double dividerPosition1;
  private double dividerPosition2;

  private final List<SaveState> saveStateList = new ArrayList<> ();
  public boolean debug = false;

  private final StatusBar statusBar = new StatusBar ();

  // ---------------------------------------------------------------------------------//
  private Parent createContent ()
  // ---------------------------------------------------------------------------------//
  {
    for (String s : args)
      if ("-debug".equals (s))
        debug = true;

    // place menubar
    final String os = System.getProperty ("os.name");
    if (os != null && os.startsWith ("Mac"))
      menuBar.setUseSystemMenuBar (true);

    // get root folder
    validateRootFolderOrExit ();

    xmitTree = new XmitTree (new FileTreeItem (new XmitFile (new File (rootFolderName))));
    treePane = new TreePane (xmitTree);

    splitPane.getItems ().addAll (treePane, tablePane, outputPane);

    // menus
    fileMenu = new FileMenu (this, xmitTree);
    viewMenu = new ViewMenu (this, fontManager, filterManager);

    // codepage listeners
    viewMenu.addCodePageListener (this);

    // lines listeners
    viewMenu.addShowLinesListener (outputPane);
    viewMenu.addShowLinesListener (statusBar);

    // font change listeners
    fontManager.addFontChangeListener (outputPane);
    fontManager.addFontChangeListener (xmitTable);
    fontManager.addFontChangeListener (xmitTree);

    // filter change listeners
    filterManager.addFilterListener (statusBar);
    //    filterManager.addFilterListener (tablePane.tableHeaderBar);
    filterManager.addFilterListener (xmitTable);
    filterManager.addFilterListener (outputPane);

    // filter action listeners
    xmitTable.addFilterListener (statusBar);

    // treeview listeners
    xmitTree.addListener (fileMenu);
    xmitTree.addListener (outputPane);
    xmitTree.addListener (outputPane.outputHeaderBar);
    xmitTree.addListener (tablePane.tableHeaderBar);
    xmitTree.addListener (xmitTable);
    xmitTree.addListener (statusBar);

    // table listeners
    xmitTable.addListener (fileMenu);
    xmitTable.addListener (viewMenu);
    xmitTable.addListener (outputPane);
    xmitTable.addListener (statusBar);

    BorderPane mainPane = new BorderPane ();
    mainPane.setCenter (splitPane);

    // status bar
    mainPane.setBottom (statusBar);
    if (args.length > 0)
      statusBar.setText ("Args: " + args[0]);

    // add menus
    mainPane.setTop (menuBar);
    ObservableList<Menu> menus = menuBar.getMenus ();
    menus.addAll (fileMenu.getMenu (), viewMenu.getMenu ());

    fileMenu.setOutputWriter (outputPane);

    // exit action
    primaryStage.setOnCloseRequest (e -> exit ());

    // ensure viewMenu (codepage) is set before xmitTree
    saveStateList.addAll (Arrays.asList (filterManager, outputPane, fileMenu, viewMenu,
        xmitTree, xmitTable, fontManager));

    restore ();

    return mainPane;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void start (Stage primaryStage) throws Exception
  // ---------------------------------------------------------------------------------//
  {
    this.primaryStage = primaryStage;
    primaryStage.setTitle ("XmitApp");
    Scene scene = new Scene (createContent ());
    primaryStage.setScene (scene);

    scene.setOnKeyPressed (e -> keyPressed (e));

    //    if (false)
    //    {
    //      Desktop desktop = Desktop.getDesktop ();
    //      System.out.println (Desktop.isDesktopSupported ());
    //      System.out.println (desktop.isSupported (Action.APP_ABOUT));
    //      desktop.setAboutHandler (e -> squawk ("About dialog"));
    //      desktop.setPreferencesHandler (e -> squawk ("Preferences dialog"));
    //      desktop.setQuitHandler ( (e, r) -> squawk ("Quit dialog"));
    //    }

    primaryStage.show ();

    splitPane.setDividerPosition (0, dividerPosition1);      // must happen after show()
    splitPane.setDividerPosition (1, dividerPosition2);
  }

  // ---------------------------------------------------------------------------------//
  private void keyPressed (KeyEvent keyEvent)
  // ---------------------------------------------------------------------------------//
  {
    KeyCode keyCode = keyEvent.getCode ();

    if (keyCode == KeyCode.H || keyCode == KeyCode.B || keyCode == KeyCode.X
        || keyCode == KeyCode.O)
      outputPane.keyPressed (keyCode);
    else if (keyCode == KeyCode.COMMA || keyCode == KeyCode.PERIOD)
      fontManager.keyPressed (keyEvent);
    else if (keyCode == KeyCode.F)
      filterManager.keyPressed (keyEvent);
  }

  // ---------------------------------------------------------------------------------//
  private void setWindow ()
  // ---------------------------------------------------------------------------------//
  {
    primaryStage.setWidth (1200);
    primaryStage.setHeight (800);
    primaryStage.centerOnScreen ();
  }

  // ---------------------------------------------------------------------------------//
  private void restore ()
  // ---------------------------------------------------------------------------------//
  {
    for (SaveState saveState : saveStateList)
      saveState.restore ();

    dividerPosition1 = prefs.getDouble (PREFS_DIVIDER_POSITION_1, .33);
    dividerPosition2 = prefs.getDouble (PREFS_DIVIDER_POSITION_2, .67);
    String windowLocation = prefs.get (PREFS_WINDOW_LOCATION, "");

    if (debug)
    {
      System.out.println ("Restoring dividers");
      System.out.printf ("  Div1: %f%n", dividerPosition1);
      System.out.printf ("  Div2: %f%n", dividerPosition2);
    }

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
  private void exit ()
  // ---------------------------------------------------------------------------------//
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

    if (debug)
    {
      System.out.println ("Saving dividers");
      System.out.printf ("  Div1: %f%n", positions[0]);
      System.out.printf ("  Div2: %f%n", positions[1]);
    }

    for (SaveState saveState : saveStateList)
      saveState.save ();

    Platform.exit ();
  }

  // ---------------------------------------------------------------------------------//
  void changeRootFolder ()
  // ---------------------------------------------------------------------------------//
  {
    if (setRootFolder ())
      treePane
          .setRootFolder (new FileTreeItem (new XmitFile (new File (rootFolderName))));
  }

  // ---------------------------------------------------------------------------------//
  private void validateRootFolderOrExit ()
  // ---------------------------------------------------------------------------------//
  {
    rootFolderName = prefs.get (PREFS_ROOT_FOLDER, "");
    if (rootFolderName.isEmpty ())
    {
      Utility.showAlert (AlertType.INFORMATION, "XMIT folder",
          "The XMIT file folder has not yet been defined. Please choose the "
              + "TOP LEVEL FOLDER where you store your XMIT files. This folder "
              + "may contain subfolders. It can also be changed at any time.");
    }
    else
    {
      File file = new File (rootFolderName);
      if (!file.exists ())
        rootFolderName = "";
    }

    if (rootFolderName.isEmpty () && !setRootFolder ())
    {
      Platform.exit ();
      System.exit (0);
    }
  }

  // ---------------------------------------------------------------------------------//
  private boolean setRootFolder ()
  // ---------------------------------------------------------------------------------//
  {
    DirectoryChooser directoryChooser = new DirectoryChooser ();
    directoryChooser.setTitle ("Set XMIT file folder");

    String previousRootFolderName = prefs.get (PREFS_ROOT_FOLDER, "");
    if (previousRootFolderName.isEmpty ())
      directoryChooser.setInitialDirectory (new File (System.getProperty ("user.home")));
    else
      directoryChooser.setInitialDirectory (new File (previousRootFolderName));

    File file = directoryChooser.showDialog (null);
    //    System.out.println (file);
    if (file != null && file.isDirectory ())
    {
      rootFolderName = file.getAbsolutePath ();
      prefs.put (PREFS_ROOT_FOLDER, rootFolderName);
      return true;
    }

    return false;
  }

  // ---------------------------------------------------------------------------------//
  void setTabVisible (boolean headersVisible, boolean blocksVisible, boolean hexVisible)
  // ---------------------------------------------------------------------------------//
  {
    outputPane.setTabVisible (headersVisible, blocksVisible, hexVisible);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void selectCodePage (String codePageName)
  // ---------------------------------------------------------------------------------//
  {
    Utility.setCodePage (codePageName);     // ensure correct code page is set first
    outputPane.selectCodePage ();
  }

  // ---------------------------------------------------------------------------------//
  public static void main (String[] args)
  // ---------------------------------------------------------------------------------//
  {
    XmitApp.args = args;
    Application.launch (args);
  }
}
