package com.bytezone.xmit.gui;

import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;

// ----------------------------------------------------------------------------------- //
public class TableTab extends XmitTableTab
//----------------------------------------------------------------------------------- //
{
  XmitTable xmitTable = new XmitTable ();

  //----------------------------------------------------------------------------------- //
  public TableTab (String title, KeyCode keyCode)
  //----------------------------------------------------------------------------------- //
  {
    super (title, keyCode);

    setContent (xmitTable);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    super.setFont (font);

    xmitTable.setFont (font);
  }

}
