package com.bytezone.xmit.gui;

import java.util.Comparator;

import javafx.scene.control.TreeItem;

// ---------------------------------------------------------------------------------//
class FileComparator implements Comparator<TreeItem<XmitFile>>
//---------------------------------------------------------------------------------//
{
  @Override
  public int compare (TreeItem<XmitFile> thisFile, TreeItem<XmitFile> thatFile)
  {
    XmitFile thisXmitFile = thisFile.getValue ();
    XmitFile thatXmitFile = thatFile.getValue ();

    boolean thisFileIsDirectory = thisXmitFile.isDirectory ();
    boolean thatFileIsDirectory = thatXmitFile.isDirectory ();

    if (thisFileIsDirectory && !thatFileIsDirectory)
      return 1;
    if (!thisFileIsDirectory && thatFileIsDirectory)
      return -1;

    return thisXmitFile.getName ().compareToIgnoreCase (thatXmitFile.getName ());
  }
}