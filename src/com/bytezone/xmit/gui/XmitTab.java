package com.bytezone.xmit.gui;

import javafx.geometry.Orientation;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
class XmitTab
//---------------------------------------------------------------------------------//
{
  final Tab tab;
  final TextArea textArea;
  final KeyCode keyCode;
  final ScrollBarState scrollBarState;
  final Runnable tabUpdater;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public XmitTab (String title, TextArea textArea, KeyCode keyCode, Runnable tabUpdater)
  {
    this.tab = new Tab (title, textArea);
    this.textArea = textArea;
    this.keyCode = keyCode;
    this.tabUpdater = tabUpdater;
    scrollBarState = new ScrollBarState (textArea, Orientation.VERTICAL);
    tab.setUserData (this);
  }

  // ---------------------------------------------------------------------------------//
  // update
  // ---------------------------------------------------------------------------------//

  void update ()
  {
    if (isTextEmpty ())
      tabUpdater.run ();
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
  // setFont
  // ---------------------------------------------------------------------------------//

  void setFont (Font font)
  {
    textArea.setFont (font);
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