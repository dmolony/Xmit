package com.bytezone.xmit.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
class StatusBar extends HBox
    implements FilterChangeListener, ShowLinesListener, FontChangeListener
// ---------------------------------------------------------------------------------//
{
  private final Label statusMessage = new Label ();
  private final Label statusDisplay = new Label ();

  private FilterStatus filterStatus;
  private boolean expandJclInclude;

  // ---------------------------------------------------------------------------------//
  public StatusBar ()
  // ---------------------------------------------------------------------------------//
  {
    super (10);

    Region filler = new Region ();
    HBox.setHgrow (filler, Priority.ALWAYS);
    getChildren ().addAll (statusMessage, filler, statusDisplay);
    setPadding (new Insets (5));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (FilterStatus filterStatus)
  // ---------------------------------------------------------------------------------//
  {
    this.filterStatus = filterStatus;
    setStatusText ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void showLinesSelected (LineDisplayStatus lineDisplayStatus)
  // ---------------------------------------------------------------------------------//
  {
    this.expandJclInclude = lineDisplayStatus.expandInclude;
    setStatusText ();
  }

  // ---------------------------------------------------------------------------------//
  private void setStatusText ()
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

    statusDisplay.setText (String.format ("Filter: %-20s Show: %-20s JCL Include: %-3s ",
        filterText, showText, includeText));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    statusMessage.setFont (font);
    statusDisplay.setFont (font);
  }
}
