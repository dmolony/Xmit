package com.bytezone.xmit.gui;

import javafx.scene.input.KeyCode;

// ----------------------------------------------------------------------------------- //
class TableTabPane extends XmitTabPane
//----------------------------------------------------------------------------------- //
{
  final MembersTab tableTab = new MembersTab ("Members", KeyCode.M);
  final HeadersTab headersTab = new HeadersTab ("Headers", KeyCode.H);

  //----------------------------------------------------------------------------------- //
  public TableTabPane (String prefsId)
  //----------------------------------------------------------------------------------- //
  {
    super (prefsId);

    add (headersTab);
    add (tableTab);

    getTabs ().addAll (headersTab, tableTab);
  }

}
