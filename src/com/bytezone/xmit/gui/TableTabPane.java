package com.bytezone.xmit.gui;

import com.bytezone.appbase.TabPaneBase;

import javafx.scene.input.KeyCode;

// -----------------------------------------------------------------------------------//
class TableTabPane extends TabPaneBase
// -----------------------------------------------------------------------------------//
{
  final HeadersTab headersTab = new HeadersTab ("Headers", KeyCode.H);
  final CommentsTab commentsTab = new CommentsTab ("Comments", KeyCode.C);
  final MembersTab membersTab = new MembersTab ("Members", KeyCode.M);

  // ---------------------------------------------------------------------------------//
  public TableTabPane (String prefsId)
  // ---------------------------------------------------------------------------------//
  {
    super (prefsId);

    add (headersTab);
    add (commentsTab);
    add (membersTab);

    setDefaultTab (2);
  }
}
