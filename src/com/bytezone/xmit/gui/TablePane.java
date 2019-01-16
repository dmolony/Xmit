package com.bytezone.xmit.gui;

import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Reader;

import javafx.scene.control.Label;

public class TablePane extends DefaultPane implements TreeItemSelectionListener
{
  private final Label lblPdsInfo = new Label ();
  private final Label lblFileName = new Label ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public TablePane (XmitTable table)
  {
    setCenter (table);
    setTop (getHBox (lblFileName, lblPdsInfo));
  }

  // ---------------------------------------------------------------------------------//
  // treeItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemSelected (Reader reader, Dataset dataset, String name, String path)
  {
    if (reader == null)
      lblFileName.setText ("");
    else
      lblFileName.setText (reader.getFileName ());
  }
}
