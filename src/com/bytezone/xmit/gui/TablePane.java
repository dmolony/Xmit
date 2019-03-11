package com.bytezone.xmit.gui;

import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Reader;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

// ---------------------------------------------------------------------------------//
class TablePane extends HeaderPane implements TreeItemSelectionListener, FilterListener
//---------------------------------------------------------------------------------//
{
  private final Label lblFileName = new Label ();
  private final Label lblFilter = new Label ();

  // ---------------------------------------------------------------------------------//
  public TablePane (XmitTable table)
  // ---------------------------------------------------------------------------------//
  {
    setCenter (table);
    setTop (getHBox (lblFileName, lblFilter));
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
      Reader reader = dataset.getReader ();
      lblFileName.setText (reader.getFileName ());
      lblFileName.setTextFill (reader.isIncomplete () ? Color.RED : Color.BLACK);
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (String filter)
  // ---------------------------------------------------------------------------------//
  {
    lblFilter.setText (filter.isEmpty () ? "" : "filter: " + filter);
  }
}
