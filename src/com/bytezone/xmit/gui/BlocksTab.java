package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.PdsMember;
import com.bytezone.xmit.gui.XmitTree.NodeDataListener;

import javafx.scene.input.KeyCode;

// -----------------------------------------------------------------------------------//
class BlocksTab extends XmitTextTab
    implements NodeDataListener, TableItemSelectionListener
// -----------------------------------------------------------------------------------//
{
  private NodeData nodeData;
  private CatalogEntry catalogEntry;

  // ---------------------------------------------------------------------------------//
  public BlocksTab (String title, KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    super (title, keyCode);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  List<String> getLines ()
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = new ArrayList<> ();

    if (nodeData == null || !nodeData.isDataFile ())
      return lines;

    if (nodeData.isPartitionedDataset ())
      ((PdsMember) nodeData.getDataFile ()).listSizeCounts (lines);

    lines.add (nodeData.getDataFile ().toString ());

    return lines;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void nodeSelected (NodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    this.nodeData = nodeData;
    refresh ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    this.catalogEntry = catalogEntry;
    refresh ();
  }
}
