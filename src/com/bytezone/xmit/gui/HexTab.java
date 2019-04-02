package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.Utility;

import javafx.scene.input.KeyCode;

public class HexTab extends XmitTab
{
  private static final int MAX_HEX_BYTES = 0x20_000;

  public HexTab (OutputPane parent, String title, KeyCode keyCode)
  {
    super (parent, title, keyCode);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  List<String> getLines ()
  //----------------------------------------------------------------------------------- //
  {
    List<String> lines = new ArrayList<> ();
    if (parent.dataFile == null)
      return lines;

    byte[] buffer = parent.dataFile.getDataBuffer ();
    return Utility.getHexDumpLines (buffer, 0, Math.min (MAX_HEX_BYTES, buffer.length));
  }
}
