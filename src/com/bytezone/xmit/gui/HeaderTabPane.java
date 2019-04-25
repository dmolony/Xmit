package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
abstract class HeaderTabPane extends BorderPane
//---------------------------------------------------------------------------------//
{
  private static final int TAB_WIDTH = 100;
  final TabPane tabPane = new TabPane ();
  final List<XmitTab> xmitTabs = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public HeaderTabPane ()
  // ---------------------------------------------------------------------------------//
  {
    tabPane.setSide (Side.BOTTOM);
    tabPane.setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    tabPane.setTabMinWidth (TAB_WIDTH);

    tabPane.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, prev, selectedTab) -> ((XmitTab) selectedTab).update ());

    setCenter (tabPane);
  }

  // ---------------------------------------------------------------------------------//
  void updateCurrentTab ()
  // ---------------------------------------------------------------------------------//
  {
    Tab selectedTab = tabPane.getSelectionModel ().getSelectedItem ();
    if (selectedTab != null)
      ((XmitTab) selectedTab).update ();
  }

  // ---------------------------------------------------------------------------------//
  void clearText ()
  // ---------------------------------------------------------------------------------//
  {
    for (XmitTab xmitTab : xmitTabs)
      xmitTab.clear ();
  }

  // ---------------------------------------------------------------------------------//
  public void keyPressed (KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    for (XmitTab xmitTab : xmitTabs)
      if (xmitTab.keyCode == keyCode)
      {
        tabPane.getSelectionModel ().select (xmitTab);
        break;
      }
  }

  // ---------------------------------------------------------------------------------//
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    for (XmitTab xmitTab : xmitTabs)
      xmitTab.setFont (font);
  }
}
