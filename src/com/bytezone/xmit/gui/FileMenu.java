package com.bytezone.xmit.gui;

import java.io.File;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

class FileMenu
{
  private final Menu fileMenu = new Menu ("File");
  private final MenuItem rootMenuItem = new MenuItem ("Set root folder");

  private final TreeView<File> tree;
  private Alert alert;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public FileMenu (XmitApp owner, TreeView<File> tree)
  {
    this.tree = tree;

    fileMenu.getItems ().addAll (rootMenuItem);
    rootMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.R, KeyCombination.SHORTCUT_DOWN));

    rootMenuItem.setOnAction (e -> owner.changeRootFolder ());
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
}