package com.bytezone.xmit.gui;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipEntry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class FileTreeItem extends TreeItem<File>
{
  static final FileComparator comparator = new FileComparator ();

  private boolean isFirstTimeChildren = true;
  private boolean isFirstTimeLeaf = true;
  private boolean isLeaf;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public FileTreeItem (File f)
  {
    super (f);
  }

  // ---------------------------------------------------------------------------------//
  // getChildren
  // ---------------------------------------------------------------------------------//

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

  // ---------------------------------------------------------------------------------//
  // isLeaf
  // ---------------------------------------------------------------------------------//

  @Override
  public boolean isLeaf ()
  {
    if (isFirstTimeLeaf)
    {
      isFirstTimeLeaf = false;
      File f = getValue ();
      String suffix = XmitTree.getSuffix (f.getName ());
      isLeaf = f.isFile () && !XmitTree.isCompressionSuffix (suffix);
    }

    return isLeaf;
  }

  // ---------------------------------------------------------------------------------//
  // buildChildren
  // ---------------------------------------------------------------------------------//

  private ObservableList<TreeItem<File>> buildChildren (TreeItem<File> TreeItem)
  {
    File f = getValue ();

    if (f == null)
      return FXCollections.emptyObservableList ();

    if (f.isDirectory ())
    {
      File[] files = f.listFiles ();
      if (files != null)
      {
        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList ();

        for (File childFile : files)
        {
          String name = childFile.getName ().toUpperCase ();
          if (childFile.isHidden ())
            continue;
          if (childFile.isDirectory () || XmitTree.isValidFileName (name))
            children.add (new FileTreeItem (childFile));
        }

        Collections.sort (children, comparator);
        return children;
      }
    }

    String suffix = XmitTree.getSuffix (f.getName ());
    if (XmitTree.isCompressionSuffix (suffix))
    {
      Map<ZipEntry, File> fileList = XmitTree.decompressZip (f.toPath ());
      if (fileList.size () > 0)
      {
        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList ();
        for (ZipEntry entry : fileList.keySet ())
        {
          String entryName = entry.getName ();
          String[] chunks = entryName.split ("/");
          int filePos = chunks.length - 1;
          String fileName = chunks[filePos];
          //          System.out.println (entryName);
          //          System.out.println (fileName);
          if (XmitTree.isValidFileName (fileName))
          {
            children.add (new FileTreeItem (fileList.get (entry)));
          }
        }
        Collections.sort (children, comparator);
        return children;
      }
    }

    return FXCollections.emptyObservableList ();
  }
}