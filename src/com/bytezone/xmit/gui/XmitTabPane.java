package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Side;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
abstract class XmitTabPane extends TabPane
//---------------------------------------------------------------------------------//
{
  private static final int TAB_WIDTH = 100;
  //  final TabPane tabPane = new TabPane ();
  final List<XmitTab> xmitTabs = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public XmitTabPane ()
  // ---------------------------------------------------------------------------------//
  {
    setSide (Side.BOTTOM);
    setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    setTabMinWidth (TAB_WIDTH);

    getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, prev, selectedTab) -> ((XmitTextTab) selectedTab).update ());
  }

  // ---------------------------------------------------------------------------------//
  void updateCurrentTab ()
  // ---------------------------------------------------------------------------------//
  {
    for (XmitTab xmitTab : xmitTabs)
      xmitTab.clear ();

    XmitTab selectedTab = (XmitTab) getSelectionModel ().getSelectedItem ();
    if (selectedTab != null)
      selectedTab.update ();
  }

  // ---------------------------------------------------------------------------------//
  public void keyPressed (KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    for (XmitTab xmitTab : xmitTabs)
      if (xmitTab.keyCode == keyCode)
      {
        getSelectionModel ().select (xmitTab);
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
