package com.bytezone.xmit.gui;

import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Reader;

public interface TreeItemSelectionListener
{
  public void treeItemSelected (Reader reader, Dataset dataset, String name, String path);
}