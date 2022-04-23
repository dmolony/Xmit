package com.bytezone.xmit.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.bytezone.appbase.AppBase;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsMember;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TreeItem;

// -----------------------------------------------------------------------------------//
public class XmitTreeItem extends TreeItem<TreeNodeData>
// -----------------------------------------------------------------------------------//
{
  private static final FileComparator comparator = new FileComparator ();

  private boolean firstTimeLeaf = true;
  private boolean firstTimeChildren = true;
  private boolean isLeaf;

  // ---------------------------------------------------------------------------------//
  public XmitTreeItem (TreeNodeData nodeData)
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
      TreeNodeData nodeData = getValue ();

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
  public ObservableList<TreeItem<TreeNodeData>> getChildren ()
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
  ObservableList<TreeItem<TreeNodeData>> buildChildren ()
  // ---------------------------------------------------------------------------------//
  {
    ObservableList<TreeItem<TreeNodeData>> children = FXCollections.observableArrayList ();
    TreeNodeData nodeData = getValue ();

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
  private void addFiles (ObservableList<TreeItem<TreeNodeData>> children, TreeNodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    File[] files = nodeData.getFile ().listFiles ();
    if (files == null)
      return;

    for (File file : files)
    {
      if (file.isHidden ())
        continue;

      if (file.isDirectory () || TreeNodeData.isValidFileName (file.getName ()))
        children.add (new XmitTreeItem (new TreeNodeData (file)));
    }
    Collections.sort (children, comparator);
  }

  // ---------------------------------------------------------------------------------//
  private void addDatasets (ObservableList<TreeItem<TreeNodeData>> children, TreeNodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    for (Dataset dataset : nodeData)
      children.add (new XmitTreeItem (new TreeNodeData (dataset)));
  }

  // ---------------------------------------------------------------------------------//
  private void addPdsMembers (ObservableList<TreeItem<TreeNodeData>> children, TreeNodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    for (PdsMember member : nodeData.getPdsXmitMembers ())
      children.add (new XmitTreeItem (new TreeNodeData (member)));
  }

  // ---------------------------------------------------------------------------------//
  private void addCompressedFiles (ObservableList<TreeItem<TreeNodeData>> children, TreeNodeData nodeData)
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
        if (TreeNodeData.isValidFileName (fileName))
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
        else if (TreeNodeData.isValidFileName (entryName))
        {
          int pos = entryName.lastIndexOf ('.');
          String suffix = pos < 0 ? "" : entryName.substring (pos).toLowerCase ();
          InputStream inputStream = zipFile.getInputStream (entry);

          File tempFile = File.createTempFile (entryName, suffix);
          FileOutputStream outputStream = new FileOutputStream (tempFile);

          int bytesRead;
          byte[] buffer = new byte[1024];
          while ((bytesRead = inputStream.read (buffer)) > 0)
            outputStream.write (buffer, 0, bytesRead);

          inputStream.close ();
          outputStream.close ();
          tempFile.deleteOnExit ();          // it hasn't been read yet
          fileMap.put (entry, new XmitTreeItem (new TreeNodeData (tempFile, entryName)));
        }
        else
          invalidNames.add (entryName);
      }

      if (fileMap.isEmpty ())
      {
        int size = invalidNames.size ();
        String message = String.format ("Zip file contains %d file%s, but no .XMI or .AWS files",
            size, size == 1 ? "" : "s");
        if (containsFolder)
          message += "\nFile contains unexamined subfolders";
        AppBase.showAlert (AlertType.INFORMATION, "", message);
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
