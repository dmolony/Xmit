package com.bytezone.xmit.gui;

import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Reader;

import javafx.scene.paint.Color;

// ---------------------------------------------------------------------------------//
public class TableHeaderBar extends HeaderBar
    implements TreeItemSelectionListener, FilterChangeListener
// ---------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  // ---------------------------------------------------------------------------------//
  {
    if (dataset == null)
      leftLabel.setText ("");
    else
    {
      Reader reader = dataset.getReader ();
      leftLabel.setText (reader.getFileName ());
      leftLabel.setTextFill (reader.isIncomplete () ? Color.RED : Color.BLACK);
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (String filter, boolean fullFilter)
  // ---------------------------------------------------------------------------------//
  {
    rightLabel.setText (filter.isEmpty () ? "" : "filter: " + filter);
  }
}
