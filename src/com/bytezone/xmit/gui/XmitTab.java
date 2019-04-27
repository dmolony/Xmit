package com.bytezone.xmit.gui;

import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;

public class XmitTab extends Tab
{
  final KeyCode keyCode;
  Font font;

  // ---------------------------------------------------------------------------------//
  public XmitTab (String title, KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    super (title);

    this.keyCode = keyCode;
  }

  // ---------------------------------------------------------------------------------//
  void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    this.font = font;
  }
}
