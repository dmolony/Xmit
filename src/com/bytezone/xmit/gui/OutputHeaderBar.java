package com.bytezone.xmit.gui;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.gui.XmitTree.TreeNodeListener;

// -----------------------------------------------------------------------------------//
class OutputHeaderBar extends HeaderBar
    implements TreeNodeListener, TableItemSelectionListener, ShowLinesListener
// -----------------------------------------------------------------------------------//
{
  private LineDisplayStatus lineDisplayStatus;
  private TreeNodeData nodeData;
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
      leftLabel.setText (indicator + nodeData.getName ());
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
  public void treeNodeSelected (TreeNodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    this.nodeData = nodeData;

    rightLabel
        .setText (nodeData.isDataset () ? nodeData.getDisposition ().toString () : "");
    updateNameLabel (lineDisplayStatus.truncateLines);
  }
}
