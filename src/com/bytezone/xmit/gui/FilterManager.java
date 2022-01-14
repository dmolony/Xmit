package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.appbase.SaveState;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// -----------------------------------------------------------------------------------//
class FilterManager implements SaveState
// -----------------------------------------------------------------------------------//
{
  private Stage stage;

  private final TextField filterTextField = new TextField ();
  private final CheckBox filterExclusionCheckBox = new CheckBox ();
  private final CheckBox filterReverseCheckBox = new CheckBox ();

  private final FilterStatus filterStatus = new FilterStatus ();
  private final FilterStatus savedFilterStatus = new FilterStatus ();
  private final List<FilterChangeListener> listeners = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  void showWindow ()
  // ---------------------------------------------------------------------------------//
  {
    if (stage == null)
      buildStage ();

    savedFilterStatus.copy (filterStatus);

    filterTextField.setText (filterStatus.filterValue);
    filterTextField.requestFocus ();
    filterTextField.selectAll ();

    filterExclusionCheckBox.setSelected (filterStatus.filterExclusion);
    filterReverseCheckBox.setSelected (filterStatus.filterReverse);

    stage.show ();
    stage.toFront ();
  }

  // ---------------------------------------------------------------------------------//
  private void apply ()
  // ---------------------------------------------------------------------------------//
  {
    if (!filterStatus.filterValue.equals (filterTextField.getText ())
        || filterStatus.filterExclusion != filterExclusionCheckBox.isSelected ()
        || filterStatus.filterReverse != filterReverseCheckBox.isSelected ())
    {
      filterStatus.set (                            //
          filterTextField.getText (),               //
          filterExclusionCheckBox.isSelected (),    //
          filterReverseCheckBox.isSelected (),      //
          !filterTextField.getText ().isEmpty ());

      notifyListeners ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private void cancel ()
  // ---------------------------------------------------------------------------------//
  {
    if (!filterStatus.matches (savedFilterStatus))
    {
      filterStatus.copy (savedFilterStatus);
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
    savedFilterStatus.reset ();
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

  // ---------------------------------------------------------------------------------//
  @Override
  public void save (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    filterStatus.save (prefs);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    filterStatus.restore (prefs);
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  void toggleFilterExclusion ()
  // ---------------------------------------------------------------------------------//
  {
    filterStatus.filterExclusion = !filterStatus.filterExclusion;
    notifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  void keyPressed (KeyEvent keyEvent)
  // ---------------------------------------------------------------------------------//
  {
    if (keyEvent.getCode () == KeyCode.F && !keyEvent.isMetaDown ())
    {
      if (keyEvent.isShiftDown ())
        filterStatus.filterReverse = !filterStatus.filterReverse;
      else
        filterStatus.filterActive = !filterStatus.filterActive;
      notifyListeners ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private void notifyListeners ()
  // ---------------------------------------------------------------------------------//
  {
    FilterStatus copy = new FilterStatus (filterStatus);
    for (FilterChangeListener listener : listeners)
      listener.setFilter (copy);
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
    BorderPane borderPane = new BorderPane ();
    Label lblText = new Label ("Filter text");
    Label lblExclusive = new Label ("Exclusive");
    Label lblReverse = new Label ("Reverse");

    filterTextField.setPrefWidth (300);
    lblText.setPrefWidth (60);
    lblExclusive.setPrefWidth (60);
    lblReverse.setPrefWidth (60);

    Button btnApply = getButton ("Apply");
    Button btnCancel = getButton ("Cancel");
    Button btnAccept = getButton ("Accept");
    Button btnRemove = getButton ("Remove");

    HBox textBox1 = new HBox (10);
    textBox1.setPrefHeight (30);
    textBox1.setPadding (new Insets (6, 10, 6, 10));
    textBox1.setAlignment (Pos.CENTER_LEFT);
    textBox1.getChildren ().addAll (lblText, filterTextField);

    HBox textBox2 = new HBox (10);
    textBox2.setPrefHeight (20);
    textBox2.setPadding (new Insets (6, 10, 6, 10));
    textBox2.setAlignment (Pos.CENTER_LEFT);
    textBox2.getChildren ().addAll (lblExclusive, filterExclusionCheckBox);

    HBox textBox3 = new HBox (10);
    textBox3.setPrefHeight (20);
    textBox3.setPadding (new Insets (6, 10, 6, 10));
    textBox3.setAlignment (Pos.CENTER_LEFT);
    textBox3.getChildren ().addAll (lblReverse, filterReverseCheckBox);

    VBox vBox = new VBox ();
    vBox.setPadding (new Insets (6, 10, 6, 10));
    vBox.getChildren ().addAll (textBox1, textBox2, textBox3);

    HBox controlBox = new HBox (10);
    controlBox.setPrefHeight (20);
    controlBox.setPadding (new Insets (6, 10, 6, 10));
    controlBox.setAlignment (Pos.CENTER_RIGHT);
    controlBox.getChildren ().addAll (btnCancel, btnApply, btnAccept, btnRemove);

    borderPane.setCenter (vBox);
    borderPane.setBottom (controlBox);

    btnApply.setOnAction (e -> apply ());
    btnCancel.setOnAction (e -> cancel ());
    btnAccept.setOnAction (e -> accept ());
    btnRemove.setOnAction (e -> remove ());

    btnAccept.setDefaultButton (true);
    btnCancel.setCancelButton (true);

    stage = new Stage ();
    stage.setTitle ("Filter Manager");

    stage.setScene (new Scene (borderPane));
    stage.sizeToScene ();
    stage.setResizable (false);
  }
}
