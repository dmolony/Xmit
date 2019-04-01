package com.bytezone.xmit.gui;

import java.util.List;
import java.util.function.Supplier;

import com.bytezone.xmit.Utility;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

// ---------------------------------------------------------------------------------//
class XmitTab
//---------------------------------------------------------------------------------//
{
  final Tab tab;
  final TextFlow textFlow;
  final KeyCode keyCode;
  final Supplier<List<String>> textSupplier;
  final ScrollPane scrollPane;
  private Font font;
  private final TextFormatter textFormatter = new TextFormatter ();
  private final TextFormatter textFormatterJcl = new TextFormatterJcl ();

  // ---------------------------------------------------------------------------------//
  public XmitTab (String title, KeyCode keyCode, Supplier<List<String>> textSupplier)
  // ---------------------------------------------------------------------------------//
  {
    this.keyCode = keyCode;
    this.textSupplier = textSupplier;

    textFlow = new TextFlow ();
    textFlow.setLineSpacing (1);

    scrollPane = new ScrollPane (textFlow);
    scrollPane.setPadding (new Insets (5, 5, 5, 5));
    scrollPane.setStyle ("-fx-background: white;-fx-border-color: lightgray;");

    tab = new Tab (title, scrollPane);
    tab.setUserData (this);
  }

  // ---------------------------------------------------------------------------------//
  void update ()
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = textSupplier.get ();
    List<Text> textList = null;
    if (Utility.isJCL (lines))
      textList = textFormatterJcl.format (lines);
    else
      textList = textFormatter.format (lines);
    for (Text text : textList)
      text.setFont (font);
    textFlow.getChildren ().clear ();
    textFlow.getChildren ().addAll (textList);
  }

  // ---------------------------------------------------------------------------------//
  void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    this.font = font;
    for (Node node : textFlow.getChildren ())
      ((Text) node).setFont (font);
  }
}