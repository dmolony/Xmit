package com.bytezone.xmit.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsMember;
import com.bytezone.xmit.Utility;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TreeItem;

// -----------------------------------------------------------------------------------//
public class XmitTreeItem extends TreeItem<NodeData>
// -----------------------------------------------------------------------------------//
{
  private static final FileComparator comparator = new FileComparator ();

  private boolean firstTimeLeaf = true;
  private boolean firstTimeChildren = true;
  private boolean isLeaf;

  // ---------------------------------------------------------------------------------//
  public XmitTreeItem (NodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    super (nodeData);
  }

  // ---------------------------------------------------------------------------------//
  void setLeaf (boolean isLeaf)
  // ---------------------------------------------------------------------------------//
  {
    this.isLeaf = isLeaf;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isLeaf ()
  // ---------------------------------------------------------------------------------//
  {
    if (firstTimeLeaf)
    {
      firstTimeLeaf = false;
      NodeData nodeData = getValue ();

      if (nodeData.isFile ())
        if (nodeData.isDirectory () || nodeData.isCompressedFile () || nodeData.isTape ())
          isLeaf = false;
        else
          isLeaf = XmitTree.merging;
      else if (nodeData.isMember ())
        isLeaf = XmitTree.merging;
      else if (nodeData.isDataset ())
        if (nodeData.isPartitionedDataset ())
          isLeaf = nodeData.getPdsXmitMembers ().size () == 0;
        else
          isLeaf = true;
    }

    return isLeaf;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public ObservableList<TreeItem<NodeData>> getChildren ()
  // ---------------------------------------------------------------------------------//
  {
    if (firstTimeChildren)
    {
      firstTimeChildren = false;
      super.getChildren ().setAll (buildChildren ());
    }
    return super.getChildren ();
  }

  // ---------------------------------------------------------------------------------//
  ObservableList<TreeItem<NodeData>> buildChildren ()
  // ---------------------------------------------------------------------------------//
  {
    ObservableList<TreeItem<NodeData>> children = FXCollections.observableArrayList ();
    NodeData nodeData = getValue ();

    if (nodeData.isPartitionedDataset ())
      addPdsMembers (children, nodeData);
    else if (nodeData.isMember ())
      addDatasets (children, nodeData);
    else if (nodeData.isFile ())
      if (nodeData.isDirectory ())
        addFiles (children, nodeData);
      else if (nodeData.isCompressedFile ())
        addCompressedFiles (children, nodeData);
      else
        addDatasets (children, nodeData);

    return children;
  }

  // ---------------------------------------------------------------------------------//
  private void addFiles (ObservableList<TreeItem<NodeData>> children, NodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    File[] files = nodeData.getFile ().listFiles ();
    if (files == null)
      return;

    for (File file : files)
    {
      if (file.isHidden ())
        continue;

      if (file.isDirectory () || NodeData.isValidFileName (file.getName ()))
        children.add (new XmitTreeItem (new NodeData (file)));
    }
    Collections.sort (children, comparator);
  }

  // ---------------------------------------------------------------------------------//
  private void addDatasets (ObservableList<TreeItem<NodeData>> children,
      NodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    for (Dataset dataset : nodeData)
      children.add (new XmitTreeItem (new NodeData (dataset)));
  }

  // ---------------------------------------------------------------------------------//
  private void addPdsMembers (ObservableList<TreeItem<NodeData>> children,
      NodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    for (PdsMember member : nodeData.getPdsXmitMembers ())
      children.add (new XmitTreeItem (new NodeData (member)));
  }

  // ---------------------------------------------------------------------------------//
  private void addCompressedFiles (ObservableList<TreeItem<NodeData>> children,
      NodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    Map<ZipEntry, XmitTreeItem> fileList = decompressZip (nodeData.getFile ().toPath ());
    if (fileList.size () > 0)
    {
      for (ZipEntry entry : fileList.keySet ())
      {
        String entryName = entry.getName ();
        String[] chunks = entryName.split ("/");
        int filePos = chunks.length - 1;
        String fileName = chunks[filePos];
        if (NodeData.isValidFileName (fileName))
          children.add (fileList.get (entry));
      }
      Collections.sort (children, comparator);
    }
  }

  // ---------------------------------------------------------------------------------//
  public static Map<ZipEntry, XmitTreeItem> decompressZip (Path path)
  // ---------------------------------------------------------------------------------//
  {
    Map<ZipEntry, XmitTreeItem> fileMap = new HashMap<> ();

    try (ZipFile zipFile = new ZipFile (path.toString ()))
    {
      Enumeration<? extends ZipEntry> entries = zipFile.entries ();
      List<String> invalidNames = new ArrayList<> ();
      boolean containsFolder = false;
      while (entries.hasMoreElements ())
      {
        ZipEntry entry = entries.nextElement ();
        String entryName = entry.getName ();
        if (entryName.endsWith ("/"))
          containsFolder = true;
        else if (NodeData.isValidFileName (entryName))
        {
          int pos = entryName.lastIndexOf ('.');
          String suffix = pos < 0 ? "" : entryName.substring (pos).toLowerCase ();
          InputStream stream = zipFile.getInputStream (entry);
          File tmp = File.createTempFile (entry.getName (), suffix);

          FileOutputStream fos = new FileOutputStream (tmp);

          int bytesRead;
          byte[] buffer = new byte[1024];
          while ((bytesRead = stream.read (buffer)) > 0)
            fos.write (buffer, 0, bytesRead);

          stream.close ();
          fos.close ();
          tmp.deleteOnExit ();          // why not delete it now?
          fileMap.put (entry, new XmitTreeItem (new NodeData (tmp, entryName)));
        }
        else
          invalidNames.add (entryName);
      }

      if (fileMap.isEmpty ())
      {
        int size = invalidNames.size ();
        String message =
            String.format ("Zip file contains %d file%s, but no .XMI or .AWS files", size,
                size == 1 ? "" : "s");
        if (containsFolder)
          message += "\nFile contains unexamined subfolders";
        Utility.showAlert (AlertType.INFORMATION, "", message);
      }
    }
    catch (IOException e)
    {
      System.out.println (e);
      System.err.println (e.getMessage ());
    }

    return fileMap;
  }
}
