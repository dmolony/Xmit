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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

// -----------------------------------------------------------------------------------//
class FontManager implements SaveState
// -----------------------------------------------------------------------------------//
{
  private static final String PREFS_FONT_NAME = "FontName";
  private static final String PREFS_FONT_SIZE = "FontSize";
  private static final String PREFS_FONTS_SELECTED = "FontsSelected";

  private static final String REQUIRED_FONT_NAME = "Monospaced";
  private static final int DEFAULT_FONT_SIZE = 13;
  private static final int MIN_FONT_SIZE = 9;
  private static final int MAX_FONT_SIZE = 15;

  private final List<FontChangeListener> listeners = new ArrayList<> ();

  private Stage stage;
  private TextArea text;

  private final ArrayList<String> fontNameSubList = new ArrayList<> ();
  private ListView<FontName> fontNameListView;
  private final IntegerSpinnerValueFactory factory =
      new IntegerSpinnerValueFactory (MIN_FONT_SIZE, MAX_FONT_SIZE);

  private int currentFontIndex;       // index into fontNameSubList
  private int currentFontSize;
  private int savedFontIndex;
  private int savedFontSize;

  // ---------------------------------------------------------------------------------//
  void showWindow ()
  // ---------------------------------------------------------------------------------//
  {
    if (stage == null)
      buildStage ();

    savedFontIndex = currentFontIndex;
    savedFontSize = currentFontSize;

    setSelections ();

    stage.show ();
    stage.toFront ();
  }

  // ---------------------------------------------------------------------------------//
  private Button getButton (String text)
  // ---------------------------------------------------------------------------------//
  {
    Button button = new Button (text);
    button.setMinWidth (100);
    return button;
  }

  // ---------------------------------------------------------------------------------//
  private void setSelections ()
  // ---------------------------------------------------------------------------------//
  {
    String name = fontNameSubList.get (currentFontIndex);
    int count = 0;
    for (FontName fontName : fontNameListView.getItems ())
    {
      if (fontName.getName ().equals (name))
      {
        fontNameListView.getSelectionModel ().select (count);
        break;
      }
      ++count;
    }
    factory.setValue (currentFontSize);
  }

  // ---------------------------------------------------------------------------------//
  private void setTextFont ()
  // ---------------------------------------------------------------------------------//
  {
    text.setFont (Font.font (getSelectedName (), factory.getValue ()));
  }

  // ---------------------------------------------------------------------------------//
  private void rebuildSubList ()
  // ---------------------------------------------------------------------------------//
  {
    fontNameSubList.clear ();
    for (FontName fontName : fontNameListView.getItems ())
      if (fontName.isOn () || fontName.getName ().equals (REQUIRED_FONT_NAME))
        fontNameSubList.add (fontName.getName ());

    //    ObservableList<FontName> selectedFontNames =
    //        fontNameListView.getItems ().filtered (FontName::isOn);
    //    for (FontName fontName : selectedFontNames)
    //      System.out.println (fontName);
  }

  // ---------------------------------------------------------------------------------//
  private void accept ()
  // ---------------------------------------------------------------------------------//
  {
    apply ();
    stage.hide ();
  }

  // ---------------------------------------------------------------------------------//
  private void cancel ()
  // ---------------------------------------------------------------------------------//
  {
    // this doesn't revert the checkboxes
    if (currentFontIndex != savedFontIndex || currentFontSize != savedFontSize)
    {
      currentFontIndex = savedFontIndex;
      currentFontSize = savedFontSize;
      notifyListeners ();
    }

    stage.hide ();
  }

  // ---------------------------------------------------------------------------------//
  private void apply ()
  // ---------------------------------------------------------------------------------//
  {
    setCurrentFontIndex (getSelectedName ());
    currentFontSize = factory.getValue ();
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  private void setCurrentFontIndex (String name)
  // ---------------------------------------------------------------------------------//
  {
    currentFontIndex = fontNameSubList.indexOf (name);
    if (currentFontIndex < 0)
      currentFontIndex = fontNameSubList.indexOf (REQUIRED_FONT_NAME);
  }

  // ---------------------------------------------------------------------------------//
  private String getSelectedName ()
  // ---------------------------------------------------------------------------------//
  {
    return fontNameListView.getSelectionModel ().getSelectedItem ().getName ();
  }

  // ---------------------------------------------------------------------------------//
  void keyPressed (KeyEvent keyEvent)
  // ---------------------------------------------------------------------------------//
  {
    switch (keyEvent.getCode ())
    {
      case COMMA:
        if (keyEvent.isShiftDown ())
          --currentFontSize;
        else
          --currentFontIndex;
        break;

      case PERIOD:
        if (keyEvent.isShiftDown ())
          ++currentFontSize;
        else
          ++currentFontIndex;
        break;

      default:
        return;         // not mine - shouldn't happen
    }

    keyEvent.consume ();
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  private void notifyListeners ()
  // ---------------------------------------------------------------------------------//
  {
    if (currentFontIndex >= fontNameSubList.size ())
      currentFontIndex = 0;
    else if (currentFontIndex < 0)
      currentFontIndex = fontNameSubList.size () - 1;

    if (currentFontSize < MIN_FONT_SIZE)
      currentFontSize = MAX_FONT_SIZE;
    else if (currentFontSize > MAX_FONT_SIZE)
      currentFontSize = MIN_FONT_SIZE;

    Font font = Font.font (fontNameSubList.get (currentFontIndex), currentFontSize);
    for (FontChangeListener fontChangeListener : listeners)
      fontChangeListener.setFont (font);
  }

  // ---------------------------------------------------------------------------------//
  public void addFontChangeListener (FontChangeListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    fontNameSubList.addAll (
        Arrays.asList (prefs.get (PREFS_FONTS_SELECTED, REQUIRED_FONT_NAME).split (";")));

    currentFontSize = prefs.getInt (PREFS_FONT_SIZE, DEFAULT_FONT_SIZE);
    setCurrentFontIndex (prefs.get (PREFS_FONT_NAME, REQUIRED_FONT_NAME));

    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void save (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    if (stage != null && stage.isShowing ())
    {
      currentFontIndex = savedFontIndex;
      currentFontSize = savedFontSize;
    }

    prefs.put (PREFS_FONT_NAME, fontNameSubList.get (currentFontIndex));
    prefs.putInt (PREFS_FONT_SIZE, currentFontSize);

    StringBuilder text = new StringBuilder ();
    for (String fontName : fontNameSubList)
      text.append (fontName + ";");

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    prefs.put (PREFS_FONTS_SELECTED, text.toString ());
  }

  // ---------------------------------------------------------------------------------//
  private List<FontName> getMonospacedFonts ()
  // ---------------------------------------------------------------------------------//
  {
    final Text thinTxt = new Text ("....");
    final Text thikTxt = new Text ("WWMM");

    List<FontName> monospacedFonts = new ArrayList<> ();

    for (String familyName : Font.getFamilies ())
    {
      Font font = Font.font (familyName, FontWeight.NORMAL, FontPosture.REGULAR, 15.0);
      thinTxt.setFont (font);
      thikTxt.setFont (font);
      if (thinTxt.getLayoutBounds ().getWidth () == thikTxt.getLayoutBounds ()
          .getWidth ())
      {
        FontName fontName = new FontName (familyName);
        fontName.setOn (fontNameSubList.contains (familyName));
        monospacedFonts.add (fontName);
        fontName.onProperty ().addListener ( (obs, o, n) -> rebuildSubList ());
      }
    }

    return monospacedFonts;
  }

  // ---------------------------------------------------------------------------------//
  private TextArea getTextArea ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    String line;

    DataInputStream inputStream = new DataInputStream (XmitApp.class.getClassLoader ()
        .getResourceAsStream ("com/bytezone/xmit/gui/jcl.txt"));
    try (BufferedReader in = new BufferedReader (new InputStreamReader (inputStream)))
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
  private void buildStage ()
  // ---------------------------------------------------------------------------------//
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
        .addListener ( (obs, o, n) -> setTextFont ());

    //      fontNameListView.setCellFactory (CheckBoxListCell
    //          .forListView (new Callback<FontName, ObservableValue<Boolean>> ()
    //          {
    //            @Override
    //            public ObservableValue<Boolean> call (FontName item)
    //            {
    //              return item.onProperty ();
    //            }
    //          }));
    fontNameListView.setCellFactory (CheckBoxListCell.forListView (FontName::onProperty));

    factory.setWrapAround (true);
    factory.valueProperty ().addListener ( (obs, o, n) -> setTextFont ());

    HBox controlBox = new HBox (10);
    controlBox.setPrefHeight (20);
    controlBox.setPadding (new Insets (6, 10, 6, 10));
    controlBox.setAlignment (Pos.CENTER_LEFT);

    HBox messageBox = new HBox (10);
    messageBox.setPrefHeight (70);
    messageBox.setPadding (new Insets (6, 10, 6, 10));
    messageBox.setAlignment (Pos.CENTER_LEFT);

    Label message = new Label ();
    message.setText ("These are the fixed-width fonts currently on your system. Please"
        + " choose the ones that you wish to be able to quickly switch between."
        + "\nWhen on the main screen use the COMMA and PERIOD keys to"
        + " cycle through the selected fonts, and the LESS-THAN and"
        + " GREATER-THAN \nkeys to alter the font size.");
    messageBox.getChildren ().add (message);

    Button btnApply = getButton ("Apply");
    Button btnCancel = getButton ("Cancel");
    Button btnAccept = getButton ("Accept");

    btnApply.setOnAction (e -> apply ());
    btnCancel.setOnAction (e -> cancel ());
    btnAccept.setOnAction (e -> accept ());

    btnAccept.setDefaultButton (true);
    btnCancel.setCancelButton (true);

    Region filler = new Region ();
    HBox.setHgrow (filler, Priority.ALWAYS);
    controlBox.getChildren ().addAll (new Label ("Font size"), new Spinner<> (factory),
        filler, btnCancel, btnApply, btnAccept);

    borderPane.setLeft (fontNameListView);
    borderPane.setCenter (text);
    borderPane.setBottom (controlBox);
    borderPane.setTop (messageBox);

    stage.addEventHandler (KeyEvent.KEY_RELEASED, (KeyEvent event) ->
    {
      if (KeyCode.ESCAPE == event.getCode ())
        cancel ();
    });

    stage.setScene (new Scene (borderPane, 1000, 700));
  }

  // ---------------------------------------------------------------------------------//
  private static class FontName
  // ---------------------------------------------------------------------------------//
  {
    private final StringProperty name = new SimpleStringProperty ();
    private final BooleanProperty on = new SimpleBooleanProperty ();

    // ---------------------------------------------------------------------------------//
    public FontName (String name)
    // ---------------------------------------------------------------------------------//
    {
      setName (name);
    }

    // ---------------------------------------------------------------------------------//
    public final StringProperty nameProperty ()
    // ---------------------------------------------------------------------------------//
    {
      return this.name;
    }

    // ---------------------------------------------------------------------------------//
    public final String getName ()
    // ---------------------------------------------------------------------------------//
    {
      return this.nameProperty ().get ();
    }

    // ---------------------------------------------------------------------------------//
    public final void setName (final String name)
    // ---------------------------------------------------------------------------------//
    {
      this.nameProperty ().set (name);
    }

    // ---------------------------------------------------------------------------------//
    public final BooleanProperty onProperty ()
    // ---------------------------------------------------------------------------------//
    {
      return this.on;
    }

    // ---------------------------------------------------------------------------------//
    public final boolean isOn ()
    // ---------------------------------------------------------------------------------//
    {
      return this.onProperty ().get ();
    }

    // ---------------------------------------------------------------------------------//
    public final void setOn (final boolean on)
    // ---------------------------------------------------------------------------------//
    {
      this.onProperty ().set (on);
    }

    // ---------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // ---------------------------------------------------------------------------------//
    {
      return getName ();
    }
  }
}
