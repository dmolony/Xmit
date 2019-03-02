package com.bytezone.xmit.gui;

import java.util.function.Supplier;

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
  final Supplier<String> textSupplier;

  // ---------------------------------------------------------------------------------//
  public XmitTab (String title, TextArea textArea, KeyCode keyCode,
      Supplier<String> textSupplier)
  // ---------------------------------------------------------------------------------//
  {
    this.tab = new Tab (title, textArea);
    this.textArea = textArea;
    this.keyCode = keyCode;
    this.textSupplier = textSupplier;
    scrollBarState = new ScrollBarState (textArea, Orientation.VERTICAL);
    tab.setUserData (this);
  }

  // ---------------------------------------------------------------------------------//
  void update ()
  // ---------------------------------------------------------------------------------//
  {
    if (textArea.getText ().isEmpty ())
      textArea.setText (textSupplier.get ());
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