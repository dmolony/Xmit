package com.bytezone.xmit.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Reader;
import com.bytezone.xmit.Utility;

import javafx.scene.control.Alert;
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
  private static final byte[] pdf = { 0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E };
  private static final byte[] zip = { 0x50, 0x4B, 0x03, 0x04 };
  private static final byte[] word = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
                                       (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1 };
  private static final byte[] winrar = { 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07 };
  private static final byte[] png = { (byte) 0x89, 0x50, 0x4E, 0x47 };

  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final Menu fileMenu = new Menu ("File");
  private final MenuItem rootMenuItem = new MenuItem ("Set root folder");
  private final MenuItem extractMenuItem = new MenuItem ("Extract file");

  private CatalogEntry catalogEntry;
  private Reader reader;
  private Dataset dataset;

  private Alert alert;
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
  // showAlert
  // ---------------------------------------------------------------------------------//

  void showAlert (AlertType alertType, String title, String message)
  {
    if (alert == null)
    {
      alert = new Alert (alertType);
      alert.setTitle (title);
      alert.setHeaderText (null);
    }

    alert.setContentText (message);
    alert.showAndWait ();
  }

  // ---------------------------------------------------------------------------------//
  // extractFile
  // ---------------------------------------------------------------------------------//

  private void extractFile ()
  {
    assert catalogEntry != null;

    byte[] buffer = catalogEntry.getDataBuffer ();

    FileChooser fileChooser = new FileChooser ();
    fileChooser.setTitle ("Extract file to");
    fileChooser.setInitialDirectory (new File (saveFolderName));

    String name = catalogEntry.getMemberName ().trim ();
    if (catalogEntry.isXmit ())
      name += ".XMI";
    else if (Utility.matches (pdf, buffer, 0))
      name += ".PDF";
    else if (Utility.matches (word, buffer, 0))
      name += ".DOC";
    else if (Utility.matches (zip, buffer, 0))
      name += ".ZIP";
    else if (Utility.matches (winrar, buffer, 0))
      name += ".RAR";
    else if (Utility.matches (png, buffer, 0))
      name += ".PNG";
    else
      name += ".BIN";
    fileChooser.setInitialFileName (name);

    File file = fileChooser.showSaveDialog (null);
    if (file != null)
      try
      {
        Files.write (Paths.get (file.getAbsolutePath ()), buffer);
        saveFolderName = file.getParent ();
        showAlert (AlertType.INFORMATION, "Success",
            "File Extracted: " + file.getName ());
      }
      catch (IOException e)
      {
        showAlert (AlertType.ERROR, "Error", "File Error: " + e.getMessage ());
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
    if (catalogEntry == null)
    {
      extractMenuItem.setText ("Extract file");
      extractMenuItem.setDisable (true);
    }
    else
    {
      extractMenuItem.setText ("Extract " + catalogEntry.getMemberName ());
      extractMenuItem.setDisable (false);
    }
  }

  // ---------------------------------------------------------------------------------//
  // treeItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemSelected (Reader reader, Dataset dataset)
  {
    this.reader = reader;
    this.dataset = dataset;
  }
}