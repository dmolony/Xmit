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
  public XmitTab (String title, TextArea textArea, KeyCode keyCode, Runnable tabUpdater)
  // ---------------------------------------------------------------------------------//
  {
    this.tab = new Tab (title, textArea);
    this.textArea = textArea;
    this.keyCode = keyCode;
    this.tabUpdater = tabUpdater;
    scrollBarState = new ScrollBarState (textArea, Orientation.VERTICAL);
    tab.setUserData (this);
  }

  // ---------------------------------------------------------------------------------//
  void update ()
  // ---------------------------------------------------------------------------------//
  {
    if (isTextEmpty ())
      tabUpdater.run ();
  }

  // ---------------------------------------------------------------------------------//
  boolean isTextEmpty ()
  // ---------------------------------------------------------------------------------//
  {
    return textArea.getText ().isEmpty ();
  }

  // ---------------------------------------------------------------------------------//
  void setText (String text)
  // ---------------------------------------------------------------------------------//
  {
    textArea.setText (text);
  }

  // ---------------------------------------------------------------------------------//
  void appendText (String text)
  // ---------------------------------------------------------------------------------//
  {
    textArea.appendText (text);
  }

  // ---------------------------------------------------------------------------------//
  void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    textArea.setFont (font);
  }

  // ---------------------------------------------------------------------------------//
  void saveScrollBar ()
  // ---------------------------------------------------------------------------------//
  {
    scrollBarState.save ();
  }

  // ---------------------------------------------------------------------------------//
  void restoreScrollBar ()
  // ---------------------------------------------------------------------------------//
  {
    scrollBarState.restore ();
  }
}