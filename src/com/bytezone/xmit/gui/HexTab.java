package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Utility;

import javafx.scene.input.KeyCode;

// ----------------------------------------------------------------------------------- //
class HexTab extends XmitTab
    implements TreeItemSelectionListener, TableItemSelectionListener
//----------------------------------------------------------------------------------- //
{
  private static final int MAX_HEX_BYTES = 0x20_000;
  Dataset dataset;                // usually file #1 in the Reader
  DataFile dataFile;              // FlatFile or PdsMember
  CatalogEntry catalogEntry;      // needed for alias members

  //----------------------------------------------------------------------------------- //
  public HexTab (String title, KeyCode keyCode)
  //----------------------------------------------------------------------------------- //
  {
    super (title, keyCode);
    textFormatter = new TextFormatterHex ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  List<String> getLines ()
  //----------------------------------------------------------------------------------- //
  {
    List<String> lines = new ArrayList<> ();

    //    DataFile dataFile = parent.dataFile;              // improve this
    if (dataFile == null)
      return lines;

    byte[] buffer = dataFile.getDataBuffer ();
    return Utility.getHexDumpLines (buffer, 0, Math.min (MAX_HEX_BYTES, buffer.length));
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  //----------------------------------------------------------------------------------- //
  {
    this.dataset = dataset;
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  //----------------------------------------------------------------------------------- //
  {
    if (dataset == null || dataset.isPs ())
      return;

    this.catalogEntry = catalogEntry;
    dataFile = catalogEntry == null ? null : catalogEntry.getMember ();
  }
}
