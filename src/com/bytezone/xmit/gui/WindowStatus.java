package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

// ---------------------------------------------------------------------------------//
public class WindowStatus implements SaveState
//---------------------------------------------------------------------------------//
{
  private static final String PREFS_WINDOW_LOCATION = "WindowLocation";

  double width;
  double height;
  double x;
  double y;

  //---------------------------------------------------------------------------------//
  public WindowStatus ()
  //---------------------------------------------------------------------------------//
  {
    reset ();
  }

  //---------------------------------------------------------------------------------//
  void set (double width, double height, double x, double y)
  //---------------------------------------------------------------------------------//
  {
    this.width = width;
    this.height = height;
    this.x = x;
    this.y = y;
  }

  //---------------------------------------------------------------------------------//
  void reset ()
  //---------------------------------------------------------------------------------//
  {
    width = 0.0;
    height = 0.0;
    x = 0.0;
    y = 0.0;
  }

  //---------------------------------------------------------------------------------//
  @Override
  public void save (Preferences prefs)
  //---------------------------------------------------------------------------------//
  {
    String text = String.format ("%f,%f,%f,%f", width, height, x, y);
    prefs.put (PREFS_WINDOW_LOCATION, text);
  }

  //---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  //---------------------------------------------------------------------------------//
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
