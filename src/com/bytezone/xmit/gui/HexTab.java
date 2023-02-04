package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Utility;
import com.bytezone.xmit.gui.XmitTree.TreeNodeListener;

import javafx.scene.input.KeyCode;

// -----------------------------------------------------------------------------------//
class HexTab extends XmitTextTab
    implements TreeNodeListener, TableItemSelectionListener, CodePageSelectedListener
// -----------------------------------------------------------------------------------//
{
  private static final int MAX_HEX_BYTES = 0x20_000;

  TreeNodeData nodeData;
  DataFile dataFile;

  // ---------------------------------------------------------------------------------//
  public HexTab (String title, KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    super (title, keyCode);

    textFormatter = new TextFormatterHex ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  List<String> getLines ()
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = new ArrayList<> ();

    if (dataFile == null)
      return lines;

    byte[] buffer = dataFile.getDataBuffer ();

    return Utility.getHexDumpLines (buffer, 0, Math.min (MAX_HEX_BYTES, buffer.length));
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

  // ---------------------------------------------------------------------------------//
  @Override
  public void selectCodePage (String codePageName)
  // ---------------------------------------------------------------------------------//
  {
    refresh ();
  }
}
