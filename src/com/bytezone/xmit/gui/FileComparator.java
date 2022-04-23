package com.bytezone.xmit.gui;

import java.util.Comparator;

import javafx.scene.control.TreeItem;

// -----------------------------------------------------------------------------------//
class FileComparator implements Comparator<TreeItem<TreeNodeData>>
//-----------------------------------------------------------------------------------//
{
  @Override
  public int compare (TreeItem<TreeNodeData> thisFile, TreeItem<TreeNodeData> thatFile)
  {
    TreeNodeData nodeData1 = thisFile.getValue ();
    TreeNodeData nodeData2 = thatFile.getValue ();

    boolean thisFileIsDirectory = nodeData1.isDirectory ();
    boolean thatFileIsDirectory = nodeData2.isDirectory ();

    if (thisFileIsDirectory && !thatFileIsDirectory)
      return 1;
    if (!thisFileIsDirectory && thatFileIsDirectory)
      return -1;

    return nodeData1.getName ().compareToIgnoreCase (nodeData2.getName ());
  }
}