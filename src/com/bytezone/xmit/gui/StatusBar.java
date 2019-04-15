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
    implements FilterChangeListener, FilterActionListener, ShowLinesListener
// ---------------------------------------------------------------------------------//
{
  private final Label status = new Label ();
  private String filterValue = "";
  private boolean fullFilter;
  private FilterMode filterMode;
  private final Label filterSettings = new Label ();
  public static Font statusFont = Font.font ("Monaco", 12);
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
    status.setFont (statusFont);
    filterSettings.setFont (statusFont);
  }

  // ---------------------------------------------------------------------------------//
  void setText (String text)
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

    if (filterValue.isEmpty ())
      status.setText ("");
    setFilterText ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void filtering (int found, int max, boolean done)
  // ---------------------------------------------------------------------------------//
  {
    if (!done)
      return;

    if (filterValue.isEmpty ())
      status.setText ("");
    else
    {
      String mode = filterMode.toString ();
      status.setText (String.format ("%s found %d member%s from %d", mode, found,
          (found == 1 ? "" : "s"), max));
    }
  }

  // ---------------------------------------------------------------------------------//
  private void setFilterText ()
  // ---------------------------------------------------------------------------------//
  {
    String showText = (filterMode != FilterMode.ON) ? "All lines"
        : fullFilter ? "Filtered lines" : "All lines";
    String includeText = expandInclude ? "ON" : "OFF";
    String filterText = filterMode == FilterMode.OFF ? "OFF"
        : filterMode == FilterMode.ON ? filterValue : "~" + filterValue;
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
}
