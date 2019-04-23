package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

// ---------------------------------------------------------------------------------//
public class FilterStatus
//---------------------------------------------------------------------------------//
{
  private static final String PREFS_FILTER = "Filter";
  private static final String PREFS_FILTER_EXC = "FilterExc";
  private static final String PREFS_FILTER_REVERSE = "FilterReverse";
  private static final String PREFS_FILTER_ACTIVE = "FilterActive";

  String filterValue;
  boolean filterExclusion;
  boolean filterReverse;
  boolean filterActive;

  //---------------------------------------------------------------------------------//
  FilterStatus ()
  //---------------------------------------------------------------------------------//
  {
    reset ();
  }

  //---------------------------------------------------------------------------------//
  FilterStatus (FilterStatus filterStatus)
  //---------------------------------------------------------------------------------//
  {
    copy (filterStatus);
  }

  //---------------------------------------------------------------------------------//
  void copy (FilterStatus filterStatus)
  //---------------------------------------------------------------------------------//
  {
    this.filterValue = filterStatus.filterValue;
    this.filterExclusion = filterStatus.filterExclusion;
    this.filterReverse = filterStatus.filterReverse;
    this.filterActive = filterStatus.filterActive;
  }

  //---------------------------------------------------------------------------------//
  boolean matches (FilterStatus filterStatus)
  //---------------------------------------------------------------------------------//
  {
    return filterStatus != null       //
        && this.filterValue.equals (filterStatus.filterValue)
        && this.filterExclusion == filterStatus.filterExclusion
        && this.filterReverse == filterStatus.filterReverse
        && this.filterActive == filterStatus.filterActive;
  }

  //---------------------------------------------------------------------------------//
  void set (String filterValue, boolean filterExclusion, boolean filterReverse,
      boolean filterActive)
  //---------------------------------------------------------------------------------//
  {
    this.filterValue = filterValue;
    this.filterExclusion = filterExclusion;
    this.filterReverse = filterReverse;
    this.filterActive = filterActive;
  }

  //---------------------------------------------------------------------------------//
  void reset ()
  //---------------------------------------------------------------------------------//
  {
    filterValue = "";
    filterExclusion = false;
    filterReverse = false;
    filterActive = false;
  }

  //---------------------------------------------------------------------------------//
  void save (Preferences prefs)
  //---------------------------------------------------------------------------------//
  {
    prefs.put (PREFS_FILTER, filterValue);
    prefs.putBoolean (PREFS_FILTER_EXC, filterExclusion);
    prefs.putBoolean (PREFS_FILTER_REVERSE, filterReverse);
    prefs.putBoolean (PREFS_FILTER_ACTIVE, filterActive);
  }

  //---------------------------------------------------------------------------------//
  void restore (Preferences prefs)
  //---------------------------------------------------------------------------------//
  {
    filterValue = prefs.get (PREFS_FILTER, "");
    filterExclusion = prefs.getBoolean (PREFS_FILTER_EXC, false);
    filterReverse = prefs.getBoolean (PREFS_FILTER_REVERSE, false);
    filterActive = prefs.getBoolean (PREFS_FILTER_ACTIVE, false);
  }
}
