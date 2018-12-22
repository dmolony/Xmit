package com.bytezone.xmit.gui;

import java.io.File;
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
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final List<ShowLinesListener> listeners = new ArrayList<> ();

  private final Menu viewMenu = new Menu ("View");
  private final CheckMenuItem linesMenuItem = new CheckMenuItem ("Show line numbers");

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public ViewMenu (XmitApp owner, TreeView<File> tree)
  {
    viewMenu.getItems ().addAll (linesMenuItem);
    linesMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.L, KeyCombination.SHORTCUT_DOWN));
    linesMenuItem.setOnAction (e -> setLines ());
  }

  // ---------------------------------------------------------------------------------//
  // setLines
  // ---------------------------------------------------------------------------------//

  private void setLines ()
  {
    for (ShowLinesListener listener : listeners)
      listener.showLinesSelected (linesMenuItem.isSelected ());
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  void restore ()
  {
    linesMenuItem.setSelected (prefs.getBoolean (PREFS_SHOW_LINES, false));
    setLines ();
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.putBoolean (PREFS_SHOW_LINES, linesMenuItem.isSelected ());
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
