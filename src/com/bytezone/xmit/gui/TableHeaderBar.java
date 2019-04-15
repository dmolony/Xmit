package com.bytezone.xmit.gui;

import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Filter.FilterMode;
import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.Reader;

import javafx.scene.paint.Color;

// ---------------------------------------------------------------------------------//
public class TableHeaderBar extends HeaderBar
    implements TreeItemSelectionListener, FilterActionListener, FilterChangeListener
// ---------------------------------------------------------------------------------//
{
  String filterValue;
  Dataset dataset;

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  // ---------------------------------------------------------------------------------//
  {
    this.dataset = dataset;

    if (dataset == null)
      leftLabel.setText ("");
    else
    {
      Reader reader = dataset.getReader ();
      leftLabel.setText (reader.getFileName ());
      leftLabel.setTextFill (reader.isIncomplete () ? Color.RED : Color.BLACK);
      setMembersLabel ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private void setMembersLabel ()
  // ---------------------------------------------------------------------------------//
  {
    if (dataset != null && dataset.isPds ())
    {
      int members = ((PdsDataset) dataset).getCatalogEntries ().size ();
      rightLabel
          .setText (String.format ("%d member%s", members, members == 1 ? "" : "s"));
    }
    else
      rightLabel.setText ("");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void filtering (int found, int max, boolean done)
  // ---------------------------------------------------------------------------------//
  {
    if (done && !filterValue.isEmpty ())
    {
      //      if (filterValue.isEmpty ())
      //      {
      //        rightLabel.setText (String.format ("%d member%s", max, max == 1 ? "" : "s"));
      //      }
      //      else
      rightLabel
          .setText (String.format ("%d / %d member%s", found, max, max == 1 ? "" : "s"));
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (String filter, boolean fullFilter, FilterMode filterMode)
  // ---------------------------------------------------------------------------------//
  {
    this.filterValue = filter;
    setMembersLabel ();
  }
}
