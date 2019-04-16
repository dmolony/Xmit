package com.bytezone.xmit.gui;

import com.bytezone.xmit.Filter.FilterMode;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
public class StatusBar extends HBox
    implements FilterChangeListener, ShowLinesListener, FontChangeListener
// ---------------------------------------------------------------------------------//
{
  private final Label status = new Label ();
  private final Label filterSettings = new Label ();

  private String filterValue = "";
  private boolean fullFilter;
  private FilterMode filterMode;
  private boolean expandInclude;

  // ---------------------------------------------------------------------------------//
  public StatusBar ()
  // ---------------------------------------------------------------------------------//
  {
    super (10);

    Region filler = new Region ();
    HBox.setHgrow (filler, Priority.ALWAYS);
    getChildren ().addAll (status, filler, filterSettings);
    setPadding (new Insets (5));
  }

  // ---------------------------------------------------------------------------------//
  void setStatus (String text)
  // ---------------------------------------------------------------------------------//
  {
    status.setText (text);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (String filterValue, boolean fullFilter, FilterMode filterMode)
  // ---------------------------------------------------------------------------------//
  {
    this.filterValue = filterValue;
    this.fullFilter = fullFilter;
    this.filterMode = filterMode;

    setFilterText ();
  }

  // ---------------------------------------------------------------------------------//
  private void setFilterText ()
  // ---------------------------------------------------------------------------------//
  {
    String showText = (filterMode != FilterMode.ON) ? "All lines"
        : fullFilter ? "Filtered lines" : "All lines";
    String includeText = expandInclude ? "ON" : "OFF";
    String filterText = filterValue.isEmpty () ? "NONE" : filterMode == FilterMode.OFF
        ? "OFF" : filterMode == FilterMode.ON ? filterValue : "~" + filterValue;

    filterSettings
        .setText (String.format ("Filter: %-12s  Show: %-14s  JCL Include: %-3s ",
            filterText, showText, includeText));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void showLinesSelected (boolean showLines, boolean stripLines,
      boolean truncateLines, boolean expandInclude)
  // ---------------------------------------------------------------------------------//
  {
    this.expandInclude = expandInclude;
    setFilterText ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    status.setFont (font);
    filterSettings.setFont (font);
  }
}
