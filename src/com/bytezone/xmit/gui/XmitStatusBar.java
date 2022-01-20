package com.bytezone.xmit.gui;

import com.bytezone.appbase.StatusBar;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

// -----------------------------------------------------------------------------------//
public class XmitStatusBar extends StatusBar
    implements FilterChangeListener, ShowLinesListener, CodePageSelectedListener
// -----------------------------------------------------------------------------------//
{
  private FilterStatus filterStatus;
  private boolean expandJclInclude;
  private String codePageName;
  private final Label statusDisplay = new Label ();

  // ---------------------------------------------------------------------------------//
  public XmitStatusBar ()
  // ---------------------------------------------------------------------------------//
  {
    Region filler = new Region ();
    HBox.setHgrow (filler, Priority.ALWAYS);
    getChildren ().addAll (filler, statusDisplay);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    super.setFont (font);
    statusDisplay.setFont (font);
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
  @Override
  public void selectCodePage (String codePageName)
  // ---------------------------------------------------------------------------------//
  {
    this.codePageName = codePageName;
    setStatusText ();
  }

  // ---------------------------------------------------------------------------------//
  private void setStatusText ()
  // ---------------------------------------------------------------------------------//
  {
    String filterText = filterStatus.filterActive ? filterStatus.filterValue.isEmpty () ? "NONE"
        : (filterStatus.filterReverse ? "~" : "") + filterStatus.filterValue : "OFF";

    String showText = (filterStatus.filterActive && !filterStatus.filterValue.isEmpty ())
        ? filterStatus.filterExclusion ? "Filtered lines" : "All lines" : "All lines";

    String includeText = expandJclInclude ? "ON" : "OFF";

    statusDisplay
        .setText (String.format ("Filter: %-20s Show: %-20s JCL Include: %-12s Codepage: %-6s",
            filterText, showText, includeText, codePageName));
  }
}
