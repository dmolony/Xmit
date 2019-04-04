package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Utility;

import javafx.scene.input.KeyCode;

public class HexTab extends XmitTab
{
  private static final int MAX_HEX_BYTES = 0x20_000;

  public HexTab (OutputPane parent, String title, KeyCode keyCode)
  {
    super (title, parent, keyCode);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  List<String> getLines ()
  //----------------------------------------------------------------------------------- //
  {
    List<String> lines = new ArrayList<> ();

    DataFile dataFile = parent.dataFile;
    if (dataFile == null)
      return lines;

    byte[] buffer = dataFile.getDataBuffer ();
    return Utility.getHexDumpLines (buffer, 0, Math.min (MAX_HEX_BYTES, buffer.length));
  }
}
