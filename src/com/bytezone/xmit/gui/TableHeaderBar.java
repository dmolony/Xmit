package com.bytezone.xmit.gui;

import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.Reader;

import javafx.scene.paint.Color;

// ---------------------------------------------------------------------------------//
public class TableHeaderBar extends HeaderBar
    implements TreeItemSelectionListener, FilterActionListener, FilterChangeListener
// ---------------------------------------------------------------------------------//
{
  private String filterValue;
  private Dataset dataset;
  private boolean filterReverse;
  private int found;
  private int max;

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  // ---------------------------------------------------------------------------------//
  {
    this.dataset = dataset;

    if (dataset == null)
    {
      leftLabel.setText ("");
      rightLabel.setText ("");
    }
    else
    {
      Reader reader = dataset.getReader ();
      leftLabel.setText (reader.getFileName ());
      leftLabel.setTextFill (reader.isIncomplete () ? Color.RED : Color.BLACK);
      setMembersLabel ();
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void filtering (int found, int max, boolean done)
  // ---------------------------------------------------------------------------------//
  {
    if (!done || filterValue.isEmpty ())
      return;

    this.found = found;
    this.max = max;

    setMembersLabel ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (String filter, boolean fullFilter, boolean filterReverse)
  // ---------------------------------------------------------------------------------//
  {
    this.filterValue = filter;
    //    this.fullFilter = fullFilter;
    this.filterReverse = filterReverse;

    setMembersLabel ();
  }

  // ---------------------------------------------------------------------------------//
  private void setMembersLabel ()
  // ---------------------------------------------------------------------------------//
  {
    if (dataset != null && dataset.isPds ())
    {
      int members = ((PdsDataset) dataset).getCatalogEntries ().size ();

      if (filterValue.isEmpty ())
        rightLabel
            .setText (String.format ("%d member%s", members, members == 1 ? "" : "s"));
      else
        rightLabel.setText (
            String.format ("%d / %d member%s", found, max, max == 1 ? "" : "s"));
    }
    else
      rightLabel.setText ("");
  }
}
