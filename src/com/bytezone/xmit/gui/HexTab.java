package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.Utility;

import javafx.scene.input.KeyCode;

// ----------------------------------------------------------------------------------- //
class HexTab extends XmitTextTab implements TreeItemSelectionListener,
    TableItemSelectionListener, CodePageSelectedListener
//----------------------------------------------------------------------------------- //
{
  private static final int MAX_HEX_BYTES = 0x20_000;
  DatasetStatus datasetStatus;

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

    if (datasetStatus == null || datasetStatus.getDataFile () == null)
      return lines;

    byte[] buffer = datasetStatus.getDataBuffer ();
    return Utility.getHexDumpLines (buffer, 0, Math.min (MAX_HEX_BYTES, buffer.length));
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
    this.datasetStatus = datasetStatus;
    refresh ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void tableItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
    this.datasetStatus = datasetStatus;
    refresh ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void selectCodePage (String codePageName)
  //----------------------------------------------------------------------------------- //
  {
    refresh ();
  }
}
