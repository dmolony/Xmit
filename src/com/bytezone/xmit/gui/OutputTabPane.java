package com.bytezone.xmit.gui;

import com.bytezone.appbase.TabPaneBase;

import javafx.scene.input.KeyCode;

// -----------------------------------------------------------------------------------//
class OutputTabPane extends TabPaneBase
// -----------------------------------------------------------------------------------//
{
  final BlocksTab blocksTab = new BlocksTab ("Blocks", KeyCode.B);
  final HexTab hexTab = new HexTab ("Hex", KeyCode.X);
  final OutputTab outputTab = new OutputTab ("Output", KeyCode.O);

  // ---------------------------------------------------------------------------------//
  OutputTabPane (String prefsId)
  // ---------------------------------------------------------------------------------//
  {
    super (prefsId);

    add (blocksTab);
    add (hexTab);
    add (outputTab);

    setDefaultTab (2);
  }
}
