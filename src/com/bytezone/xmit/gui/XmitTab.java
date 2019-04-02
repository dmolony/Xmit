package com.bytezone.xmit.gui;

import java.util.List;

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
abstract class XmitTab
//---------------------------------------------------------------------------------//
{
  final Tab tab;
  private final TextFlow textFlow;
  final KeyCode keyCode;
  //  final Supplier<List<String>> textSupplier;
  final ScrollPane scrollPane;
  OutputPane parent;

  private Font font;
  private final TextFormatter textFormatter = new TextFormatter ();
  private final TextFormatter textFormatterJcl = new TextFormatterJcl ();

  //  Dataset dataset;                // usually file #1 in the Reader
  //  DataFile dataFile;              // FlatFile or PdsMember

  // ---------------------------------------------------------------------------------//
  public XmitTab (OutputPane parent, String title, KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    this.keyCode = keyCode;
    this.parent = parent;
    //    this.textSupplier = textSupplier;

    textFlow = new TextFlow ();
    textFlow.setLineSpacing (1);

    scrollPane = new ScrollPane (textFlow);
    scrollPane.setPadding (new Insets (5, 5, 5, 5));
    scrollPane.setStyle ("-fx-background: white;-fx-border-color: lightgray;");

    tab = new Tab (title, scrollPane);
    tab.setUserData (this);
  }

  // ---------------------------------------------------------------------------------//
  abstract List<String> getLines ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  void clear ()
  // ---------------------------------------------------------------------------------//
  {
    textFlow.getChildren ().clear ();
  }

  // ---------------------------------------------------------------------------------//
  void update ()
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = getLines ();

    List<Text> textList = null;
    if (Utility.isJCL (lines))
      textList = textFormatterJcl.format (lines);
    else
      textList = textFormatter.format (lines);

    for (Text text : textList)
      text.setFont (font);

    textFlow.getChildren ().setAll (textList);

    scrollPane.setVvalue (0);
    scrollPane.setHvalue (0);
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