package com.bytezone.xmit.gui;

import java.util.Comparator;

import javafx.scene.control.TreeItem;

// -----------------------------------------------------------------------------------//
public class FileComparator2 implements Comparator<TreeItem<String>>
//-----------------------------------------------------------------------------------//
{
  @Override
  public int compare (TreeItem<String> thisFile, TreeItem<String> thatFile)
  {
    NodeData nodeData1 = ((XmitTreeItem2) thisFile).nodeData;
    NodeData nodeData2 = ((XmitTreeItem2) thatFile).nodeData;

    boolean thisFileIsDirectory = nodeData1.isDirectory ();
    boolean thatFileIsDirectory = nodeData2.isDirectory ();

    if (thisFileIsDirectory && !thatFileIsDirectory)
      return 1;
    if (!thisFileIsDirectory && thatFileIsDirectory)
      return -1;

    return nodeData1.name.compareToIgnoreCase (nodeData2.name);
  }
}
