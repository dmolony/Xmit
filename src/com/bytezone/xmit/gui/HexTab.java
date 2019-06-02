package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Utility;
import com.bytezone.xmit.gui.XmitTree.NodeDataListener;

import javafx.scene.input.KeyCode;

// -----------------------------------------------------------------------------------//
class HexTab extends XmitTextTab
    implements NodeDataListener, TableItemSelectionListener, CodePageSelectedListener
// -----------------------------------------------------------------------------------//
{
  private static final int MAX_HEX_BYTES = 0x20_000;

  NodeData nodeData;
  CatalogEntry catalogEntry;

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

    if (nodeData == null || !nodeData.isDataFile ())
      return lines;

    byte[] buffer = nodeData.getDataFile ().getDataBuffer ();
    return Utility.getHexDumpLines (buffer, 0, Math.min (MAX_HEX_BYTES, buffer.length));
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

  // ---------------------------------------------------------------------------------//
  @Override
  public void selectCodePage (String codePageName)
  // ---------------------------------------------------------------------------------//
  {
    refresh ();
  }
}
