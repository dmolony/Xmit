package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

class FontManager
{
  private static final String PREFS_FONT_NAME = "FontName";
  private static final String PREFS_FONT_SIZE = "FontSize";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  List<FontChangeListener> listeners = new ArrayList<> ();
  List<String> fontNames = getMonospacedFonts ();
  int currentFont;
  int currentSize;
  Font font;

  // ---------------------------------------------------------------------------------//
  // addShowLinesListener
  // ---------------------------------------------------------------------------------//

  public void addFontChangeListener (FontChangeListener listener)
  {
    if (!listeners.contains (listener))
    {
      listeners.add (listener);
      //      listener.setFont (font);
    }
  }

  // ---------------------------------------------------------------------------------//
  // KeyEvent
  // ---------------------------------------------------------------------------------//

  void keyPressed (KeyEvent keyEvent)
  {
    KeyCode keyCode = keyEvent.getCode ();
    boolean shiftDown = keyEvent.isShiftDown ();

    if (keyCode == KeyCode.COMMA)
      if (shiftDown)
        smaller ();
      else
        previous ();
    else if (keyCode == KeyCode.PERIOD)
      if (shiftDown)
        bigger ();
      else
        next ();
  }

  // ---------------------------------------------------------------------------------//
  // bigger
  // ---------------------------------------------------------------------------------//

  private void bigger ()
  {
    ++currentSize;
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // smaller
  // ---------------------------------------------------------------------------------//

  private void smaller ()
  {
    --currentSize;
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // next
  // ---------------------------------------------------------------------------------//

  private void next ()
  {
    ++currentFont;
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // previous
  // ---------------------------------------------------------------------------------//

  private void previous ()
  {
    --currentFont;
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // setFont
  // ---------------------------------------------------------------------------------//

  private void setFont ()
  {
    if (currentFont >= fontNames.size ())
      currentFont = 0;
    else if (currentFont < 0)
      currentFont = fontNames.size () - 1;

    if (currentSize < 7)
      currentSize = 20;
    else if (currentSize > 20)
      currentSize = 7;

    font = Font.font (fontNames.get (currentFont), currentSize);
  }

  // ---------------------------------------------------------------------------------//
  // notify
  // ---------------------------------------------------------------------------------//

  private void notifyListeners ()
  {
    setFont ();
    for (FontChangeListener fontChangeListener : listeners)
      fontChangeListener.setFont (font);
  }

  // ---------------------------------------------------------------------------------//
  //restore
  // ---------------------------------------------------------------------------------//

  void restore ()
  {
    currentSize = prefs.getInt (PREFS_FONT_SIZE, 13);
    setCurrentFont (prefs.get (PREFS_FONT_NAME, "Monospaced"));
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.put (PREFS_FONT_NAME, fontNames.get (currentFont));
    prefs.putInt (PREFS_FONT_SIZE, currentSize);
  }

  // ---------------------------------------------------------------------------------//
  // setCurrentFont
  // ---------------------------------------------------------------------------------//

  private void setCurrentFont (String fontName)
  {
    int count = 0;
    for (String name : fontNames)
    {
      if (name.equals (fontName))
      {
        currentFont = count;
        break;
      }
      ++count;
    }
  }

  // ---------------------------------------------------------------------------------//
  //getMonospacedFonts
  // ---------------------------------------------------------------------------------//

  List<String> getMonospacedFonts ()
  {
    final Text thinTxt = new Text ("1 l");
    final Text thikTxt = new Text ("MWX");

    List<String> fontFamilyList = Font.getFamilies ();
    List<String> monospacedFonts = new ArrayList<> ();

    for (String fontFamilyName : fontFamilyList)
    {
      Font font =
          Font.font (fontFamilyName, FontWeight.NORMAL, FontPosture.REGULAR, 14.0d);
      thinTxt.setFont (font);
      thikTxt.setFont (font);
      if (thinTxt.getLayoutBounds ().getWidth () == thikTxt.getLayoutBounds ()
          .getWidth ())
        monospacedFonts.add (fontFamilyName);
    }

    return monospacedFonts;
  }
}
