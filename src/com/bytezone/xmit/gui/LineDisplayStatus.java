package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

// ---------------------------------------------------------------------------------//
public class LineDisplayStatus
//---------------------------------------------------------------------------------//
{
  private static final String PREFS_SHOW_LINES = "ShowLines";
  private static final String PREFS_STRIP_LINES = "StripLines";
  private static final String PREFS_TRUNCATE = "Truncate";
  private static final String PREFS_CHECK_INCLUDE = "CheckInclude";

  boolean showLines;
  boolean stripLines;
  boolean truncateLines;
  boolean expandInclude;

  //---------------------------------------------------------------------------------//
  LineDisplayStatus ()
  //---------------------------------------------------------------------------------//
  {
    reset ();
  }

  //---------------------------------------------------------------------------------//
  LineDisplayStatus (LineDisplayStatus lineDisplayStatus)
  //---------------------------------------------------------------------------------//
  {
    copy (lineDisplayStatus);
  }

  //---------------------------------------------------------------------------------//
  void reset ()
  //---------------------------------------------------------------------------------//
  {
    this.showLines = false;
    this.stripLines = false;
    this.truncateLines = false;
    this.expandInclude = false;
  }

  //---------------------------------------------------------------------------------//
  void set (boolean showLines, boolean stripLines, boolean truncateLines,
      boolean expandLines)
  //---------------------------------------------------------------------------------//
  {
    this.showLines = showLines;
    this.stripLines = stripLines;
    this.truncateLines = truncateLines;
    this.expandInclude = expandLines;
  }

  //---------------------------------------------------------------------------------//
  void copy (LineDisplayStatus lineDisplayStatus)
  //---------------------------------------------------------------------------------//
  {
    this.showLines = lineDisplayStatus.showLines;
    this.stripLines = lineDisplayStatus.stripLines;
    this.truncateLines = lineDisplayStatus.truncateLines;
    this.expandInclude = lineDisplayStatus.expandInclude;
  }

  //---------------------------------------------------------------------------------//
  void save (Preferences prefs)
  //---------------------------------------------------------------------------------//
  {
    prefs.putBoolean (PREFS_SHOW_LINES, showLines);
    prefs.putBoolean (PREFS_STRIP_LINES, stripLines);
    prefs.putBoolean (PREFS_TRUNCATE, truncateLines);
    prefs.putBoolean (PREFS_CHECK_INCLUDE, expandInclude);
  }

  //---------------------------------------------------------------------------------//
  void restore (Preferences prefs)
  //---------------------------------------------------------------------------------//
  {
    showLines = prefs.getBoolean (PREFS_SHOW_LINES, false);
    stripLines = prefs.getBoolean (PREFS_STRIP_LINES, false);
    truncateLines = prefs.getBoolean (PREFS_TRUNCATE, false);
    expandInclude = prefs.getBoolean (PREFS_CHECK_INCLUDE, false);
  }
}
