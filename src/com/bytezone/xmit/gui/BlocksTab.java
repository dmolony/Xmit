package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.PdsMember;
import com.bytezone.xmit.gui.XmitTree.TreeNodeListener;

import javafx.scene.input.KeyCode;

// -----------------------------------------------------------------------------------//
class BlocksTab extends XmitTextTab implements TreeNodeListener, TableItemSelectionListener
// -----------------------------------------------------------------------------------//
{
  private TreeNodeData nodeData;
  private DataFile dataFile;

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

    if (dataFile == null)
      return lines;

    if (nodeData.isPartitionedDataset ())
    {
      PdsMember member = ((PdsMember) dataFile);
      member.listSizeCounts (lines);
      member.getText (lines);
      //        lines.add (member.getText ());        // should split
    }
    else
      lines.add (dataFile.toString ());     // should split

    return lines;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeNodeSelected (TreeNodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    this.nodeData = nodeData;

    if (nodeData.isPhysicalSequentialDataset ())
      dataFile = nodeData.getDataFile ();
    else
      dataFile = null;

    refresh ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    dataFile = catalogEntry == null ? null : catalogEntry.getMember ();
    refresh ();
  }
}
