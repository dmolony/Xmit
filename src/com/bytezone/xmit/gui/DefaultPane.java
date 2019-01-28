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
  //  static String[] fontNames =
  //      { "A2like", "Andale Mono", "Anonymous Pro", "Apple2Forever", "Apple2Forever80",
  //        "Consolas", "Courier", "Courier New", "Fira Code", "IBM Plex Mono", "Iosevka",
  //        "Menlo", "Monaco", "Monospaced", "PR Number 3", "PT Mono", "Print Char 21",
  //        "VT220-mod" };
  static Font headingFont = Font.font ("Lucida Sans Typewriter", 14);
  //  static Font monospacedFont = Font.font (fontNames[17], 13);

  // Courier

  // ---------------------------------------------------------------------------------//
  // getHBox
  // ---------------------------------------------------------------------------------//

  HBox getHBox (Label label1, Label label2)
  {
    HBox hbox = getHBox (label1);

    Region filler = new Region ();
    HBox.setHgrow (filler, Priority.ALWAYS);
    hbox.getChildren ().addAll (filler, label2);

    label2.setFont (headingFont);
    label2.setAlignment (Pos.CENTER_RIGHT);

    return hbox;
  }

  // ---------------------------------------------------------------------------------//
  // getHBox
  // ---------------------------------------------------------------------------------//

  HBox getHBox (Label label1)
  {
    HBox hbox = new HBox (10);
    hbox.setPrefHeight (20);
    hbox.setAlignment (Pos.CENTER_LEFT);
    hbox.setPadding (new Insets (6, 10, 6, 10));
    hbox.getChildren ().add (label1);

    label1.setFont (headingFont);

    return hbox;
  }

  // ---------------------------------------------------------------------------------//
  // createTab
  // ---------------------------------------------------------------------------------//

  XmitTab createTab (String title, KeyCode keyCode, TabUpdater tabUpdater)
  {
    Tab tab = new Tab (title);
    TextArea text = new TextArea ();
    text.setWrapText (false);
    tab.setContent (text);
    //    text.setFont (monospacedFont);
    text.setEditable (false);
    return new XmitTab (tab, text, keyCode, tabUpdater);
  }
}
