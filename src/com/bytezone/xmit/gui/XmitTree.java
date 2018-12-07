package com.bytezone.xmit.gui;

import java.io.File;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class XmitTree extends TreeView<File>
{
  XmitTree (FileTreeItem fileTreeItem)
  {
    super (fileTreeItem);

    fileTreeItem.setExpanded (true);
    setStyle ("-fx-font-size: 13; -fx-font-family: monospaced");

    setCellFactory (new Callback<TreeView<File>, TreeCell<File>> ()
    {
      @Override
      public TreeCell<File> call (TreeView<File> parm)
      {
        TreeCell<File> cell = new TreeCell<> ()
        {
          public void updateItem (File item, boolean empty)
          {
            super.updateItem (item, empty);
            if (empty || item == null)
            {
              setText (null);
              setGraphic (null);
            }
            else
            {
              setText (item.getName ());
              setGraphic (getTreeItem ().getGraphic ());
            }
          }
        };
        return cell;
      }
    });
  }
}
