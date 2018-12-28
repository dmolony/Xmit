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
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final List<ShowLinesListener> showLinesListeners = new ArrayList<> ();
  private final List<CodePageSelectedListener> codePageListeners = new ArrayList<> ();
  private final XmitApp xmitApp;

  private final Menu viewMenu = new Menu ("View");
  private final CheckMenuItem linesMenuItem = new CheckMenuItem ("Line numbers");
  private final CheckMenuItem controlMenuItem = new CheckMenuItem ("Control tab");
  private final CheckMenuItem debugMenuItem = new CheckMenuItem ("Debug tab");

  private final ToggleGroup toggleGroup = new ToggleGroup ();
  private final RadioMenuItem cp285MenuItem = new RadioMenuItem ("CP285");
  private final RadioMenuItem cp037MenuItem = new RadioMenuItem ("CP037");
  private final RadioMenuItem cp500MenuItem = new RadioMenuItem ("CP500");
  private final RadioMenuItem cp1047MenuItem = new RadioMenuItem ("CP1047");

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public ViewMenu (XmitApp xmitApp, TreeView<XmitFile> tree)
  {
    this.xmitApp = xmitApp;

    SeparatorMenuItem separator = new SeparatorMenuItem ();

    cp037MenuItem.setToggleGroup (toggleGroup);
    cp285MenuItem.setToggleGroup (toggleGroup);
    cp500MenuItem.setToggleGroup (toggleGroup);
    cp1047MenuItem.setToggleGroup (toggleGroup);
    //    cp037MenuItem.setSelected (true);

    cp037MenuItem.setOnAction (e -> notifyCodePageListeners ());
    cp285MenuItem.setOnAction (e -> notifyCodePageListeners ());
    cp500MenuItem.setOnAction (e -> notifyCodePageListeners ());
    cp1047MenuItem.setOnAction (e -> notifyCodePageListeners ());

    cp037MenuItem.setUserData ("CP037");
    cp285MenuItem.setUserData ("CP285");
    cp500MenuItem.setUserData ("CP500");
    cp1047MenuItem.setUserData ("CP1047");

    cp037MenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN));
    cp285MenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN));
    cp500MenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN));
    cp1047MenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN));

    viewMenu.getItems ().addAll (linesMenuItem, controlMenuItem, debugMenuItem, separator,
        cp037MenuItem, cp285MenuItem, cp500MenuItem, cp1047MenuItem);
    linesMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.L, KeyCombination.SHORTCUT_DOWN));

    linesMenuItem.setOnAction (e -> notifyLinesListeners ());
    controlMenuItem.setOnAction (e -> setTabs ());
    debugMenuItem.setOnAction (e -> setTabs ());
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
    //    System.out.println (codePageName);
    //    System.out.println (codePageListeners.size ());
    for (CodePageSelectedListener listener : codePageListeners)
      listener.selectCodePage (codePageName);
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

    String codePageName = prefs.get (PREFS_CODE_PAGE, "CP037");
    switch (codePageName)
    {
      case "CP037":
        //        cp037MenuItem.setSelected (true);
        toggleGroup.selectToggle (cp037MenuItem);
        break;
      case "CP285":
        //        cp285MenuItem.setSelected (true);
        toggleGroup.selectToggle (cp285MenuItem);
        break;
      case "CP500":
        //        cp500MenuItem.setSelected (true);
        toggleGroup.selectToggle (cp500MenuItem);
        break;
      case "CP1047":
        //        cp1047MenuItem.setSelected (true);
        toggleGroup.selectToggle (cp1047MenuItem);
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
