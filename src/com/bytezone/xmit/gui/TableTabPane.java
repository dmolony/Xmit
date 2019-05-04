package com.bytezone.xmit.gui;

import javafx.scene.input.KeyCode;

// ----------------------------------------------------------------------------------- //
class TableTabPane extends XmitTabPane
//----------------------------------------------------------------------------------- //
{
  final MembersTab tableTab = new MembersTab ("Members", KeyCode.M);
  final HeadersTab headersTab = new HeadersTab ("Headers", KeyCode.H);
  final CommentsTab commentsTab = new CommentsTab ("Comments", KeyCode.C);

  //----------------------------------------------------------------------------------- //
  public TableTabPane (String prefsId)
  //----------------------------------------------------------------------------------- //
  {
    super (prefsId);

    add (headersTab);
    add (tableTab);
    add (commentsTab);

    getTabs ().addAll (headersTab, tableTab, commentsTab);
  }

}
