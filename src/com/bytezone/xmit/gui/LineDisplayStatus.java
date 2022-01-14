package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

import com.bytezone.appbase.SaveState;

// -----------------------------------------------------------------------------------//
class LineDisplayStatus implements SaveState
// -----------------------------------------------------------------------------------//
{
  private static final String PREFS_SHOW_LINES = "ShowLines";
  private static final String PREFS_STRIP_LINES = "StripLines";
  private static final String PREFS_TRUNCATE = "Truncate";
  private static final String PREFS_CHECK_INCLUDE = "CheckInclude";

  boolean showLines;          // 
  boolean stripLines;
  boolean truncateLines;
  boolean expandInclude;

  // ---------------------------------------------------------------------------------//
  LineDisplayStatus ()
  // ---------------------------------------------------------------------------------//
  {
    reset ();
  }

  // ---------------------------------------------------------------------------------//
  LineDisplayStatus (LineDisplayStatus lineDisplayStatus)
  // ---------------------------------------------------------------------------------//
  {
    copy (lineDisplayStatus);
  }

  // ---------------------------------------------------------------------------------//
  void reset ()
  // ---------------------------------------------------------------------------------//
  {
    showLines = false;
    stripLines = false;
    truncateLines = false;
    expandInclude = false;
  }

  // ---------------------------------------------------------------------------------//
  void set (boolean showLines, boolean stripLines, boolean truncateLines, boolean expandLines)
  // ---------------------------------------------------------------------------------//
  {
    this.showLines = showLines;
    this.stripLines = stripLines;
    this.truncateLines = truncateLines;
    this.expandInclude = expandLines;
  }

  // ---------------------------------------------------------------------------------//
  void copy (LineDisplayStatus lineDisplayStatus)
  // ---------------------------------------------------------------------------------//
  {
    showLines = lineDisplayStatus.showLines;
    stripLines = lineDisplayStatus.stripLines;
    truncateLines = lineDisplayStatus.truncateLines;
    expandInclude = lineDisplayStatus.expandInclude;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void save (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    prefs.putBoolean (PREFS_SHOW_LINES, showLines);
    prefs.putBoolean (PREFS_STRIP_LINES, stripLines);
    prefs.putBoolean (PREFS_TRUNCATE, truncateLines);
    prefs.putBoolean (PREFS_CHECK_INCLUDE, expandInclude);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    showLines = prefs.getBoolean (PREFS_SHOW_LINES, false);
    stripLines = prefs.getBoolean (PREFS_STRIP_LINES, false);
    truncateLines = prefs.getBoolean (PREFS_TRUNCATE, false);
    expandInclude = prefs.getBoolean (PREFS_CHECK_INCLUDE, false);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Show lines...... %s%n", showLines));
    text.append (String.format ("Strip lines..... %s%n", stripLines));
    text.append (String.format ("Truncate.lines.. %s%n", truncateLines));
    text.append (String.format ("Expand include.. %s", expandInclude));

    return text.toString ();
  }
}
