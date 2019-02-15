package com.bytezone.xmit.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PsDataset;
import com.bytezone.xmit.Utility;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;

// ---------------------------------------------------------------------------------//
class FileMenu implements TableItemSelectionListener, TreeItemSelectionListener
//---------------------------------------------------------------------------------//
{
  private static final String PREFS_EXTRACT_FOLDER = "ExtractFolder";
  private static final String PREFS_SAVE_FOLDER = "SaveFolder";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final Menu fileMenu = new Menu ("File");
  private final MenuItem rootMenuItem = new MenuItem ("Set XMIT root folder");
  private final MenuItem extractMenuItem = new MenuItem ("Extract file");
  private final MenuItem saveMenuItem = new MenuItem ("Save output");

  private CatalogEntry catalogEntry;
  private Dataset dataset;

  private String saveFolderName;
  private String extractFolderName;

  private OutputWriter outputWriter;

  // ---------------------------------------------------------------------------------//
  public FileMenu (XmitApp owner, TreeView<XmitFile> tree)
  // ---------------------------------------------------------------------------------//
  {
    fileMenu.getItems ().addAll (rootMenuItem, extractMenuItem, saveMenuItem);
    rootMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.R, KeyCombination.SHORTCUT_DOWN));
    extractMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.E, KeyCombination.SHORTCUT_DOWN));
    saveMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.S, KeyCombination.SHORTCUT_DOWN));

    rootMenuItem.setOnAction (e -> owner.changeRootFolder ());
    extractMenuItem.setOnAction (e -> extractFile ());
    saveMenuItem.setOnAction (e -> saveFile ());
    saveMenuItem.setDisable (true);
  }

  // ---------------------------------------------------------------------------------//
  void setOutputWriter (OutputWriter outputWriter)
  // ---------------------------------------------------------------------------------//
  {
    this.outputWriter = outputWriter;
    saveMenuItem.setDisable (false);
  }

  // ---------------------------------------------------------------------------------//
  private void saveFile ()
  // ---------------------------------------------------------------------------------//
  {
    if (outputWriter == null)
      return;

    String extra = dataset.isPds () ? "." + catalogEntry.getMemberName () : "";
    String name = dataset.getReader ().getFileName () + extra + ".txt";

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
    String name = "";

    if (dataset.isPs ())
    {
      DataFile member = ((PsDataset) dataset).getMember ();
      buffer = member.getDataBuffer ();
      name = dataset.getReader ().getFileName () + "." //+ this.name + "."
          + member.getFileType ().name ();
    }
    else
    {
      buffer = catalogEntry.getMember ().getDataBuffer ();
      name = dataset.getReader ().getFileName () + "." + catalogEntry.getMemberName ()
          + "." + catalogEntry.getMember ().getFileType ().name ();
    }

    FileChooser fileChooser = new FileChooser ();
    fileChooser.setTitle ("Extract file to");
    fileChooser.setInitialDirectory (new File (extractFolderName));
    fileChooser.setInitialFileName (name);

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
  void restore ()
  // ---------------------------------------------------------------------------------//
  {
    saveFolderName = prefs.get (PREFS_SAVE_FOLDER, System.getProperty ("user.home"));
    extractFolderName =
        prefs.get (PREFS_EXTRACT_FOLDER, System.getProperty ("user.home"));
  }

  // ---------------------------------------------------------------------------------//
  void exit ()
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
  public void tableItemSelected (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    this.catalogEntry = catalogEntry;
    extractMenuItem.setText ("Extract " + catalogEntry.getMemberName ());
    extractMenuItem.setDisable (false);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  // ---------------------------------------------------------------------------------//
  {
    this.dataset = dataset;
    catalogEntry = null;

    if (dataset == null)
    {
      extractMenuItem.setText ("Extract file");
      extractMenuItem.setDisable (true);
    }
    else if (dataset.isPs ())
    {
      extractMenuItem.setText ("Extract " + dataset.getReader ().getFileName ());
      extractMenuItem.setDisable (false);
    }
    else
    {
      extractMenuItem.setText ("Extract " + name);
      extractMenuItem.setDisable (true);
    }
  }
}