package com.bytezone.xmit.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Utility;

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

// ---------------------------------------------------------------------------------//
class FileMenu implements TableItemSelectionListener, TreeItemSelectionListener, SaveState
//---------------------------------------------------------------------------------//
{
  private static final String PREFS_EXTRACT_FOLDER = "ExtractFolder";
  private static final String PREFS_SAVE_FOLDER = "SaveFolder";

  private final Menu fileMenu = new Menu ("File");
  private final MenuItem rootMenuItem = new MenuItem ("Set XMIT root folder...");
  private final MenuItem extractMenuItem = new MenuItem ("Extract file...");
  private final MenuItem saveMenuItem = new MenuItem ("Save output...");
  private final MenuItem aboutMenuItem = new MenuItem ("Show version...");

  private DatasetStatus datasetStatus;

  private String saveFolderName;
  private String extractFolderName;

  private OutputWriter outputWriter;

  // ---------------------------------------------------------------------------------//
  public FileMenu ()
  // ---------------------------------------------------------------------------------//
  {
    fileMenu.getItems ().addAll (rootMenuItem, extractMenuItem, saveMenuItem,
        new SeparatorMenuItem (), aboutMenuItem);
    rootMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.R, KeyCombination.SHORTCUT_DOWN));
    extractMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.E, KeyCombination.SHORTCUT_DOWN));
    saveMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.S, KeyCombination.SHORTCUT_DOWN));

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
    Utility.showAlert (AlertType.INFORMATION, "About",
        "Version: 1.0.23\nReleased: 1 May 2019\nAuthor: Denis Molony");
  }

  // ---------------------------------------------------------------------------------//
  private void saveFile ()
  // ---------------------------------------------------------------------------------//
  {
    if (outputWriter == null)
      return;

    String extra = datasetStatus.isPds () ? "." + datasetStatus.getMemberName () : "";
    String name = datasetStatus.getReaderFileName () + extra + ".txt";

    FileChooser fileChooser = new FileChooser ();
    fileChooser.setTitle ("Save output text to");
    fileChooser.setInitialDirectory (new File (saveFolderName));
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
    byte[] buffer = null;
    String fileName = "";

    if (datasetStatus.isPs ())
    {
      DataFile member = datasetStatus.getFlatFile ();
      buffer = member.getDataBuffer ();
      fileName = datasetStatus.getReaderFileName () + "." + member.getFileType ().name ();
    }
    else
    {
      buffer = datasetStatus.getMember ().getDataBuffer ();
      fileName = datasetStatus.getReaderFileName () + "." + datasetStatus.getMemberName ()
          + "." + datasetStatus.getMember ().getFileType ().name ();
    }

    FileChooser fileChooser = new FileChooser ();
    fileChooser.setTitle ("Extract file to");
    fileChooser.setInitialDirectory (new File (extractFolderName));
    fileChooser.setInitialFileName (fileName);

    File file = fileChooser.showSaveDialog (null);
    if (file != null)
      try
      {
        Files.write (Paths.get (file.getAbsolutePath ()), buffer);
        extractFolderName = file.getParent ();
        Utility.showAlert (AlertType.INFORMATION, "Success",
            "File Extracted: " + file.getName ());
      }
      catch (IOException e)
      {
        Utility.showAlert (AlertType.ERROR, "Error", "File Error: " + e.getMessage ());
      }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    saveFolderName = prefs.get (PREFS_SAVE_FOLDER, System.getProperty ("user.home"));
    extractFolderName =
        prefs.get (PREFS_EXTRACT_FOLDER, System.getProperty ("user.home"));
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
  Menu getMenu ()
  // ---------------------------------------------------------------------------------//
  {
    return fileMenu;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (DatasetStatus datasetStatus)
  // ---------------------------------------------------------------------------------//
  {
    this.datasetStatus = datasetStatus;

    if (!datasetStatus.hasDataset ())
    {
      extractMenuItem.setText ("Extract file...");
      extractMenuItem.setDisable (true);
    }
    else if (datasetStatus.isPs ())
    {
      extractMenuItem.setText ("Extract " + datasetStatus.getReaderFileName () + "...");
      extractMenuItem.setDisable (false);
    }
    else
    {
      extractMenuItem.setText ("Extract " + datasetStatus.getName () + "...");
      extractMenuItem.setDisable (true);
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void tableItemSelected (DatasetStatus datasetStatus)
  // ---------------------------------------------------------------------------------//
  {
    if (!datasetStatus.hasCatalogEntry ())
    {
      extractMenuItem.setText ("Extract... ");
      extractMenuItem.setDisable (true);
    }
    else
    {
      extractMenuItem.setText ("Extract " + datasetStatus.getMemberName () + "...");
      extractMenuItem.setDisable (false);
    }
  }
}