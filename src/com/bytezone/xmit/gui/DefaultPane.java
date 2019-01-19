package com.bytezone.xmit.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

abstract class DefaultPane extends BorderPane
{
  static Font headingFont = Font.font ("Lucida Sans Typewriter", 14);
  static Font monospacedFont = Font.font ("Monospaced", 13);

  // ---------------------------------------------------------------------------------//
  // getHBox
  // ---------------------------------------------------------------------------------//

  HBox getHBox (Label label1, Label label2)
  {
    HBox hbox = new HBox (10);
    hbox.setPrefHeight (20);
    hbox.setAlignment (Pos.CENTER_LEFT);
    hbox.setPadding (new Insets (6, 10, 6, 10));
    Region filler = new Region ();
    HBox.setHgrow (filler, Priority.ALWAYS);
    hbox.getChildren ().addAll (label1, filler, label2);

    label1.setFont (headingFont);
    label2.setFont (headingFont);
    label2.setAlignment (Pos.CENTER_RIGHT);

    return hbox;
  }

  // ---------------------------------------------------------------------------------//
  // createTab
  // ---------------------------------------------------------------------------------//

  XmitTab createTab (String title, KeyCode keyCode)
  {
    Tab tab = new Tab (title);
    TextArea text = new TextArea ();
    tab.setContent (text);
    text.setFont (monospacedFont);
    text.setEditable (false);
    text.setWrapText (false);
    return new XmitTab (tab, text, keyCode);
  }
}
