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
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private static final String PREFS_ROOT_FOLDER = "RootFolder";

  private Stage primaryStage;
  private String rootFolderName;

  private XmitTree xmitTree;
  private TreePane treePane;

  private final XmitTable xmitTable = new XmitTable ();
  private final OutputTabPane outputTabPane = new OutputTabPane ();

  private final FontManager fontManager = new FontManager ();
  private final FilterManager filterManager = new FilterManager ();

  private final MenuBar menuBar = new MenuBar ();
  private FileMenu fileMenu;
  private ViewMenu viewMenu;

  private final SplitPane splitPane = new SplitPane ();
  private final WindowStatus windowStatus = new WindowStatus ();

  private final List<SaveState> saveStateList = new ArrayList<> ();
  private boolean debug = false;

  // ---------------------------------------------------------------------------------//
  private Parent createContent ()
  // ---------------------------------------------------------------------------------//
  {
    Parameters parameters = getParameters ();
    for (String s : parameters.getUnnamed ())
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

    BorderPane tableBorderPane = new BorderPane ();
    BorderPane outputBorderPane = new BorderPane ();
    TableHeaderBar tableHeaderBar = new TableHeaderBar ();
    OutputHeaderBar outputHeaderBar = new OutputHeaderBar ();

    outputBorderPane.setTop (outputHeaderBar);
    outputBorderPane.setCenter (outputTabPane);
    tableBorderPane.setCenter (xmitTable);
    tableBorderPane.setTop (tableHeaderBar);

    splitPane.getItems ().addAll (treePane, tableBorderPane, outputBorderPane);
    StatusBar statusBar = new StatusBar ();

    // menus
    fileMenu = new FileMenu (this);
    viewMenu = new ViewMenu (this, fontManager, filterManager);   // listeners??

    // codepage listeners
    viewMenu.addCodePageListener (this);

    // lines listeners
    viewMenu.addShowLinesListener (statusBar);
    viewMenu.addShowLinesListener (outputHeaderBar);
    viewMenu.addShowLinesListener (outputTabPane.outputTab);

    // font change listeners
    fontManager.addFontChangeListener (xmitTable);
    fontManager.addFontChangeListener (xmitTree);
    fontManager.addFontChangeListener (outputTabPane);
    fontManager.addFontChangeListener (statusBar);

    // filter change listeners (filter parameters)
    filterManager.addFilterListener (statusBar);
    filterManager.addFilterListener (tableHeaderBar);
    filterManager.addFilterListener (outputTabPane.outputTab);
    filterManager.addFilterListener (xmitTable);

    // filter action listeners (filter results)
    xmitTable.addFilterListener (tableHeaderBar);

    // treeview listeners
    xmitTree.addListener (fileMenu);
    xmitTree.addListener (outputTabPane.headersTab);
    xmitTree.addListener (outputTabPane.hexTab);
    xmitTree.addListener (outputTabPane.blocksTab);
    xmitTree.addListener (outputTabPane.outputTab);
    xmitTree.addListener (outputHeaderBar);
    xmitTree.addListener (tableHeaderBar);
    xmitTree.addListener (xmitTable);

    // table listeners
    xmitTable.addListener (fileMenu);
    xmitTable.addListener (outputTabPane.hexTab);
    xmitTable.addListener (outputTabPane.blocksTab);
    xmitTable.addListener (outputTabPane.outputTab);
    xmitTable.addListener (outputHeaderBar);

    BorderPane mainPane = new BorderPane ();
    mainPane.setCenter (splitPane);

    // status bar
    mainPane.setBottom (statusBar);

    // add menus
    mainPane.setTop (menuBar);
    ObservableList<Menu> menus = menuBar.getMenus ();
    menus.addAll (fileMenu.getMenu (), viewMenu.getMenu ());

    fileMenu.setOutputWriter (outputTabPane.outputTab);

    // exit action
    primaryStage.setOnCloseRequest (e -> exit ());

    // ensure viewMenu (codepage) is set before xmitTree
    saveStateList.addAll (Arrays.asList (filterManager, outputTabPane, fileMenu, viewMenu,
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

    // this must happen after show()
    splitPane.setDividerPosition (0, windowStatus.dividerPosition1);
    splitPane.setDividerPosition (1, windowStatus.dividerPosition2);
  }

  // ---------------------------------------------------------------------------------//
  private void keyPressed (KeyEvent keyEvent)
  // ---------------------------------------------------------------------------------//
  {
    KeyCode keyCode = keyEvent.getCode ();

    if (keyCode == KeyCode.H || keyCode == KeyCode.B || keyCode == KeyCode.X
        || keyCode == KeyCode.O)
      outputTabPane.keyPressed (keyCode);
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
      saveState.restore (prefs);

    windowStatus.restore (prefs);

    if (windowStatus.width <= 0 || windowStatus.height <= 22 || windowStatus.x < 0
        || windowStatus.y < 0)
      setWindow ();
    else
    {
      primaryStage.setWidth (windowStatus.width);
      primaryStage.setHeight (windowStatus.height);
      primaryStage.setX (windowStatus.x);
      primaryStage.setY (windowStatus.y);
    }
  }
  //  }

  // ---------------------------------------------------------------------------------//
  private void exit ()
  // ---------------------------------------------------------------------------------//
  {
    windowStatus.setLocation (primaryStage);
    windowStatus.setDividers (splitPane);

    windowStatus.save (prefs);

    for (SaveState saveState : saveStateList)
      saveState.save (prefs);

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
    outputTabPane.setTabVisible (headersVisible, blocksVisible, hexVisible);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void selectCodePage (String codePageName)
  // ---------------------------------------------------------------------------------//
  {
    Utility.setCodePage (codePageName);     // ensure correct code page is set first
    outputTabPane.selectCodePage ();
  }

  // ---------------------------------------------------------------------------------//
  public static void main (String[] args)
  // ---------------------------------------------------------------------------------//
  {
    Application.launch (args);
  }
}
