package com.bytezone.xmit.gui;

import javafx.scene.input.KeyCode;

// ------------------------------------------------------------------------------------ //
class OutputTabPane extends XmitTabPane implements SaveState
//------------------------------------------------------------------------------------- //
{
  final HeadersTab headersTab = new HeadersTab ("Headers", KeyCode.H);
  final BlocksTab blocksTab = new BlocksTab ("Blocks", KeyCode.B);
  final HexTab hexTab = new HexTab ("Hex", KeyCode.X);
  final OutputTab outputTab = new OutputTab ("Output", KeyCode.O);

  //----------------------------------------------------------------------------------- //
  OutputTabPane (String prefsId)
  //----------------------------------------------------------------------------------- //
  {
    super (prefsId);

    add (headersTab);
    add (blocksTab);
    add (hexTab);
    add (outputTab);
  }

  //----------------------------------------------------------------------------------- //
  void setTabVisible (boolean headersVisible, boolean blocksVisible, boolean hexVisible)
  //----------------------------------------------------------------------------------- //
  {
    getTabs ().clear ();

    if (headersVisible)
      getTabs ().add (headersTab);
    if (blocksVisible)
      getTabs ().add (blocksTab);
    if (hexVisible)
      getTabs ().add (hexTab);

    getTabs ().add (outputTab);         // always visible
  }
}
