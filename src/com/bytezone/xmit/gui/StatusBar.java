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

  //  private boolean filterActive;
  //  private String filterValue = "";
  //  private boolean fullFilter;
  //  private boolean reverseFilter;
  private FilterStatus filterStatus;

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
  public void setFilter (FilterStatus filterStatus)
  // ---------------------------------------------------------------------------------//
  {
    this.filterStatus = filterStatus;
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
    String filterText =
        filterStatus.filterActive
            ? filterStatus.filterValue.isEmpty () ? "NONE"
                : (filterStatus.filterReverse ? "~" : "") + filterStatus.filterValue
            : "OFF";
    String showText = (filterStatus.filterActive && !filterStatus.filterValue.isEmpty ())
        ? filterStatus.filterExclusion ? "Filtered lines" : "All lines" : "All lines";
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
