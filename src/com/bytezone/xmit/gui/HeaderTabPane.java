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
// HeaderTabPane
// ---------------------------------------------------------------------------------//

abstract class HeaderTabPane extends HeaderPane
{
  final TabPane tabPane = new TabPane ();
  final List<XmitTab> tabs = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public HeaderTabPane ()
  {
    tabPane.setSide (Side.BOTTOM);
    tabPane.setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    tabPane.setTabMinWidth (100);

    tabPane.getSelectionModel ().selectedItemProperty ()
        .addListener ( (ov, oldTab, newTab) -> updateCurrentTab ());
    setCenter (tabPane);
  }

  // ---------------------------------------------------------------------------------//
  // createTab
  // ---------------------------------------------------------------------------------//

  XmitTab createTab (String title, KeyCode keyCode, Runnable tabUpdater)
  {
    TextArea textArea = new TextArea ();
    textArea.setWrapText (false);
    textArea.setEditable (false);

    XmitTab xmitTab = new XmitTab (title, textArea, keyCode, tabUpdater);
    tabs.add (xmitTab);

    return xmitTab;
  }

  // ---------------------------------------------------------------------------------//
  // updateCurrentTab
  // ---------------------------------------------------------------------------------//

  void updateCurrentTab ()
  {
    Tab selectedTab = tabPane.getSelectionModel ().getSelectedItem ();
    if (selectedTab != null)
    {
      XmitTab xmitTab = (XmitTab) selectedTab.getUserData ();
      if (xmitTab.isTextEmpty ())
        xmitTab.update ();
    }
  }

  // ---------------------------------------------------------------------------------//
  // clearText
  // ---------------------------------------------------------------------------------//

  void clearText ()
  {
    for (XmitTab tab : tabs)
      tab.textArea.clear ();
  }

  // ---------------------------------------------------------------------------------//
  // saveScrollBars
  // ---------------------------------------------------------------------------------//

  void saveScrollBars ()
  {
    for (XmitTab tab : tabs)
      tab.saveScrollBar ();
  }

  // ---------------------------------------------------------------------------------//
  // restoreScrollBars
  // ---------------------------------------------------------------------------------//

  void restoreScrollBars ()
  {
    for (XmitTab tab : tabs)
      tab.restoreScrollBar ();
  }

  // ---------------------------------------------------------------------------------//
  // keyPressed
  // ---------------------------------------------------------------------------------//

  public void keyPressed (KeyCode keyCode)
  {
    for (XmitTab tab : tabs)
      if (tab.keyCode == keyCode)
      {
        tabPane.getSelectionModel ().select (tab.tab);
        return;
      }
  }

  // ---------------------------------------------------------------------------------//
  // setFont
  // ---------------------------------------------------------------------------------//

  public void setFont (Font font)
  {
    for (XmitTab tab : tabs)
      tab.setFont (font);
  }
}
