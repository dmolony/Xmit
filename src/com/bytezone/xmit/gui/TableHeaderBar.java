package com.bytezone.xmit.gui;

import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.Reader;

import javafx.scene.paint.Color;

// ---------------------------------------------------------------------------------//
class TableHeaderBar extends HeaderBar
    implements TreeItemSelectionListener, FilterActionListener, FilterChangeListener
// ---------------------------------------------------------------------------------//
{
  private DatasetStatus datasetStatus;
  private FilterStatus filterStatus;
  private int found;
  private int max;

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (DatasetStatus datasetStatus)
  // ---------------------------------------------------------------------------------//
  {
    this.datasetStatus = datasetStatus;

    if (datasetStatus.dataset == null)
    {
      leftLabel.setText ("");
      rightLabel.setText ("");
    }
    else
    {
      Reader reader = datasetStatus.dataset.getReader ();
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
    if (!done || filterStatus.filterValue.isEmpty ())
      return;

    this.found = found;
    this.max = max;

    setMembersLabel ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (FilterStatus filterStatus)
  // ---------------------------------------------------------------------------------//
  {
    this.filterStatus = filterStatus;
    setMembersLabel ();
  }

  // ---------------------------------------------------------------------------------//
  private void setMembersLabel ()
  // ---------------------------------------------------------------------------------//
  {
    if (datasetStatus != null && datasetStatus.isPds ())
    {
      int members = ((PdsDataset) datasetStatus.dataset).getCatalogEntries ().size ();

      if (filterStatus.filterValue.isEmpty () || !filterStatus.filterActive)
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
