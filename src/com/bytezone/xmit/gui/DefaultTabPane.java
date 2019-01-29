package com.bytezone.xmit.gui;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;

public abstract class DefaultTabPane extends DefaultPane
{
  final XmitTab[] tabs;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public DefaultTabPane (int numTabs)
  {
    tabs = new XmitTab[numTabs];
  }

  // ---------------------------------------------------------------------------------//
  // createTab
  // ---------------------------------------------------------------------------------//

  XmitTab createTab (String title, KeyCode keyCode, TabUpdater tabUpdater)
  {
    Tab tab = new Tab (title);
    TextArea text = new TextArea ();
    text.setWrapText (false);
    tab.setContent (text);
    text.setEditable (false);
    XmitTab xmitTab = new XmitTab (tab, text, keyCode, tabUpdater);
    tab.setUserData (xmitTab);
    return xmitTab;
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
  // setFont
  // ---------------------------------------------------------------------------------//

  public void setFont (Font font)
  {
    for (XmitTab tab : tabs)
      tab.setFont (font);
  }
}
