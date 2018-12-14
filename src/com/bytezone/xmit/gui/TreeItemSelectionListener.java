package com.bytezone.xmit.gui;

import java.io.File;

import com.bytezone.xmit.Reader;

import javafx.scene.control.TreeItem;

public interface TreeItemSelectionListener
{
  public void treeItemSelected (Reader reader);

  public void treeItemExpanded (TreeItem<File> treeItem);
}