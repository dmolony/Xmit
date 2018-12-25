package com.bytezone.xmit.gui;

import java.util.Comparator;

import javafx.scene.control.TreeItem;

class FileComparator implements Comparator<TreeItem<XmitFile>>
{
  @Override
  public int compare (TreeItem<XmitFile> thisFile, TreeItem<XmitFile> thatFile)
  {
    boolean thisFileIsDirectory = thisFile.getValue ().isDirectory ();
    boolean thatFileIsDirectory = thatFile.getValue ().isDirectory ();

    if (thisFileIsDirectory && !thatFileIsDirectory)
      return 1;
    if (!thisFileIsDirectory && thatFileIsDirectory)
      return -1;

    return thisFile.getValue ().getName ()
        .compareToIgnoreCase (thatFile.getValue ().getName ());
  }
}