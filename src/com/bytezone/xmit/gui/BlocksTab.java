package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.PdsMember;

import javafx.scene.input.KeyCode;

// ----------------------------------------------------------------------------------- //
class BlocksTab extends XmitTab
//----------------------------------------------------------------------------------- //
{
  //----------------------------------------------------------------------------------- //
  public BlocksTab (OutputPane parent, String title, KeyCode keyCode)
  //----------------------------------------------------------------------------------- //
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

    if (dataFile instanceof PdsMember)
      ((PdsMember) dataFile).listSizeCounts (lines);

    lines.add (dataFile.toString ());

    return lines;
  }
}
