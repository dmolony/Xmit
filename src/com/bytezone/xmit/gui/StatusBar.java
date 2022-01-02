package com.bytezone.xmit.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

// -----------------------------------------------------------------------------------//
class StatusBar extends HBox implements FontChangeListener
// -----------------------------------------------------------------------------------//
{
  private static final int MAX_TICKS = 3;
  private final Label statusMessage = new Label ();

  private int ticksLeft;

  // ---------------------------------------------------------------------------------//
  public StatusBar ()
  // ---------------------------------------------------------------------------------//
  {
    super (10);

    getChildren ().addAll (statusMessage);
    setPadding (new Insets (5));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    statusMessage.setFont (font);
    setStatusMessage (font.getName () + " " + font.getSize ());
  }

  // ---------------------------------------------------------------------------------//
  void setStatusMessage (String message)
  // ---------------------------------------------------------------------------------//
  {
    statusMessage.setText (message);
    ticksLeft = MAX_TICKS;
  }

  // ---------------------------------------------------------------------------------//
  void tick ()
  // ---------------------------------------------------------------------------------//
  {
    if (ticksLeft == 0)
      statusMessage.setText ("");
    else
      --ticksLeft;
  }
}
