package com.bytezone.xmit.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

// -----------------------------------------------------------------------------------//
class HeaderBar extends HBox
// -----------------------------------------------------------------------------------//
{
  static Font headingFont = Font.font ("Lucida Sans Typewriter", 14);

  Label leftLabel = new Label ();
  Label rightLabel = new Label ();

  // ---------------------------------------------------------------------------------//
  public HeaderBar ()
  // ---------------------------------------------------------------------------------//
  {
    super (10);

    setPrefHeight (20);
    setPadding (new Insets (6, 10, 6, 10));
    setAlignment (Pos.CENTER_LEFT);

    Region filler = new Region ();
    HBox.setHgrow (filler, Priority.ALWAYS);

    getChildren ().addAll (leftLabel, filler, rightLabel);

    leftLabel.setAlignment (Pos.CENTER_LEFT);
    rightLabel.setAlignment (Pos.CENTER_RIGHT);

    leftLabel.setFont (headingFont);
    rightLabel.setFont (headingFont);
  }
}
