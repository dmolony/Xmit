package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

import javafx.stage.Stage;

// -----------------------------------------------------------------------------------//
class WindowStatus implements SaveState
// -----------------------------------------------------------------------------------//
{
  private static final String PREFS_WINDOW_LOCATION = "WindowLocation";

  double width;
  double height;
  double x;
  double y;

  // ---------------------------------------------------------------------------------//
  public WindowStatus ()
  // ---------------------------------------------------------------------------------//
  {
    reset ();
  }

  // ---------------------------------------------------------------------------------//
  void setLocation (Stage stage)
  // ---------------------------------------------------------------------------------//
  {
    this.width = stage.getWidth ();
    this.height = stage.getHeight ();
    this.x = stage.getX ();
    this.y = stage.getY ();
  }

  // ---------------------------------------------------------------------------------//
  void reset ()
  // ---------------------------------------------------------------------------------//
  {
    width = 0.0;
    height = 0.0;
    x = 0.0;
    y = 0.0;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void save (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    if (width > 100 && height > 100)
    {
      String text = String.format ("%f,%f,%f,%f", width, height, x, y);
      prefs.put (PREFS_WINDOW_LOCATION, text);
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    String windowLocation = prefs.get (PREFS_WINDOW_LOCATION, "");
    if (windowLocation.isEmpty ())
      reset ();
    else
    {
      String[] chunks = windowLocation.split (",");
      width = Double.parseDouble (chunks[0]);
      height = Double.parseDouble (chunks[1]);
      x = Double.parseDouble (chunks[2]);
      y = Double.parseDouble (chunks[3]);
    }
  }
}
