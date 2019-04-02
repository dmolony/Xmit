package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.PdsMember;

import javafx.scene.input.KeyCode;

public class BlocksTab extends XmitTab
{

  //----------------------------------------------------------------------------------- //
  public BlocksTab (OutputPane parent, String title, KeyCode keyCode)
  //----------------------------------------------------------------------------------- //
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

    if (parent.dataFile instanceof PdsMember)
      ((PdsMember) parent.dataFile).listSizeCounts (lines);
    lines.add (parent.dataFile.toString ());

    return lines;
  }
}
