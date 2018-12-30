package com.bytezone.xmit.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Reader;

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
  private final Menu fileMenu = new Menu ("File");
  private final MenuItem rootMenuItem = new MenuItem ("Set root folder");
  private final MenuItem extractMenuItem = new MenuItem ("Extract file");

  private CatalogEntry catalogEntry;
  private Reader reader;
  private Dataset dataset;

  private Alert alert;

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

  void showAlert (String message)
  {
    if (alert == null)
    {
      alert = new Alert (AlertType.ERROR);
      alert.setTitle ("Error");
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

    System.out.println ("Extracting file: " + catalogEntry.getMemberName ());
    byte[] buffer = catalogEntry.getDataBuffer ();

    // use the PDS dataset name as the default
    // or use the parent file.member name
    FileChooser fileChooser = new FileChooser ();
    //    fileChooser.getExtensionFilters ()
    //        .add (new FileChooser.ExtensionFilter ("XMIT files (*.xmi)", "*.xmi"));

    File file = fileChooser.showSaveDialog (null);
    if (file != null)
      try
      {
        Files.write (Paths.get (file.getAbsolutePath ()), buffer);
      }
      catch (IOException e)
      {
        showAlert ("File Error: " + e.getMessage ());
      }
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
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