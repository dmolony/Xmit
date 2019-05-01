package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
abstract class XmitTabPane extends TabPane implements FontChangeListener, SaveState
//---------------------------------------------------------------------------------//
{
  private final String PREFS_LAST_TAB;

  private static final int TAB_WIDTH = 100;
  private final List<XmitTab> xmitTabs = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public XmitTabPane (String prefsId)
  // ---------------------------------------------------------------------------------//
  {
    setSide (Side.BOTTOM);
    setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    setTabMinWidth (TAB_WIDTH);
    setFocusTraversable (false);

    PREFS_LAST_TAB = "lastTab" + prefsId;

    getSelectionModel ().selectedItemProperty ().addListener (this::select);
  }

  // ---------------------------------------------------------------------------------//
  private void select (ObservableValue<? extends Tab> obs, Tab prev, Tab next)
  // ---------------------------------------------------------------------------------//
  {
    if (prev != null)
      ((XmitTab) prev).active = false;

    ((XmitTab) next).active = true;
    ((XmitTab) next).update ();
  }

  // ---------------------------------------------------------------------------------//
  void add (XmitTab tab)
  // ---------------------------------------------------------------------------------//
  {
    xmitTabs.add (tab);
  }

  // ---------------------------------------------------------------------------------//
  void keyPressed (KeyEvent keyEvent)
  // ---------------------------------------------------------------------------------//
  {
    KeyCode keyCode = keyEvent.getCode ();
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

  //----------------------------------------------------------------------------------- //
  @Override
  public void restore (Preferences prefs)
  //----------------------------------------------------------------------------------- //
  {
    getSelectionModel ().select (prefs.getInt (PREFS_LAST_TAB, 0));
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void save (Preferences prefs)
  //----------------------------------------------------------------------------------- //
  {
    prefs.putInt (PREFS_LAST_TAB, getSelectionModel ().getSelectedIndex ());
  }
}
