package com.bytezone.xmit.gui;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.gui.XmitTree.NodeDataListener;

// -----------------------------------------------------------------------------------//
class OutputHeaderBar extends HeaderBar
    implements NodeDataListener, TableItemSelectionListener, ShowLinesListener
// -----------------------------------------------------------------------------------//
{
  private LineDisplayStatus lineDisplayStatus;
  private NodeData nodeData;
  private CatalogEntry catalogEntry;

  // ---------------------------------------------------------------------------------//
  void updateNameLabel (boolean truncateLines)
  // ---------------------------------------------------------------------------------//
  {
    if (nodeData == null || !nodeData.isDataset () || catalogEntry == null)
    {
      leftLabel.setText ("");
      return;
    }

    String indicator = truncateLines ? "<-" : "";

    if (nodeData.isPartitionedDataset ())
    {
      String memberName = indicator + catalogEntry.getMemberName ();
      if (catalogEntry.isAlias ())
        leftLabel.setText (memberName + " -> " + catalogEntry.getAliasName ());
      else
        leftLabel.setText (memberName);
    }
    else
      leftLabel.setText (indicator + nodeData.name);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void showLinesSelected (LineDisplayStatus lineDisplayStatus)
  // ---------------------------------------------------------------------------------//
  {
    this.lineDisplayStatus = lineDisplayStatus;
    updateNameLabel (lineDisplayStatus.truncateLines);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    this.catalogEntry = catalogEntry;
    updateNameLabel (lineDisplayStatus.truncateLines);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void nodeSelected (NodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    this.nodeData = nodeData;

    rightLabel
        .setText (nodeData.isDataset () ? nodeData.getDisposition ().toString () : "");
    updateNameLabel (lineDisplayStatus.truncateLines);
  }
}
