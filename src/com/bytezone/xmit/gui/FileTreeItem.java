package com.bytezone.xmit.gui;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class FileTreeItem extends TreeItem<File>
{
  private boolean isFirstTimeChildren = true;
  private boolean isFirstTimeLeaf = true;
  private boolean isLeaf;

  public FileTreeItem (File f)
  {
    super (f);
  }

  @Override
  public ObservableList<TreeItem<File>> getChildren ()
  {
    if (isFirstTimeChildren)
    {
      isFirstTimeChildren = false;
      super.getChildren ().setAll (buildChildren (this));
    }
    return super.getChildren ();
  }

  @Override
  public boolean isLeaf ()
  {
    if (isFirstTimeLeaf)
    {
      isFirstTimeLeaf = false;
      File f = getValue ();
      isLeaf = f.isFile ();
    }

    return isLeaf;
  }

  private ObservableList<TreeItem<File>> buildChildren (TreeItem<File> TreeItem)
  {
    File f = TreeItem.getValue ();
    if (f != null && f.isDirectory ())
    {
      File[] files = f.listFiles ();
      if (files != null)
      {
        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList ();

        for (File childFile : files)
          if (childFile.isFile () && !childFile.isHidden ())
            children.add (new FileTreeItem (childFile));

        return children;
      }
    }

    return FXCollections.emptyObservableList ();
  }
}