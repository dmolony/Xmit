package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

import javafx.stage.Stage;

// ---------------------------------------------------------------------------------//
public class WindowStatus implements SaveState
//---------------------------------------------------------------------------------//
{
  private static final String PREFS_WINDOW_LOCATION = "WindowLocation";
  private static final String PREFS_DIVIDER_POSITION_1 = "DividerPosition1";
  private static final String PREFS_DIVIDER_POSITION_2 = "DividerPosition2";

  double width;
  double height;
  double x;
  double y;

  double dividerPosition1;
  double dividerPosition2;

  //---------------------------------------------------------------------------------//
  public WindowStatus ()
  //---------------------------------------------------------------------------------//
  {
    reset ();
  }

  //---------------------------------------------------------------------------------//
  void set (Stage stage)
  //---------------------------------------------------------------------------------//
  {
    this.width = stage.getWidth ();
    this.height = stage.getHeight ();
    this.x = stage.getX ();
    this.y = stage.getY ();
  }

  //---------------------------------------------------------------------------------//
  void set (double[] dividerPositions)
  //---------------------------------------------------------------------------------//
  {
    this.dividerPosition1 = dividerPositions[0];
    this.dividerPosition2 = dividerPositions[1];
  }

  //---------------------------------------------------------------------------------//
  void reset ()
  //---------------------------------------------------------------------------------//
  {
    width = 0.0;
    height = 0.0;
    x = 0.0;
    y = 0.0;

    dividerPosition1 = 0.0;
    dividerPosition2 = 0.0;
  }

  //---------------------------------------------------------------------------------//
  @Override
  public void save (Preferences prefs)
  //---------------------------------------------------------------------------------//
  {
    if (width > 100 && height > 100)
    {
      String text = String.format ("%f,%f,%f,%f", width, height, x, y);
      prefs.put (PREFS_WINDOW_LOCATION, text);
    }

    prefs.putDouble (PREFS_DIVIDER_POSITION_1, dividerPosition1);
    prefs.putDouble (PREFS_DIVIDER_POSITION_2, dividerPosition2);
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

    dividerPosition1 = prefs.getDouble (PREFS_DIVIDER_POSITION_1, .33);
    dividerPosition2 = prefs.getDouble (PREFS_DIVIDER_POSITION_2, .67);
  }
}
