package com.bytezone.xmit.gui;

import com.bytezone.xmit.Dataset;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

// ---------------------------------------------------------------------------------//
class TablePane extends HeaderPane implements TreeItemSelectionListener
//---------------------------------------------------------------------------------//
{
  private final Label lblPdsInfo = new Label ();
  private final Label lblFileName = new Label ();

  // ---------------------------------------------------------------------------------//
  public TablePane (XmitTable table)
  // ---------------------------------------------------------------------------------//
  {
    setCenter (table);
    setTop (getHBox (lblFileName, lblPdsInfo));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  // ---------------------------------------------------------------------------------//
  {
    if (dataset == null)
      lblFileName.setText ("");
    else
    {
      lblFileName.setText (dataset.getReader ().getFileName ());

      if (dataset.getReader ().isIncomplete ())
        lblFileName.setTextFill (Color.RED);
      else
        lblFileName.setTextFill (Color.BLACK);
    }
  }
}
