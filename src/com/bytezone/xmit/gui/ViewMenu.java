package com.bytezone.xmit.gui;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.appbase.AppBase;
import com.bytezone.appbase.SaveState;
import com.bytezone.xmit.Utility;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

// -----------------------------------------------------------------------------------//
class ViewMenu extends Menu implements SaveState
// -----------------------------------------------------------------------------------//
{
  private static final String PREFS_CODE_PAGE = "CodePage";
  private static final String PREFS_EURO_PAGE = "EuroPage";

  private static final boolean SHIFT = true;
  private static final boolean NO_SHIFT = !SHIFT;

  private final List<ShowLinesListener> showLinesListeners = new ArrayList<> ();
  private final List<CodePageSelectedListener> codePageListeners = new ArrayList<> ();
  private final LineDisplayStatus lineDisplayStatus = new LineDisplayStatus ();

  private final MenuItem fontMenuItem = new MenuItem ("Set Font...");
  private final MenuItem filterMenuItem = new MenuItem ("Set PDS Filter...");
  private final CheckMenuItem exclusiveFilterMenuItem = new CheckMenuItem ("Exclusive Filter");

  private final CheckMenuItem showLinesMenuItem;
  private final CheckMenuItem stripLinesMenuItem;
  private final CheckMenuItem truncateMenuItem;
  private final CheckMenuItem expandIncludeMenuItem;
  private final CheckMenuItem euroMenuItem;

  private final String[][]                    //
  codePageNames = {                           //
      { "CP037", "CP1140" },     // USA/Canada
      { "CP273", "CP1141" },     // Germany
      { "CP285", "CP1146" },     // UK
      { "CP297", "CP1147" },     // France
      { "CP500", "CP1148" },     // International #5
      { "CP870", "CP1153" },     // Latin-2
      { "CP1047", "CP924" },     // Latin-1
      { "USER1", "USER1" } };    // 1047 with NL/LF swapped

  private final KeyCode[] keyCodes = { KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3,
      KeyCode.DIGIT4, KeyCode.DIGIT5, KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8 };

  private final ToggleGroup toggleGroup = new ToggleGroup ();
  private final List<RadioMenuItem> codePageMenuItems = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public ViewMenu (String name)
  // ---------------------------------------------------------------------------------//
  {
    super (name);

    ObservableList<MenuItem> menuItems = getItems ();

    // Filter keys
    KeyCodeCombination cmdShiftF =
        new KeyCodeCombination (KeyCode.F, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    KeyCodeCombination cmdF = new KeyCodeCombination (KeyCode.F, KeyCombination.SHORTCUT_DOWN);
    KeyCodeCombination cmdAltF =
        new KeyCodeCombination (KeyCode.F, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);

    fontMenuItem.setAccelerator (cmdAltF);
    filterMenuItem.setAccelerator (cmdF);
    exclusiveFilterMenuItem.setAccelerator (cmdShiftF);

    menuItems.add (fontMenuItem);
    menuItems.add (filterMenuItem);
    menuItems.add (exclusiveFilterMenuItem);

    menuItems.add (new SeparatorMenuItem ());

    EventHandler<ActionEvent> action = e -> alterLineStatus ();

    showLinesMenuItem = addCheckMenuItem ("Add Sequence Numbers", KeyCode.L, action);
    stripLinesMenuItem = addCheckMenuItem ("Strip Line Numbers", KeyCode.L, SHIFT, action);
    truncateMenuItem = addCheckMenuItem ("Truncate Column 1", KeyCode.T, action);
    expandIncludeMenuItem = addCheckMenuItem ("Expand Include Members", KeyCode.I, action);

    menuItems.add (new SeparatorMenuItem ());

    for (int i = 0; i < codePageNames.length; i++)
    {
      RadioMenuItem item = setRadioMenuItem (codePageNames[i][0], keyCodes[i]);
      codePageMenuItems.add (item);
      menuItems.add (item);
    }

    menuItems.add (new SeparatorMenuItem ());

    euroMenuItem =
        addCheckMenuItem ("Euro update", KeyCode.DIGIT9, e -> setEuroAndNotifyListeners ());
  }

  // ---------------------------------------------------------------------------------//
  void setFontAction (EventHandler<ActionEvent> action)
  // ---------------------------------------------------------------------------------//
  {
    fontMenuItem.setOnAction (action);
  }

  // ---------------------------------------------------------------------------------//
  void setFilterAction (EventHandler<ActionEvent> action)
  // ---------------------------------------------------------------------------------//
  {
    filterMenuItem.setOnAction (action);
  }

  // ---------------------------------------------------------------------------------//
  void setExclusiveFilterAction (EventHandler<ActionEvent> action)
  // ---------------------------------------------------------------------------------//
  {
    exclusiveFilterMenuItem.setOnAction (action);
  }

  // ---------------------------------------------------------------------------------//
  private CheckMenuItem addCheckMenuItem (String name, KeyCode keyCode,
      EventHandler<ActionEvent> action)
  // ---------------------------------------------------------------------------------//
  {
    return addCheckMenuItem (name, keyCode, NO_SHIFT, action);
  }

  // ---------------------------------------------------------------------------------//
  private CheckMenuItem addCheckMenuItem (String name, KeyCode keyCode, boolean shift,
      EventHandler<ActionEvent> action)
  // ---------------------------------------------------------------------------------//
  {
    CheckMenuItem menuItem = new CheckMenuItem (name);
    getItems ().add (menuItem);

    if (keyCode != null)
      if (shift)
        menuItem.setAccelerator (new KeyCodeCombination (keyCode, KeyCombination.SHORTCUT_DOWN,
            KeyCombination.SHIFT_DOWN));
      else
        menuItem.setAccelerator (new KeyCodeCombination (keyCode, KeyCombination.SHORTCUT_DOWN));

    menuItem.setOnAction (action);

    return menuItem;
  }

  // ---------------------------------------------------------------------------------//
  private RadioMenuItem setRadioMenuItem (String name, KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    RadioMenuItem menuItem = new RadioMenuItem (name);
    menuItem.setToggleGroup (toggleGroup);
    menuItem.setUserData (name);
    menuItem.setOnAction (e -> notifyCodePageListeners ());
    menuItem.setAccelerator (new KeyCodeCombination (keyCode, KeyCombination.SHORTCUT_DOWN));
    return menuItem;
  }

  // ---------------------------------------------------------------------------------//
  private void alterLineStatus ()
  // ---------------------------------------------------------------------------------//
  {
    lineDisplayStatus.set (                     //
        showLinesMenuItem.isSelected (),        // Add Sequence Numbers
        stripLinesMenuItem.isSelected (),       // Strip Line Numbers
        truncateMenuItem.isSelected (),         // Truncate Column 1
        expandIncludeMenuItem.isSelected ());   // Expand Include Members
    notifyLinesListeners ();
  }

  // ---------------------------------------------------------------------------------//
  private void notifyLinesListeners ()
  // ---------------------------------------------------------------------------------//
  {
    LineDisplayStatus copy = new LineDisplayStatus (lineDisplayStatus);
    for (ShowLinesListener listener : showLinesListeners)
      listener.showLinesSelected (copy);
  }

  // ---------------------------------------------------------------------------------//
  private void notifyCodePageListeners ()
  // ---------------------------------------------------------------------------------//
  {
    Toggle toggle = toggleGroup.getSelectedToggle ();
    if (toggle == null)
    {
      System.out.println ("Nothing selected");        // windows bug
    }
    else
    {
      Object o = toggle.getUserData ();
      String codePageName = o.toString ();
      if (!codePageName.startsWith ("USER") && !Charset.isSupported (codePageName))
        AppBase.showAlert (AlertType.ERROR, "Encoding Exception",
            "Charset " + codePageName + " is not supported");
      else
      {
        Utility.setCodePage (codePageName);     // ensure correct code page is set first
        for (CodePageSelectedListener listener : codePageListeners)
          listener.selectCodePage (codePageName);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private void setEuroAndNotifyListeners ()
  // ---------------------------------------------------------------------------------//
  {
    int j = euroMenuItem.isSelected () ? 1 : 0;
    for (int i = 0; i < codePageNames.length; i++)
    {
      codePageMenuItems.get (i).setText (codePageNames[i][j]);
      codePageMenuItems.get (i).setUserData (codePageNames[i][j]);
    }
    notifyCodePageListeners ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    lineDisplayStatus.restore (prefs);

    showLinesMenuItem.setSelected (lineDisplayStatus.showLines);
    stripLinesMenuItem.setSelected (lineDisplayStatus.stripLines);
    truncateMenuItem.setSelected (lineDisplayStatus.truncateLines);
    expandIncludeMenuItem.setSelected (lineDisplayStatus.expandInclude);

    notifyLinesListeners ();

    euroMenuItem.setSelected (prefs.getBoolean (PREFS_EURO_PAGE, false));

    int j = euroMenuItem.isSelected () ? 1 : 0;
    String codePageName = prefs.get (PREFS_CODE_PAGE, codePageNames[0][j]);

    for (int i = 0; i < codePageNames.length; i++)
      if (codePageNames[i][j].equals (codePageName))
      {
        toggleGroup.selectToggle (codePageMenuItems.get (i));
        break;
      }

    setEuroAndNotifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void save (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    lineDisplayStatus.save (prefs);
    prefs.putBoolean (PREFS_EURO_PAGE, euroMenuItem.isSelected ());

    Toggle toggle = toggleGroup.getSelectedToggle ();
    if (toggle != null)
      prefs.put (PREFS_CODE_PAGE, toggle.getUserData ().toString ());
  }

  // ---------------------------------------------------------------------------------//
  public void addShowLinesListener (ShowLinesListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!showLinesListeners.contains (listener))
      showLinesListeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  public void addCodePageListener (CodePageSelectedListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!codePageListeners.contains (listener))
      codePageListeners.add (listener);
  }
}
