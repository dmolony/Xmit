package com.bytezone.xmit.gui;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.PdsMember;
import com.bytezone.xmit.Reader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

// -----------------------------------------------------------------------------------//
class FileTreeItem extends TreeItem<XmitFileNode>
// -----------------------------------------------------------------------------------//
{
  static private final FileComparator comparator = new FileComparator ();

  private boolean isFirstTimeChildren = true;
  private boolean isFirstTimeLeaf = true;
  private boolean isLeaf;

  // ---------------------------------------------------------------------------------//
  public FileTreeItem (XmitFileNode xmitFile)
  // ---------------------------------------------------------------------------------//
  {
    super (xmitFile);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isLeaf ()
  // ---------------------------------------------------------------------------------//
  {
    if (isFirstTimeLeaf)
    {
      isFirstTimeLeaf = false;
      isLeaf = getValue ().isLeaf ();
    }

    return isLeaf;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public ObservableList<TreeItem<XmitFileNode>> getChildren ()
  // ---------------------------------------------------------------------------------//
  {
    if (isFirstTimeChildren)
    {
      isFirstTimeChildren = false;
      super.getChildren ().setAll (buildChildren ());
    }
    return super.getChildren ();
  }

  // ---------------------------------------------------------------------------------//
  ObservableList<TreeItem<XmitFileNode>> buildChildren ()
  // ---------------------------------------------------------------------------------//
  {
    ObservableList<TreeItem<XmitFileNode>> children =
        FXCollections.observableArrayList ();

    XmitFileNode xmitFileNode = getValue ();
    if (xmitFileNode == null)
      return children;

    // list directory contents
    if (xmitFileNode.isDirectory ())
    {
      File[] files = xmitFileNode.listFiles ();
      if (files == null)
        return children;

      for (File file : files)
      {
        if (file.isHidden ())
          continue;

        if (file.isDirectory () || XmitFileNode.isValidFileName (file))
          children.add (new FileTreeItem (new XmitFileNode (file)));
      }

      Collections.sort (children, comparator);

      return children;
    }

    // list compressed file contents
    if (xmitFileNode.isCompressed ())
    {
      Map<ZipEntry, XmitFileNode> fileList =
          XmitFileNode.decompressZip (xmitFileNode.toPath ());
      if (fileList.size () > 0)
      {
        for (ZipEntry entry : fileList.keySet ())
        {
          String entryName = entry.getName ();
          String[] chunks = entryName.split ("/");
          int filePos = chunks.length - 1;
          String fileName = chunks[filePos];
          if (XmitFileNode.isValidFileName (fileName))
          {
            children.add (new FileTreeItem (fileList.get (entry)));
          }
        }
        Collections.sort (children, comparator);
      }
      return children;
    }

    Reader reader = xmitFileNode.getReader ();
    if (reader.size () > 1)
    {
      System.out.println ("build multi dataset");
    }

    Dataset dataset = reader.getActiveDataset ();

    if (dataset.isPartitionedDataset ())
    {
      // check for xmit files stored in PDS members
      List<PdsMember> members = ((PdsDataset) dataset).getPdsXmitMembers ();
      if (members.size () > 0)
      {
        isLeaf = false;
        for (PdsMember member : members)
          children.add (new FileTreeItem (new XmitFileNode (member)));
        Collections.sort (children, comparator);
      }
    }

    return children;
  }
}
