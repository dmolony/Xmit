package com.bytezone.xmit.gui;

import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.gui.XmitTree.TreeNodeListener;

import javafx.scene.paint.Color;

// -----------------------------------------------------------------------------------//
class TableHeaderBar extends HeaderBar
    implements TreeNodeListener, FilterActionListener, FilterChangeListener
// -----------------------------------------------------------------------------------//
{
  private TreeNodeData nodeData;
  private FilterStatus filterStatus;
  private int found;
  private int max;

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeNodeSelected (TreeNodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    this.nodeData = nodeData;

    if (nodeData.isDataset ())
    {
      leftLabel.setText (nodeData.getDatasetName ());
      leftLabel
          .setTextFill (nodeData.getReader ().isIncomplete () ? Color.RED : Color.BLACK);
      setMembersLabel ();
    }
    else
    {
      leftLabel.setText ("");
      rightLabel.setText ("");
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
    if (nodeData != null && nodeData.isPartitionedDataset ())
    {
      int members = ((PdsDataset) nodeData.getDataset ()).getCatalogEntries ().size ();

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
