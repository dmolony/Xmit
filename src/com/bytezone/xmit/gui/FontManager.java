package com.bytezone.xmit.gui;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

class FontManager
{
  private static final String PREFS_FONT_NAME = "FontName";
  private static final String PREFS_FONT_SIZE = "FontSize";
  private static final String PREFS_FONTS_SELECTED = "FontsSelected";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  List<FontChangeListener> listeners = new ArrayList<> ();
  List<FontName> fontNames;// = getMonospacedFonts ();
  int currentFont;
  int currentSize;
  Font font;
  Stage stage;
  TextArea text;

  ListView<FontName> fontList;
  IntegerSpinnerValueFactory factory = new IntegerSpinnerValueFactory (9, 15);

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public FontManager ()
  {

  }

  // ---------------------------------------------------------------------------------//
  // fontHandler
  // ---------------------------------------------------------------------------------//

  void manageFonts ()
  {
    if (stage == null)
    {
      stage = new Stage ();
      stage.setTitle ("Font Manager");

      text = getTextArea ();
      text.setPrefWidth (750);
      text.setEditable (false);

      BorderPane borderPane = new BorderPane ();
      ObservableList<FontName> names = FXCollections.observableArrayList ();
      names.addAll (fontNames);
      fontList = new ListView<> (names);

      fontList.getSelectionModel ().selectedItemProperty ()
          .addListener ( (a, b, c) -> text.setFont (getFont ()));
      fontList.getSelectionModel ().select (currentFont);

      fontList.setCellFactory (CheckBoxListCell
          .forListView (new Callback<FontName, ObservableValue<Boolean>> ()
          {
            @Override
            public ObservableValue<Boolean> call (FontName item)
            {
              return item.onProperty ();
            }
          }));

      HBox hbox = new HBox (10);
      hbox.setPrefHeight (20);
      hbox.setAlignment (Pos.CENTER);
      hbox.setPadding (new Insets (6, 10, 6, 10));
      Button btnApply = new Button ("Apply");
      Button btnCancel = new Button ("Cancel");
      Button btnClose = new Button ("Accept");
      Spinner<Integer> fontSize;
      fontSize = new Spinner<> (factory);
      hbox.getChildren ().addAll (fontSize, btnCancel, btnApply, btnClose);

      factory.setWrapAround (true);
      factory.valueProperty ().addListener ( (a, b, c) -> text.setFont (getFont ()));
      factory.setValue (currentSize);

      borderPane.setLeft (fontList);
      borderPane.setCenter (text);
      borderPane.setBottom (hbox);

      stage.setScene (new Scene (borderPane, 1000, 600));
    }

    stage.show ();
  }

  // ---------------------------------------------------------------------------------//
  // getFont
  // ---------------------------------------------------------------------------------//

  Font getFont ()
  {
    FontName name = fontList.getSelectionModel ().getSelectedItem ();
    return Font.font (name.getName (), factory.getValue ());
  }

  // ---------------------------------------------------------------------------------//
  // addShowLinesListener
  // ---------------------------------------------------------------------------------//

  public void addFontChangeListener (FontChangeListener listener)
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
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

    font = Font.font (fontNames.get (currentFont).getName (), currentSize);
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
    List<String> fontsSelected =
        Arrays.asList (prefs.get (PREFS_FONTS_SELECTED, "Monospaced").split (";"));
    fontNames = getMonospacedFonts ();
    for (FontName fontName : fontNames)
      if (fontsSelected.contains (fontName.getName ()))
        fontName.setOn (true);

    currentSize = prefs.getInt (PREFS_FONT_SIZE, 13);
    setCurrentFont (prefs.get (PREFS_FONT_NAME, "Monospaced"));
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.put (PREFS_FONT_NAME, fontNames.get (currentFont).getName ());
    prefs.putInt (PREFS_FONT_SIZE, currentSize);

    StringBuilder text = new StringBuilder ();
    for (FontName fontName : fontNames)
    {
      if (fontName.isOn ())
        text.append (fontName + ";");
    }
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    prefs.put (PREFS_FONTS_SELECTED, text.toString ());
    System.out.println (text.toString ());
  }

  // ---------------------------------------------------------------------------------//
  // setCurrentFont
  // ---------------------------------------------------------------------------------//

  private void setCurrentFont (String fontName)
  {
    int count = 0;
    for (FontName name : fontNames)
    {
      if (name.getName ().equals (fontName))
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

  List<FontName> getMonospacedFonts ()
  {
    final Text thinTxt = new Text ("ii11");
    final Text thikTxt = new Text ("WWMM");

    List<String> fontFamilyList = Font.getFamilies ();
    List<FontName> monospacedFonts = new ArrayList<> ();

    for (String fontFamilyName : fontFamilyList)
    {
      Font font =
          Font.font (fontFamilyName, FontWeight.NORMAL, FontPosture.REGULAR, 14.0d);
      thinTxt.setFont (font);
      thikTxt.setFont (font);
      if (thinTxt.getLayoutBounds ().getWidth () == thikTxt.getLayoutBounds ()
          .getWidth ())
      {
        FontName item = new FontName (fontFamilyName, true);
        monospacedFonts.add (item);
        //        item.onProperty ().addListener ( (obs, wasOn, isNowOn) ->
        //        {
        //          System.out.println (
        //              item.getName () + " changed on state from "
        //+ wasOn + " to " + isNowOn);
        //        });
      }
    }

    return monospacedFonts;
  }

  // ---------------------------------------------------------------------------------//
  // getTextArea
  // ---------------------------------------------------------------------------------//

  TextArea getTextArea ()
  {
    StringBuilder text = new StringBuilder ();
    String line;

    DataInputStream inputEquates = new DataInputStream (XmitApp.class.getClassLoader ()
        .getResourceAsStream ("com/bytezone/xmit/gui/jcl.txt"));
    try (BufferedReader in = new BufferedReader (new InputStreamReader (inputEquates)))
    {
      while ((line = in.readLine ()) != null)
        text.append (line + "\n");
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return new TextArea (text.toString ());
  }

  // ---------------------------------------------------------------------------------//
  // FontName
  // ---------------------------------------------------------------------------------//

  private static class FontName
  {
    private final StringProperty name = new SimpleStringProperty ();
    private final BooleanProperty on = new SimpleBooleanProperty ();
    //    private Font font;

    public FontName (String name, boolean on)
    {
      setName (name);
      setOn (on);
    }

    public final StringProperty nameProperty ()
    {
      return this.name;
    }

    public final String getName ()
    {
      return this.nameProperty ().get ();
    }

    public final void setName (final String name)
    {
      this.nameProperty ().set (name);
    }

    public final BooleanProperty onProperty ()
    {
      return this.on;
    }

    public final boolean isOn ()
    {
      return this.onProperty ().get ();
    }

    public final void setOn (final boolean on)
    {
      this.onProperty ().set (on);
    }

    @Override
    public String toString ()
    {
      return getName ();
    }
  }
}
