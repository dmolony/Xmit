package com.bytezone.xmit.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Reader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class XmitApp extends Application
{
  private static final String PREFS_ROOT_FOLDER = "RootFolder";
  private static final String PREFS_WINDOW_LOCATION = "WindowLocation";
  private static final String PREFS_DIVIDER_POSITION_1 = "DividerPosition1";
  private static final String PREFS_DIVIDER_POSITION_2 = "DividerPosition2";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private String rootFolderName;
  private Path rootFolderPath;

  private Stage primaryStage;
  private final BorderPane mainPane = new BorderPane ();

  private final MenuBar menuBar = new MenuBar ();
  private FileMenu fileMenu;
  //  private EditMenu editMenu;
  //  private OptionsMenu optionsMenu;

  SplitPane splitPane = new SplitPane ();
  private double dividerPosition1;
  private double dividerPosition2;

  ListView<String> listView = new ListView<> ();
  TextArea textArea = new TextArea ();

  private Reader reader;

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

    XmitTree fileView =
        new XmitTree (new FileTreeItem (new File ("/Users/denismolony/code/xmit")));

    fileView.getSelectionModel ().selectedItemProperty ()
        .addListener ( (v, oldValue, newValue) -> select (newValue));

    listView.getSelectionModel ().selectedItemProperty ()
        .addListener ( (v, oldValue, newValue) -> selectMember (newValue));

    listView.setStyle ("-fx-font-size: 13; -fx-font-family: monospaced");
    textArea.setStyle ("-fx-font-size: 13; -fx-font-family: monospaced");

    splitPane.getItems ().addAll (fileView, listView, textArea);

    fileMenu = new FileMenu (this, fileView);

    mainPane.setCenter (splitPane);

    // add menus
    mainPane.setTop (menuBar);
    ObservableList<Menu> menus = menuBar.getMenus ();
    menus.add (fileMenu.getMenu ());

    // exit action
    primaryStage.setOnCloseRequest (e -> exit ());

    restoreWindowLocation ();

    return mainPane;
  }

  // ---------------------------------------------------------------------------------//
  // select
  // ---------------------------------------------------------------------------------//

  void select (TreeItem<File> treeItem)
  {
    try
    {
      File file = treeItem.getValue ();
      if (!file.isFile ())
      {
        listView.getItems ().clear ();
        textArea.clear ();
        return;
      }
      reader = new Reader (Files.readAllBytes (file.toPath ()));
      ObservableList<String> items = FXCollections.observableArrayList ();

      List<CatalogEntry> catalogEntries = reader.getCatalogEntries ();
      if (catalogEntries.size () > 0)
      {
        for (CatalogEntry catalogEntry : reader.getCatalogEntries ())
          items.add (catalogEntry.getMemberName ());
        listView.setItems (items);
        listView.getSelectionModel ().select (0);
      }
      else
      {
        listView.getItems ().clear ();
        textArea.setText (reader.getLines ());
      }
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }

  // ---------------------------------------------------------------------------------//
  // selectMember
  // ---------------------------------------------------------------------------------//

  void selectMember (String memberName)
  {
    List<CatalogEntry> catalogEntries = reader.getCatalogEntries ();

    for (CatalogEntry catalogEntry : catalogEntries)
      if (catalogEntry.getMemberName ().equals (memberName))
      {
        textArea.setText (catalogEntry.getText ());
        break;
      }
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

    //    fileMenu.exit ();
    //    editMenu.exit ();
    //    optionsMenu.exit ();
    //    filesTreeTable.exit ();
    //    outputPane.exit ();

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
}
