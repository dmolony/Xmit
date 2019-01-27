package com.bytezone.xmit.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import com.bytezone.xmit.*;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;

class FileMenu implements TableItemSelectionListener, TreeItemSelectionListener
{
  private static final String PREFS_SAVE_FOLDER = "SaveFolder";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final Menu fileMenu = new Menu ("File");
  private final MenuItem rootMenuItem = new MenuItem ("Set XMIT root folder");
  private final MenuItem extractMenuItem = new MenuItem ("Extract file");

  private CatalogEntry catalogEntry;
  private Dataset dataset;
  private String name;

  private String saveFolderName;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public FileMenu (XmitApp owner, TreeView<XmitFile> tree)
  {
    fileMenu.getItems ().addAll (rootMenuItem, extractMenuItem);
    rootMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.R, KeyCombination.SHORTCUT_DOWN));
    extractMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.E, KeyCombination.SHORTCUT_DOWN));

    rootMenuItem.setOnAction (e -> owner.changeRootFolder ());
    extractMenuItem.setOnAction (e -> extractFile ());
  }

  // ---------------------------------------------------------------------------------//
  // extractFile
  // ---------------------------------------------------------------------------------//

  private void extractFile ()
  {
    byte[] buffer = null;
    String name = "";

    if (dataset instanceof PsDataset)
    {
      //      buffer = ((PsDataset) dataset).getRawBuffer ();
      DataFile member = ((PsDataset) dataset).getMember ();
      buffer = member.getDataBuffer ();
      name = this.name + "." + member.getFileType ().name ();
    }
    else
    {
      buffer = catalogEntry.getMember ().getDataBuffer ();
      name = catalogEntry.getMemberName ().trim () + "."
          + catalogEntry.getMember ().getFileType ().name ();
    }

    FileChooser fileChooser = new FileChooser ();
    fileChooser.setTitle ("Extract file to");
    fileChooser.setInitialDirectory (new File (saveFolderName));
    fileChooser.setInitialFileName (name);

    File file = fileChooser.showSaveDialog (null);
    if (file != null)
      try
      {
        Files.write (Paths.get (file.getAbsolutePath ()), buffer);
        saveFolderName = file.getParent ();
        Utility.showAlert (AlertType.INFORMATION, "Success",
            "File Extracted: " + file.getName ());
      }
      catch (IOException e)
      {
        Utility.showAlert (AlertType.ERROR, "Error", "File Error: " + e.getMessage ());
      }
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  void restore ()
  {
    saveFolderName = prefs.get (PREFS_SAVE_FOLDER, System.getProperty ("user.home"));
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.put (PREFS_SAVE_FOLDER, saveFolderName);
  }

  // ---------------------------------------------------------------------------------//
  // getMenu
  // ---------------------------------------------------------------------------------//

  Menu getMenu ()
  {
    return fileMenu;
  }

  // ---------------------------------------------------------------------------------//
  // tableItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  {
    this.catalogEntry = catalogEntry;
    extractMenuItem.setText ("Extract " + catalogEntry.getMemberName ());
    extractMenuItem.setDisable (false);
  }

  // ---------------------------------------------------------------------------------//
  // treeItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemSelected (Reader reader, Dataset dataset, String name, String path)
  {
    //    this.reader = reader;
    this.dataset = dataset;
    this.name = name == null ? null : name.trim ();
    catalogEntry = null;

    if (dataset == null)
    {
      extractMenuItem.setText ("Extract file");
      extractMenuItem.setDisable (true);
    }
    else if (dataset instanceof PsDataset)
      extractMenuItem.setText ("Extract " + this.name);
  }
}