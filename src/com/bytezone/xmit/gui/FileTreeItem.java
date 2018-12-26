package com.bytezone.xmit.gui;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Reader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class FileTreeItem extends TreeItem<XmitFile>
{
  static final FileComparator comparator = new FileComparator ();

  private boolean isFirstTimeChildren = true;
  private boolean isFirstTimeLeaf = true;
  private boolean isLeaf;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public FileTreeItem (XmitFile xmitFile)
  {
    super (xmitFile);
  }

  // ---------------------------------------------------------------------------------//
  // getChildren
  // ---------------------------------------------------------------------------------//

  @Override
  public ObservableList<TreeItem<XmitFile>> getChildren ()
  {
    if (isFirstTimeChildren)
    {
      isFirstTimeChildren = false;
      super.getChildren ().setAll (buildChildren ());
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
      XmitFile xmitFile = getValue ();
      isLeaf = (xmitFile.isFile () && !xmitFile.isCompressed ()) || xmitFile.isMember ();
    }

    return isLeaf;
  }

  // ---------------------------------------------------------------------------------//
  // buildChildren
  // ---------------------------------------------------------------------------------//

  ObservableList<TreeItem<XmitFile>> buildChildren ()
  {
    XmitFile xmitFile = getValue ();

    if (xmitFile == null)
      return FXCollections.emptyObservableList ();

    if (xmitFile.isDirectory ())
    {
      ObservableList<TreeItem<XmitFile>> children = FXCollections.observableArrayList ();
      File[] files = xmitFile.listFiles ();
      if (files != null)
      {
        for (File childFile : files)
        {
          String name = childFile.getName ().toUpperCase ();
          if (childFile.isHidden ())
            continue;
          if (childFile.isDirectory () || XmitFile.isValidFileName (name))
            children.add (new FileTreeItem (new XmitFile (childFile)));
        }

        Collections.sort (children, comparator);
      }
      return children;
    }

    if (xmitFile.isCompressed ())
    {
      ObservableList<TreeItem<XmitFile>> children = FXCollections.observableArrayList ();
      Map<ZipEntry, XmitFile> fileList = XmitFile.decompressZip (xmitFile.toPath ());
      if (fileList.size () > 0)
      {
        for (ZipEntry entry : fileList.keySet ())
        {
          String entryName = entry.getName ();
          String[] chunks = entryName.split ("/");
          int filePos = chunks.length - 1;
          String fileName = chunks[filePos];
          if (XmitFile.isValidFileName (fileName))
          {
            children.add (new FileTreeItem (fileList.get (entry)));
          }
        }
        Collections.sort (children, comparator);
      }
      return children;
    }

    //if (xmitFile.isFile ())
    {
      //      System.out.println ("XXXXX " + xmitFile.getName ());
      Reader reader = xmitFile.getReader ();
      ObservableList<TreeItem<XmitFile>> children = FXCollections.observableArrayList ();

      List<CatalogEntry> xmitMembers = reader.getXmitFiles ();
      if (xmitMembers.size () > 0)
      {
        //        System.out.println (xmitMembers.size ());
        isLeaf = false;
        for (CatalogEntry member : xmitMembers)
          children.add (new FileTreeItem (new XmitFile (member)));
        Collections.sort (children, comparator);
      }
      return children;
    }

    //    return FXCollections.emptyObservableList ();
  }
}
