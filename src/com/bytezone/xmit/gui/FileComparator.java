package com.bytezone.xmit.gui;

import java.util.Comparator;

import javafx.scene.control.TreeItem;

// -----------------------------------------------------------------------------------//
class FileComparator implements Comparator<TreeItem<NodeData>>
//-----------------------------------------------------------------------------------//
{
  @Override
  public int compare (TreeItem<NodeData> thisFile, TreeItem<NodeData> thatFile)
  {
    NodeData nodeData1 = thisFile.getValue ();
    NodeData nodeData2 = thatFile.getValue ();

    boolean thisFileIsDirectory = nodeData1.isDirectory ();
    boolean thatFileIsDirectory = nodeData2.isDirectory ();

    if (thisFileIsDirectory && !thatFileIsDirectory)
      return 1;
    if (!thisFileIsDirectory && thatFileIsDirectory)
      return -1;

    return nodeData1.getName ().compareToIgnoreCase (nodeData2.getName ());
  }
}