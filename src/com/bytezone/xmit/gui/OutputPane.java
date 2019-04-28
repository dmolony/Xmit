package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

import javafx.scene.input.KeyCode;

// ------------------------------------------------------------------------------------ //
class OutputPane extends XmitTabPane implements SaveState
//------------------------------------------------------------------------------------- //
{
  private static final String PREFS_LAST_TAB = "lastTab";

  final HeadersTab headersTab = new HeadersTab ("Headers", KeyCode.H);
  final BlocksTab blocksTab = new BlocksTab ("Blocks", KeyCode.B);
  final HexTab hexTab = new HexTab ("Hex", KeyCode.X);
  final OutputTab outputTab = new OutputTab ("Output", KeyCode.O);

  //----------------------------------------------------------------------------------- //
  OutputPane ()
  //----------------------------------------------------------------------------------- //
  {
    add (headersTab);
    add (blocksTab);
    add (hexTab);
    add (outputTab);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void restore (Preferences prefs)
  //----------------------------------------------------------------------------------- //
  {
    getSelectionModel ().select (prefs.getInt (PREFS_LAST_TAB, 0));
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void save (Preferences prefs)
  //----------------------------------------------------------------------------------- //
  {
    prefs.putInt (PREFS_LAST_TAB, getSelectionModel ().getSelectedIndex ());
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

  //----------------------------------------------------------------------------------- //
  public void selectCodePage ()
  //----------------------------------------------------------------------------------- //
  {
    updateCurrentTab ();
  }
}
