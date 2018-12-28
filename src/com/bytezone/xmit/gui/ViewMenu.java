package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class ViewMenu
{
  private static final String PREFS_SHOW_LINES = "ShowLines";
  private static final String PREFS_SHOW_CONTROL = "ShowControl";
  private static final String PREFS_SHOW_DEBUG = "ShowDebug";
  private static final String PREFS_CODE_PAGE = "CodePage";
  private static final String PREFS_EURO_PAGE = "EuroPage";

  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final List<ShowLinesListener> showLinesListeners = new ArrayList<> ();
  private final List<CodePageSelectedListener> codePageListeners = new ArrayList<> ();
  private final XmitApp xmitApp;

  private final Menu viewMenu = new Menu ("View");
  private final CheckMenuItem linesMenuItem = new CheckMenuItem ("Line numbers");
  private final CheckMenuItem controlMenuItem = new CheckMenuItem ("Control tab");
  private final CheckMenuItem debugMenuItem = new CheckMenuItem ("Debug tab");

  private final String[][] codePageNames =
      { { "CP037", "CP1140" }, { "CP285", "CP1146" }, { "CP297", "CP1147" },
        { "CP500", "CP1148" }, { "CP1047", "CP924" } };

  private final ToggleGroup toggleGroup = new ToggleGroup ();
  List<RadioMenuItem> codePageMenuItems = new ArrayList<> ();
  private final CheckMenuItem euroMenuItem = new CheckMenuItem ("Euro update");

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public ViewMenu (XmitApp xmitApp, TreeView<XmitFile> tree)
  {
    this.xmitApp = xmitApp;

    codePageMenuItems.add (setMenuItem (codePageNames[0][0], KeyCode.DIGIT1));
    codePageMenuItems.add (setMenuItem (codePageNames[1][0], KeyCode.DIGIT2));
    codePageMenuItems.add (setMenuItem (codePageNames[2][0], KeyCode.DIGIT3));
    codePageMenuItems.add (setMenuItem (codePageNames[3][0], KeyCode.DIGIT4));
    codePageMenuItems.add (setMenuItem (codePageNames[4][0], KeyCode.DIGIT5));

    viewMenu.getItems ().addAll (linesMenuItem, controlMenuItem, debugMenuItem,
        new SeparatorMenuItem ());
    for (RadioMenuItem item : codePageMenuItems)
      viewMenu.getItems ().add (item);
    viewMenu.getItems ().addAll (new SeparatorMenuItem (), euroMenuItem);

    linesMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.L, KeyCombination.SHORTCUT_DOWN));

    linesMenuItem.setOnAction (e -> notifyLinesListeners ());
    controlMenuItem.setOnAction (e -> setTabs ());
    debugMenuItem.setOnAction (e -> setTabs ());
    euroMenuItem.setOnAction (e ->
    {
      setEuro ();
      notifyCodePageListeners ();
    });
  }

  // ---------------------------------------------------------------------------------//
  // setMenuItem
  // ---------------------------------------------------------------------------------//

  private RadioMenuItem setMenuItem (String name, KeyCode keyCode)
  {
    RadioMenuItem menuItem = new RadioMenuItem (name);
    menuItem.setToggleGroup (toggleGroup);
    menuItem.setUserData (name);
    menuItem.setOnAction (e -> notifyCodePageListeners ());
    menuItem
        .setAccelerator (new KeyCodeCombination (keyCode, KeyCombination.SHORTCUT_DOWN));
    return menuItem;
  }

  // ---------------------------------------------------------------------------------//
  // notifyLinesListeners
  // ---------------------------------------------------------------------------------//

  private void notifyLinesListeners ()
  {
    for (ShowLinesListener listener : showLinesListeners)
      listener.showLinesSelected (linesMenuItem.isSelected ());
  }

  // ---------------------------------------------------------------------------------//
  // notifyCodePageListeners
  // ---------------------------------------------------------------------------------//

  private void notifyCodePageListeners ()
  {
    String codePageName = toggleGroup.getSelectedToggle ().getUserData ().toString ();
    for (CodePageSelectedListener listener : codePageListeners)
      listener.selectCodePage (codePageName);
  }

  // ---------------------------------------------------------------------------------//
  // setEuro
  // ---------------------------------------------------------------------------------//

  private void setEuro ()
  {
    int j = euroMenuItem.isSelected () ? 1 : 0;
    for (int i = 0; i < codePageNames.length; i++)
    {
      codePageMenuItems.get (i).setText (codePageNames[i][j]);
      codePageMenuItems.get (i).setUserData (codePageNames[i][j]);
    }
  }

  // ---------------------------------------------------------------------------------//
  // setTabs
  // ---------------------------------------------------------------------------------//

  private void setTabs ()
  {
    xmitApp.setTabVisible (controlMenuItem.isSelected (), debugMenuItem.isSelected ());
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  void restore ()
  {
    linesMenuItem.setSelected (prefs.getBoolean (PREFS_SHOW_LINES, false));
    notifyLinesListeners ();

    controlMenuItem.setSelected (prefs.getBoolean (PREFS_SHOW_CONTROL, false));
    debugMenuItem.setSelected (prefs.getBoolean (PREFS_SHOW_DEBUG, false));
    setTabs ();

    euroMenuItem.setSelected (prefs.getBoolean (PREFS_EURO_PAGE, false));
    setEuro ();

    int j = euroMenuItem.isSelected () ? 1 : 0;
    String codePageName = prefs.get (PREFS_CODE_PAGE, codePageNames[0][j]);

    for (int i = 0; i < codePageNames.length; i++)
      if (codePageNames[i][j].equals (codePageName))
      {
        RadioMenuItem item = codePageMenuItems.get (i);
        toggleGroup.selectToggle (item);
        item.setText (codePageName);
        break;
      }

    notifyCodePageListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.putBoolean (PREFS_SHOW_LINES, linesMenuItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_CONTROL, controlMenuItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_DEBUG, debugMenuItem.isSelected ());
    prefs.putBoolean (PREFS_EURO_PAGE, euroMenuItem.isSelected ());

    prefs.put (PREFS_CODE_PAGE,
        toggleGroup.getSelectedToggle ().getUserData ().toString ());
  }

  // ---------------------------------------------------------------------------------//
  // addListener
  // ---------------------------------------------------------------------------------//

  public void addShowLinesListener (ShowLinesListener listener)
  {
    if (!showLinesListeners.contains (listener))
      showLinesListeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  // addListener
  // ---------------------------------------------------------------------------------//

  public void addCodePageListener (CodePageSelectedListener listener)
  {
    if (!codePageListeners.contains (listener))
      codePageListeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  // getMenu
  // ---------------------------------------------------------------------------------//

  Menu getMenu ()
  {
    return viewMenu;
  }
}
