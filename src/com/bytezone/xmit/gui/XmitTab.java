package com.bytezone.xmit.gui;

import javafx.geometry.Orientation;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;

public class XmitTab
{
  final Tab tab;
  final TextArea textArea;
  final KeyCode keyCode;
  final ScrollBarState scrollBarState;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public XmitTab (Tab tab, TextArea textArea, KeyCode keyCode)
  {
    this.tab = tab;
    this.textArea = textArea;
    this.keyCode = keyCode;
    scrollBarState = new ScrollBarState (textArea, Orientation.VERTICAL);
  }

  // ---------------------------------------------------------------------------------//
  // isTextEmpty
  // ---------------------------------------------------------------------------------//

  boolean isTextEmpty ()
  {
    return textArea.getText ().isEmpty ();
  }

  // ---------------------------------------------------------------------------------//
  // setText
  // ---------------------------------------------------------------------------------//

  void setText (String text)
  {
    textArea.setText (text);
  }

  // ---------------------------------------------------------------------------------//
  // saveScrollBar
  // ---------------------------------------------------------------------------------//

  void saveScrollBar ()
  {
    scrollBarState.save ();
  }

  // ---------------------------------------------------------------------------------//
  // restoreScrollBar
  // ---------------------------------------------------------------------------------//

  void restoreScrollBar ()
  {
    scrollBarState.restore ();
  }
}