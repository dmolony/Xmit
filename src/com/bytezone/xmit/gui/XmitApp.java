package com.bytezone.xmit.gui;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Reader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class XmitApp extends Application implements TreeItemSelectionListener
{
  private static final String PREFS_ROOT_FOLDER = "RootFolder";
  private static final String PREFS_WINDOW_LOCATION = "WindowLocation";
  private static final String PREFS_DIVIDER_POSITION_1 = "DividerPosition1";
  private static final String PREFS_DIVIDER_POSITION_2 = "DividerPosition2";
  private static final String PREFS_MEMBER_INDEX = "MemberIndex";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private String rootFolderName;
  private Path rootFolderPath;

  private Stage primaryStage;
  private final BorderPane mainPane = new BorderPane ();
  private final OutputPane outputPane = new OutputPane ();

  private final MenuBar menuBar = new MenuBar ();
  private FileMenu fileMenu;

  SplitPane splitPane = new SplitPane ();
  private double dividerPosition1;
  private double dividerPosition2;

  XmitTree xmitTree;
  ListView<String> listView = new ListView<> ();
  private Reader reader;
  private final Map<Reader, String> selectedMembers = new HashMap<> ();

  // add a code page menu to allow selectable code pages
  // remember which member was previously selected for each xmit file

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

    xmitTree =
        new XmitTree (new FileTreeItem (new File ("/Users/denismolony/code/xmit")));

    xmitTree.addListener (outputPane);    // must come before ourselves
    xmitTree.addListener (this);

    listView.getSelectionModel ().selectedItemProperty ()
        .addListener ( (v, oldValue, newValue) -> memberSelected (newValue));

    xmitTree.setStyle ("-fx-font-size: 13; -fx-font-family: monospaced");
    listView.setStyle ("-fx-font-size: 13; -fx-font-family: monospaced");
    //    textArea.setStyle ("-fx-font-size: 13; -fx-font-family: monospaced");

    splitPane.getItems ().addAll (xmitTree, listView, outputPane);

    fileMenu = new FileMenu (this, xmitTree);

    mainPane.setCenter (splitPane);

    // add menus
    mainPane.setTop (menuBar);
    ObservableList<Menu> menus = menuBar.getMenus ();
    menus.add (fileMenu.getMenu ());

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
    int index = prefs.getInt (PREFS_MEMBER_INDEX, 0);
    listView.scrollTo (index);
    listView.getSelectionModel ().select (index);
    listView.getFocusModel ().focus (index);
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
    int index = listView.getSelectionModel ().getSelectedIndex ();
    prefs.putInt (PREFS_MEMBER_INDEX, index);

    //    fileMenu.exit ();
    //    editMenu.exit ();
    //    optionsMenu.exit ();
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
      //      filesTreeTable.setRootFolder (rootFolderPath);
      //      setTreePaneName ();
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
  // main
  // ---------------------------------------------------------------------------------//

  public static void main (String[] args)
  {
    Application.launch (args);
  }

  // ---------------------------------------------------------------------------------//
  // treeItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemSelected (Reader reader)
  {
    this.reader = reader;
    List<CatalogEntry> catalogEntries = reader.getCatalogEntries ();
    if (catalogEntries.size () == 0)
      listView.getItems ().clear ();
    else
    {
      ObservableList<String> items = FXCollections.observableArrayList ();
      for (CatalogEntry catalogEntry : catalogEntries)
        items.add (catalogEntry.getMemberName ());
      listView.setItems (items);
      if (selectedMembers.containsKey (reader))
      {
        String member = selectedMembers.get (reader);
        listView.scrollTo (member);
        listView.getSelectionModel ().select (member);
      }
      else
      {
        listView.scrollTo (0);
        listView.getSelectionModel ().select (0);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // treeItemExpanded
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemExpanded (TreeItem<File> treeItem)
  {
  }

  // ---------------------------------------------------------------------------------//
  // memberSelected
  // ---------------------------------------------------------------------------------//

  void memberSelected (String memberName)
  {
    outputPane.memberSelected (memberName);
    selectedMembers.put (reader, memberName);
  }
}
