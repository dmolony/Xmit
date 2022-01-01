package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

import com.bytezone.appbase.WindowStatus;

import javafx.scene.control.SplitPane;

// -----------------------------------------------------------------------------------//
public class XmitWindowStatus extends WindowStatus
// -----------------------------------------------------------------------------------//
{
  private static final String PREFS_DIVIDER_POSITION_1 = "DividerPosition1";
  private static final String PREFS_DIVIDER_POSITION_2 = "DividerPosition2";

  double dividerPosition1;
  double dividerPosition2;

  SplitPane splitPane;

  // ---------------------------------------------------------------------------------//
  void setSplitPane (SplitPane splitPane)
  // ---------------------------------------------------------------------------------//
  {
    this.splitPane = splitPane;

    splitPane.setDividerPosition (0, dividerPosition1);
    splitPane.setDividerPosition (1, dividerPosition2);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  protected void reset ()
  // ---------------------------------------------------------------------------------//
  {
    super.reset ();

    dividerPosition1 = 0.0;
    dividerPosition2 = 0.0;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void save (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    super.save (prefs);

    double[] dividerPositions = splitPane.getDividerPositions ();
    this.dividerPosition1 = dividerPositions[0];
    this.dividerPosition2 = dividerPositions[1];

    prefs.putDouble (PREFS_DIVIDER_POSITION_1, dividerPosition1);
    prefs.putDouble (PREFS_DIVIDER_POSITION_2, dividerPosition2);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    super.restore (prefs);

    dividerPosition1 = prefs.getDouble (PREFS_DIVIDER_POSITION_1, .33);
    dividerPosition2 = prefs.getDouble (PREFS_DIVIDER_POSITION_2, .67);
  }
}
