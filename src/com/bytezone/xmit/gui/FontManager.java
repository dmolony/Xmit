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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

class FontManager
{
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private static final String PREFS_FONT_NAME = "FontName";
  private static final String PREFS_FONT_SIZE = "FontSize";
  private static final String PREFS_FONTS_SELECTED = "FontsSelected";

  private static final String REQUIRED_FONT = "Monospaced";
  private static final int MIN_FONT_SIZE = 9;
  private static final int MAX_FONT_SIZE = 15;

  List<FontChangeListener> listeners = new ArrayList<> ();
  int currentFontIndex;
  int currentSize;
  Font currentFont;

  Stage stage;
  TextArea text;

  ArrayList<String> fontNameSubList = new ArrayList<> (); // sublist to cycle through
  ListView<FontName> fontNameListView;                    // list used to create sublist
  IntegerSpinnerValueFactory factory =
      new IntegerSpinnerValueFactory (MIN_FONT_SIZE, MAX_FONT_SIZE);

  //  String savedFontName;
  int savedFontIndex;
  int savedFontSize;

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
      names.addAll (getMonospacedFonts ());
      fontNameListView = new ListView<> (names);

      fontNameListView.getSelectionModel ().selectedItemProperty ()
          .addListener ( (a, b, c) -> text.setFont (getFont ()));

      fontNameListView.setCellFactory (CheckBoxListCell
          .forListView (new Callback<FontName, ObservableValue<Boolean>> ()
          {
            @Override
            public ObservableValue<Boolean> call (FontName item)
            {
              return item.onProperty ();
            }
          }));

      factory.setWrapAround (true);
      factory.valueProperty ().addListener ( (a, b, c) -> text.setFont (getFont ()));

      HBox hbox = new HBox (10);
      hbox.setPrefHeight (20);
      hbox.setAlignment (Pos.CENTER);
      hbox.setPadding (new Insets (6, 10, 6, 10));
      Button btnApply = new Button ("Apply");
      Button btnCancel = new Button ("Cancel");
      Button btnAccept = new Button ("Accept");
      Spinner<Integer> fontSize;
      fontSize = new Spinner<> (factory);
      Region filler = new Region ();
      HBox.setHgrow (filler, Priority.ALWAYS);
      hbox.getChildren ().addAll (fontSize, filler, btnCancel, btnApply, btnAccept);

      btnAccept.setOnAction (e -> accept ());
      btnApply.setOnAction (e -> apply ());
      btnCancel.setOnAction (e -> cancel ());

      borderPane.setLeft (fontNameListView);
      borderPane.setCenter (text);
      borderPane.setBottom (hbox);

      stage.setScene (new Scene (borderPane, 1200, 700));
    }

    savedFontIndex = currentFontIndex;
    savedFontSize = currentSize;

    fontNameListView.getSelectionModel ()
        .select (getIndex (fontNameSubList.get (currentFontIndex)));
    factory.setValue (savedFontSize);

    stage.show ();
  }

  // ---------------------------------------------------------------------------------//
  // accept
  // ---------------------------------------------------------------------------------//

  private void accept ()
  {
    // rebuild the list - fonts may have been added or removed
    fontNameSubList.clear ();
    for (FontName fontName : fontNameListView.getItems ())
      if (fontName.isOn () || fontName.getName ().equals (REQUIRED_FONT))
        fontNameSubList.add (fontName.getName ());

    apply ();

    stage.hide ();
  }

  // ---------------------------------------------------------------------------------//
  // cancel
  // ---------------------------------------------------------------------------------//

  private void cancel ()
  {
    // this doesn't revert the checkboxes
    currentFontIndex = savedFontIndex;//fontNameSubList.indexOf (savedFontName);
    currentSize = savedFontSize;
    notifyListeners ();
    stage.hide ();
  }

  // ---------------------------------------------------------------------------------//
  // apply
  // ---------------------------------------------------------------------------------//

  private void apply ()
  {
    currentFontIndex = fontNameSubList
        .indexOf (fontNameListView.getSelectionModel ().getSelectedItem ().getName ());
    if (currentFontIndex < 0)
      currentFontIndex = fontNameSubList.indexOf (REQUIRED_FONT);
    currentSize = factory.getValue ();
    notifyListeners ();
    System.out.println ("Applying: " + fontNameSubList.get (currentFontIndex));
  }

  // ---------------------------------------------------------------------------------//
  // getFont
  // ---------------------------------------------------------------------------------//

  Font getFont ()
  {
    FontName name = fontNameListView.getSelectionModel ().getSelectedItem ();
    System.out.println ("Selected: " + name);
    System.out.println (factory.getValue ());
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
    ++currentFontIndex;
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // previous
  // ---------------------------------------------------------------------------------//

  private void previous ()
  {
    --currentFontIndex;
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // setFont
  // ---------------------------------------------------------------------------------//

  private void setCurrentFont ()
  {
    if (currentFontIndex >= fontNameSubList.size ())
      currentFontIndex = 0;
    else if (currentFontIndex < 0)
      currentFontIndex = fontNameSubList.size () - 1;

    if (currentSize < MIN_FONT_SIZE)
      currentSize = MAX_FONT_SIZE;
    else if (currentSize > MAX_FONT_SIZE)
      currentSize = MIN_FONT_SIZE;

    currentFont = Font.font (fontNameSubList.get (currentFontIndex), currentSize);
  }

  // ---------------------------------------------------------------------------------//
  // notify
  // ---------------------------------------------------------------------------------//

  private void notifyListeners ()
  {
    setCurrentFont ();
    for (FontChangeListener fontChangeListener : listeners)
      fontChangeListener.setFont (currentFont);
  }

  // ---------------------------------------------------------------------------------//
  //restore
  // ---------------------------------------------------------------------------------//

  void restore ()
  {
    System.out.println ("Restoring:");
    fontNameSubList.addAll (
        Arrays.asList (prefs.get (PREFS_FONTS_SELECTED, REQUIRED_FONT).split (";")));
    for (String fontName : fontNameSubList)
      System.out.println ("  " + fontName);

    currentSize = prefs.getInt (PREFS_FONT_SIZE, 13);
    String name = prefs.get (PREFS_FONT_NAME, REQUIRED_FONT);
    System.out.println ("Selecting: " + name);
    currentFontIndex = getCurrentFont (name);
    System.out.println (currentFontIndex);
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.put (PREFS_FONT_NAME, fontNameSubList.get (currentFontIndex));
    prefs.putInt (PREFS_FONT_SIZE, currentSize);

    System.out.println ("Saving:");
    StringBuilder text = new StringBuilder ();
    for (String fontName : fontNameSubList)
    {
      System.out.println ("  " + fontName);
      text.append (fontName + ";");
    }
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    prefs.put (PREFS_FONTS_SELECTED, text.toString ());
    System.out.println ("Saving: " + fontNameSubList.get (currentFontIndex));
  }

  // ---------------------------------------------------------------------------------//
  // setCurrentFont
  // ---------------------------------------------------------------------------------//

  private int getCurrentFont (String fontName)
  {
    int count = 0;
    for (String name : fontNameSubList)
    {
      if (name.equals (fontName))
        return count;
      ++count;
    }
    return -1;
  }

  // ---------------------------------------------------------------------------------//
  // getIndex
  // ---------------------------------------------------------------------------------//

  private int getIndex (String name)
  {
    int count = 0;
    for (FontName fontName : fontNameListView.getItems ())
    {
      if (fontName.getName ().equals (name))
        return count;
      ++count;
    }
    return -1;
  }

  // ---------------------------------------------------------------------------------//
  // getMonospacedFonts
  // ---------------------------------------------------------------------------------//

  List<FontName> getMonospacedFonts ()
  {
    final Text thinTxt = new Text ("ii11");
    final Text thikTxt = new Text ("WWMM");

    List<String> fontFamilyList = Font.getFamilies ();
    List<FontName> monospacedFonts = new ArrayList<> ();

    System.out.println ("Reading:");
    for (String fontFamilyName : fontFamilyList)
    {
      Font font =
          Font.font (fontFamilyName, FontWeight.NORMAL, FontPosture.REGULAR, 14.0d);
      thinTxt.setFont (font);
      thikTxt.setFont (font);
      if (thinTxt.getLayoutBounds ().getWidth () == thikTxt.getLayoutBounds ()
          .getWidth ())
      {
        FontName item = new FontName (fontFamilyName, false);
        monospacedFonts.add (item);
        System.out.println ("  " + fontFamilyName);
        if (fontNameSubList.contains (fontFamilyName))
          item.setOn (true);
        if (false)
          item.onProperty ().addListener ( (obs, wasOn, isNowOn) ->
          {
            System.out.println (
                item.getName () + " changed on state from " + wasOn + " to " + isNowOn);
            System.out.printf ("%s %s%n", item, isNowOn);
          });
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
