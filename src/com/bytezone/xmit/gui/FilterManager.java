package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.xmit.Filter.FilterMode;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// ---------------------------------------------------------------------------------//
public class FilterManager implements SaveState
//---------------------------------------------------------------------------------//
{
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private static final String PREFS_FILTER = "Filter";
  private static final String PREFS_FILTER_EXC = "FilterExc";

  private final List<FilterChangeListener> listeners = new ArrayList<> ();
  private Stage stage;
  private final TextField filterTextField = new TextField ();
  private final CheckBox filterCheckBox = new CheckBox ();

  private String filterValue;
  private String savedFilterValue;
  private boolean filterExclusion;
  private boolean savedFilterExclusion;
  private FilterMode filterMode;
  private FilterMode savedFilterMode;

  //---------------------------------------------------------------------------------//
  void showWindow ()
  //---------------------------------------------------------------------------------//
  {
    if (stage == null)
      buildStage ();

    savedFilterValue = filterValue;
    savedFilterExclusion = filterExclusion;
    savedFilterMode = filterMode;

    filterTextField.setText (filterValue);
    filterTextField.requestFocus ();
    filterTextField.selectAll ();
    filterCheckBox.setSelected (filterExclusion);

    stage.show ();
    stage.toFront ();
  }

  // ---------------------------------------------------------------------------------//
  private void apply ()
  // ---------------------------------------------------------------------------------//
  {
    if (!filterValue.equals (filterTextField.getText ())
        || filterExclusion != filterCheckBox.isSelected ())
    {
      filterValue = filterTextField.getText ();
      filterExclusion = filterCheckBox.isSelected ();

      notifyListeners ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private void cancel ()
  // ---------------------------------------------------------------------------------//
  {
    if (!filterValue.equals (savedFilterValue) || filterExclusion != savedFilterExclusion
        || filterMode != savedFilterMode)
    {
      filterValue = savedFilterValue;
      filterExclusion = savedFilterExclusion;
      filterMode = savedFilterMode;

      notifyListeners ();
    }
    stage.hide ();
  }

  // ---------------------------------------------------------------------------------//
  private void accept ()
  // ---------------------------------------------------------------------------------//
  {
    apply ();
    stage.hide ();
  }

  // ---------------------------------------------------------------------------------//
  private void remove ()
  // ---------------------------------------------------------------------------------//
  {
    savedFilterValue = "";
    savedFilterExclusion = false;
    savedFilterMode = FilterMode.NONE;

    cancel ();
  }

  // ---------------------------------------------------------------------------------//
  private Button getButton (String text)
  // ---------------------------------------------------------------------------------//
  {
    Button button = new Button (text);
    button.setMinWidth (100);

    return button;
  }

  //---------------------------------------------------------------------------------//
  @Override
  public void save ()
  //---------------------------------------------------------------------------------//
  {
    prefs.put (PREFS_FILTER, filterValue);
    prefs.putBoolean (PREFS_FILTER_EXC, filterExclusion);
  }

  //---------------------------------------------------------------------------------//
  @Override
  public void restore ()
  //---------------------------------------------------------------------------------//
  {
    filterValue = prefs.get (PREFS_FILTER, "");
    filterExclusion = prefs.getBoolean (PREFS_FILTER_EXC, false);

    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  void toggleFilterExclusion ()
  // ---------------------------------------------------------------------------------//
  {
    filterExclusion = !filterExclusion;
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  void cycleFilterMode ()
  // ---------------------------------------------------------------------------------//
  {
    if (filterMode == FilterMode.POSITIVE)
      filterMode = FilterMode.NEGATIVE;
    else if (filterMode == FilterMode.NEGATIVE)
      filterMode = FilterMode.NONE;
    else
      filterMode = FilterMode.POSITIVE;
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  private void notifyListeners ()
  // ---------------------------------------------------------------------------------//
  {
    for (FilterChangeListener listener : listeners)
      listener.setFilter (filterValue, filterExclusion, filterMode);
  }

  // ---------------------------------------------------------------------------------//
  public void addFilterListener (FilterChangeListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  private void buildStage ()
  // ---------------------------------------------------------------------------------//
  {
    stage = new Stage ();
    stage.setTitle ("Filter Manager");

    BorderPane borderPane = new BorderPane ();
    Label lblText = new Label ("Filter text");
    Label lblExclusive = new Label ("Exclusive");
    filterTextField.setPrefWidth (300);

    Button btnApply = getButton ("Apply");
    Button btnCancel = getButton ("Cancel");
    Button btnAccept = getButton ("Accept");
    Button btnRemove = getButton ("Remove");

    HBox textBox1 = new HBox (10);
    textBox1.setPrefHeight (30);
    textBox1.setPadding (new Insets (6, 10, 6, 20));
    textBox1.setAlignment (Pos.CENTER_LEFT);
    textBox1.getChildren ().addAll (lblText, filterTextField);

    HBox textBox2 = new HBox (10);
    textBox2.setPrefHeight (30);
    textBox2.setPadding (new Insets (6, 10, 6, 20));
    textBox2.setAlignment (Pos.CENTER_LEFT);
    textBox2.getChildren ().addAll (lblExclusive, filterCheckBox);

    VBox vBox = new VBox (10);
    vBox.setPrefHeight (100);
    vBox.setPadding (new Insets (6, 10, 6, 10));
    vBox.setAlignment (Pos.CENTER_LEFT);
    vBox.getChildren ().addAll (textBox1, textBox2);

    HBox controlBox = new HBox (10);
    controlBox.setPrefHeight (20);
    controlBox.setPadding (new Insets (6, 10, 6, 10));
    controlBox.setAlignment (Pos.CENTER_LEFT);
    Region filler = new Region ();
    HBox.setHgrow (filler, Priority.ALWAYS);
    controlBox.getChildren ().addAll (filler, btnCancel, btnApply, btnAccept, btnRemove);

    borderPane.setBottom (controlBox);
    borderPane.setCenter (vBox);

    btnApply.setOnAction (e -> apply ());
    btnCancel.setOnAction (e -> cancel ());
    btnAccept.setOnAction (e -> accept ());
    btnRemove.setOnAction (e -> remove ());

    btnAccept.setDefaultButton (true);
    btnCancel.setCancelButton (true);

    stage.setScene (new Scene (borderPane, 500, 140));
  }
}
