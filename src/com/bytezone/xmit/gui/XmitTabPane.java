package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
abstract class XmitTabPane extends TabPane implements FontChangeListener
//---------------------------------------------------------------------------------//
{
  private static final int TAB_WIDTH = 100;
  private final List<XmitTab> xmitTabs = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public XmitTabPane ()
  // ---------------------------------------------------------------------------------//
  {
    setSide (Side.BOTTOM);
    setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    setTabMinWidth (TAB_WIDTH);

    getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, prev, next) -> select (obs, prev, next));
  }

  // ---------------------------------------------------------------------------------//
  private void select (ObservableValue<? extends Tab> obs, Tab prev, Tab next)
  // ---------------------------------------------------------------------------------//
  {
    if (prev != null)
      ((XmitTab) prev).active = false;

    if (next != null)
    {
      ((XmitTab) next).active = true;
      ((XmitTab) next).update ();
    }
  }

  // ---------------------------------------------------------------------------------//
  void add (XmitTab tab)
  // ---------------------------------------------------------------------------------//
  {
    xmitTabs.add (tab);
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
  @Override
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    for (XmitTab xmitTab : xmitTabs)
      xmitTab.setFont (font);
  }
}
