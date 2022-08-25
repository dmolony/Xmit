package com.bytezone.xmit.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import com.bytezone.appbase.AppBase;
import com.bytezone.appbase.SaveState;
import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.gui.XmitTree.TreeNodeListener;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;

// -----------------------------------------------------------------------------------//
class FileMenu extends Menu implements TableItemSelectionListener, TreeNodeListener, SaveState
// -----------------------------------------------------------------------------------//
{
  private static final String PREFS_EXTRACT_FOLDER = "ExtractFolder";
  private static final String PREFS_SAVE_FOLDER = "SaveFolder";

  private final MenuItem rootMenuItem = new MenuItem ("Set XMIT root folder...");
  private final MenuItem extractMenuItem = new MenuItem ("Extract file...");
  private final MenuItem saveMenuItem = new MenuItem ("Save output...");
  private final MenuItem aboutMenuItem = new MenuItem ("Show version...");

  private TreeNodeData nodeData;
  private CatalogEntry catalogEntry;
  private DataFile dataFile;

  private String saveFolderName;
  private String extractFileName;
  private String extractFolderName;

  private OutputWriter outputWriter;

  // ---------------------------------------------------------------------------------//
  public FileMenu (String name)
  // ---------------------------------------------------------------------------------//
  {
    super (name);

    getItems ().addAll (rootMenuItem, extractMenuItem, saveMenuItem, new SeparatorMenuItem (),
        aboutMenuItem);

    rootMenuItem.setAccelerator (new KeyCodeCombination (KeyCode.R, KeyCombination.SHORTCUT_DOWN));
    extractMenuItem
        .setAccelerator (new KeyCodeCombination (KeyCode.E, KeyCombination.SHORTCUT_DOWN));
    saveMenuItem.setAccelerator (new KeyCodeCombination (KeyCode.S, KeyCombination.SHORTCUT_DOWN));

    extractMenuItem.setOnAction (e -> extractFile ());

    saveMenuItem.setOnAction (e -> saveFile ());
    aboutMenuItem.setOnAction (e -> about ());
    saveMenuItem.setDisable (true);
  }

  // ---------------------------------------------------------------------------------//
  void setRootAction (EventHandler<ActionEvent> action)
  // ---------------------------------------------------------------------------------//
  {
    rootMenuItem.setOnAction (action);
  }

  // ---------------------------------------------------------------------------------//
  void setOutputWriter (OutputWriter outputWriter)
  // ---------------------------------------------------------------------------------//
  {
    this.outputWriter = outputWriter;
    saveMenuItem.setDisable (false);
  }

  // ---------------------------------------------------------------------------------//
  private void about ()
  // ---------------------------------------------------------------------------------//
  {
    AppBase.showAlert (AlertType.INFORMATION, "About XmitApp",
        "Version: 1.0.25\nReleased: 7 January 2022\nAuthor: Denis Molony");
  }

  // ---------------------------------------------------------------------------------//
  private void saveFile ()
  // ---------------------------------------------------------------------------------//
  {
    if (outputWriter == null)
      return;

    String extra = catalogEntry == null ? "" : "." + catalogEntry.getMemberName ();
    String name = nodeData.getName () + extra + ".txt";

    FileChooser fileChooser = new FileChooser ();
    fileChooser.setTitle ("Save output text to");

    File saveFolder = new File (saveFolderName);
    if (!saveFolder.exists ())
      saveFolder = new File (System.getProperty ("user.home"));

    fileChooser.setInitialDirectory (saveFolder);
    fileChooser.setInitialFileName (name);

    File file = fileChooser.showSaveDialog (null);
    if (file != null)
    {
      outputWriter.write (file);
      saveFolderName = file.getParent ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private void extractFile ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = dataFile.getDataBuffer ();

    FileChooser fileChooser = new FileChooser ();
    fileChooser.setTitle ("Extract file to");
    fileChooser.setInitialDirectory (new File (extractFolderName));

    String suffix = "." + dataFile.getFileType ().name ();
    fileChooser.setInitialFileName (extractFileName + suffix);

    File file = fileChooser.showSaveDialog (null);
    if (file != null)
      try
      {
        Files.write (Paths.get (file.getAbsolutePath ()), buffer);
        extractFolderName = file.getParent ();
        AppBase.showAlert (AlertType.INFORMATION, "Success", "File Extracted: " + file.getName ());
      }
      catch (IOException e)
      {
        AppBase.showAlert (AlertType.ERROR, "Error", "File Error: " + e.getMessage ());
      }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    saveFolderName = prefs.get (PREFS_SAVE_FOLDER, System.getProperty ("user.home"));
    if (!new File (saveFolderName).exists ())
      saveFolderName = System.getProperty ("user.home");

    extractFolderName = prefs.get (PREFS_EXTRACT_FOLDER, System.getProperty ("user.home"));
    if (!new File (extractFolderName).exists ())
      extractFolderName = System.getProperty ("user.home");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void save (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    prefs.put (PREFS_SAVE_FOLDER, saveFolderName);
    prefs.put (PREFS_EXTRACT_FOLDER, extractFolderName);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeNodeSelected (TreeNodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    this.nodeData = nodeData;

    if (nodeData.isPhysicalSequentialDataset ())
    {
      dataFile = nodeData.getDataFile ();
      set (nodeData.getDatasetName (), nodeData.getDatasetName ());
    }
    else
    {
      dataFile = null;
      set ("", "");
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    this.catalogEntry = catalogEntry;
    dataFile = catalogEntry.getMember ();

    set (catalogEntry.getMemberName (),
        nodeData.getDatasetName () + "." + catalogEntry.getMemberName ());
  }

  // ---------------------------------------------------------------------------------//
  private void set (String menuText, String fileName)
  // ---------------------------------------------------------------------------------//
  {
    extractMenuItem.setText ("Extract " + menuText + "...");
    extractFileName = fileName;
    extractMenuItem.setDisable (menuText.isEmpty ());
  }
}