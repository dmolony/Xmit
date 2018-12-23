package com.bytezone.xmit.gui;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
      isLeaf = f.isFile ();
    }

    return isLeaf;
  }

  // ---------------------------------------------------------------------------------//
  // buildChildren
  // ---------------------------------------------------------------------------------//

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

    return FXCollections.emptyObservableList ();
  }

  // ---------------------------------------------------------------------------------//
  // getCompressedFileSystems
  // ---------------------------------------------------------------------------------//

  private List<FileTreeItem> getCompressedFileSystems (Path path)
  {
    List<FileTreeItem> treeItemList = new ArrayList<> ();
    //        FileItem fileItem = getValue ();
    String suffix = XmitTree.getSuffix (path.toFile ().getName ());

    switch (suffix)
    {
      case "zip":
        Map<ZipEntry, File> fileList = XmitTree.decompressZip (path);
        if (fileList.size () == 0)
        {
          //          fileItem.setFileType ("No Disk");
          break;
        }
        link (fileList, treeItemList);
        break;
      default:
        break;
    }

    return treeItemList;
  }

  // ---------------------------------------------------------------------------------//
  // link
  // ---------------------------------------------------------------------------------//

  private void link (Map<ZipEntry, File> fileList, List<FileTreeItem> treeItemList)
  {
    for (ZipEntry entry : fileList.keySet ())
    {
      String entryName = entry.getName ();
      String[] chunks = entryName.split ("/");
      int filePos = chunks.length - 1;
      String fileName = chunks[filePos];

      //      FileTreeItem treeItem = createNamedTreeItemFile (fileList.get (entry), fileName);
      //      LocalDateTime dateTime = LocalDateTime
      //          .ofInstant (Instant.ofEpochMilli (entry.getTime ()), ZoneId.systemDefault ());
      //      treeItem.getValue ().setDateTime (dateTime);
      treeItemList.add (new FileTreeItem (fileList.get (entry)));
    }
  }

  // ---------------------------------------------------------------------------------//
  // createNamedTreeItemFile
  // ---------------------------------------------------------------------------------//

  //  private FileTreeItem createNamedTreeItemFile (File file, String name)
  //  {
  //    FileItem fileItem = new FileItem (file.toPath ());
  //    fileItem.setFileName (name);
  //    return new FileTreeItem (fileItem);
  //  }
}