package com.bytezone.xmit.gui;

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

  private boolean filterActive;
  private String filterValue = "";
  private boolean fullFilter;
  private boolean reverseFilter;

  private boolean expandJclInclude;

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
  @Override
  public void setFilter (boolean filterActive, String filterValue, boolean fullFilter,
      boolean reverseFilter)
  // ---------------------------------------------------------------------------------//
  {
    this.filterActive = filterActive;
    this.filterValue = filterValue;
    this.fullFilter = fullFilter;
    this.reverseFilter = reverseFilter;

    setFilterText ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void showLinesSelected (boolean showLines, boolean stripLines,
      boolean truncateLines, boolean expandJclInclude)
  // ---------------------------------------------------------------------------------//
  {
    this.expandJclInclude = expandJclInclude;
    setFilterText ();
  }

  // ---------------------------------------------------------------------------------//
  private void setFilterText ()
  // ---------------------------------------------------------------------------------//
  {
    String filterText = filterActive
        ? filterValue.isEmpty () ? "NONE" : (reverseFilter ? "~" : "") + filterValue
        : "OFF";
    String showText = (filterActive && !filterValue.isEmpty ())
        ? fullFilter ? "Filtered lines" : "All lines" : "All lines";
    String includeText = expandJclInclude ? "ON" : "OFF";

    filterSettings.setText (String.format ("Filter: %-20s Show: %-20s JCL Include: %-3s ",
        filterText, showText, includeText));
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
