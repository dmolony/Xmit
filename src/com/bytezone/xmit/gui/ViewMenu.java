package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class ViewMenu
{
  private static final String PREFS_SHOW_LINES = "ShowLines";
  private static final String PREFS_SHOW_CONTROL = "ShowControl";
  private static final String PREFS_SHOW_DEBUG = "ShowDebug";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final List<ShowLinesListener> listeners = new ArrayList<> ();

  private final Menu viewMenu = new Menu ("View");
  private final CheckMenuItem linesMenuItem = new CheckMenuItem ("Show line numbers");
  private final CheckMenuItem controlMenuItem = new CheckMenuItem ("Control tab");
  private final CheckMenuItem debugMenuItem = new CheckMenuItem ("Debug tab");

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public ViewMenu (XmitApp owner, TreeView<XmitFile> tree)
  {
    viewMenu.getItems ().addAll (linesMenuItem, controlMenuItem, debugMenuItem);
    linesMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.L, KeyCombination.SHORTCUT_DOWN));

    linesMenuItem.setOnAction (e -> setLines ());
    controlMenuItem.setOnAction (e -> setMeta ());
    debugMenuItem.setOnAction (e -> setDebug ());
  }

  // ---------------------------------------------------------------------------------//
  // setLines
  // ---------------------------------------------------------------------------------//

  private void setLines ()
  {
    for (ShowLinesListener listener : listeners)
      listener.showLinesSelected (linesMenuItem.isSelected ());
  }

  private void setMeta ()
  {

  }

  private void setDebug ()
  {

  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  void restore ()
  {
    linesMenuItem.setSelected (prefs.getBoolean (PREFS_SHOW_LINES, false));
    setLines ();

    controlMenuItem.setSelected (prefs.getBoolean (PREFS_SHOW_CONTROL, false));
    debugMenuItem.setSelected (prefs.getBoolean (PREFS_SHOW_DEBUG, false));
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.putBoolean (PREFS_SHOW_LINES, linesMenuItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_CONTROL, controlMenuItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_DEBUG, debugMenuItem.isSelected ());
  }

  // ---------------------------------------------------------------------------------//
  // addListener
  // ---------------------------------------------------------------------------------//

  public void addListener (ShowLinesListener listener)
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  // getMenu
  // ---------------------------------------------------------------------------------//

  Menu getMenu ()
  {
    return viewMenu;
  }
}
