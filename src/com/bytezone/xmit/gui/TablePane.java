package com.bytezone.xmit.gui;

import javafx.scene.layout.BorderPane;

// ---------------------------------------------------------------------------------//
class TablePane extends BorderPane
//---------------------------------------------------------------------------------//
{
  final TableHeaderBar tableHeaderBar = new TableHeaderBar ();

  // ---------------------------------------------------------------------------------//
  public TablePane (XmitTable table)
  // ---------------------------------------------------------------------------------//
  {
    setCenter (table);
    setTop (tableHeaderBar);
  }
}
