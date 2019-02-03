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
  public void treeItemSelected (Dataset dataset, String name, String path)
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
