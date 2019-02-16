package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
abstract class HeaderTabPane extends HeaderPane
//---------------------------------------------------------------------------------//
{
  final TabPane tabPane = new TabPane ();
  final List<XmitTab> tabs = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public HeaderTabPane ()
  // ---------------------------------------------------------------------------------//
  {
    tabPane.setSide (Side.BOTTOM);
    tabPane.setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    tabPane.setTabMinWidth (100);

    tabPane.getSelectionModel ().selectedItemProperty ()
        .addListener ( (ov, oldTab, newTab) -> updateCurrentTab ());
    setCenter (tabPane);
  }

  // ---------------------------------------------------------------------------------//
  XmitTab createTab (String title, KeyCode keyCode, Runnable tabUpdater)
  // ---------------------------------------------------------------------------------//
  {
    TextArea textArea = new TextArea ();
    textArea.setWrapText (false);
    textArea.setEditable (false);

    XmitTab xmitTab = new XmitTab (title, textArea, keyCode, tabUpdater);
    tabs.add (xmitTab);

    return xmitTab;
  }

  // ---------------------------------------------------------------------------------//
  void updateCurrentTab ()
  // ---------------------------------------------------------------------------------//
  {
    Tab selectedTab = tabPane.getSelectionModel ().getSelectedItem ();
    if (selectedTab != null)
      ((XmitTab) selectedTab.getUserData ()).update ();
  }

  // ---------------------------------------------------------------------------------//
  void clearText ()
  // ---------------------------------------------------------------------------------//
  {
    for (XmitTab tab : tabs)
      tab.textArea.clear ();
  }

  // ---------------------------------------------------------------------------------//
  void saveScrollBars ()
  // ---------------------------------------------------------------------------------//
  {
    for (XmitTab tab : tabs)
      tab.saveScrollBar ();
  }

  // ---------------------------------------------------------------------------------//
  void restoreScrollBars ()
  // ---------------------------------------------------------------------------------//
  {
    for (XmitTab tab : tabs)
      tab.restoreScrollBar ();
  }

  // ---------------------------------------------------------------------------------//
  public void keyPressed (KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    for (XmitTab tab : tabs)
      if (tab.keyCode == keyCode)
      {
        tabPane.getSelectionModel ().select (tab.tab);
        return;
      }
  }

  // ---------------------------------------------------------------------------------//
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    for (XmitTab tab : tabs)
      tab.setFont (font);
  }
}
