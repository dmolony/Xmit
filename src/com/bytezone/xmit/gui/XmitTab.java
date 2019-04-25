package com.bytezone.xmit.gui;

import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

// ---------------------------------------------------------------------------------//
abstract class XmitTab extends Tab
//---------------------------------------------------------------------------------//
{
  private final TextFlow textFlow;
  final KeyCode keyCode;
  final ScrollPane scrollPane;
  final OutputPane parent;              // improve this

  private Font font;
  TextFormatter textFormatter = new TextFormatter ();

  // ---------------------------------------------------------------------------------//
  public XmitTab (String title, OutputPane parent, KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    super (title);

    this.keyCode = keyCode;
    this.parent = parent;

    textFlow = new TextFlow ();
    textFlow.setLineSpacing (1);

    scrollPane = new ScrollPane (textFlow);
    scrollPane.setPadding (new Insets (5, 5, 5, 5));
    scrollPane.setStyle ("-fx-background: white;-fx-border-color: lightgray;");

    this.setContent (scrollPane);
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
    if (textFlow.getChildren ().size () > 0)
      return;

    List<Text> textList = textFormatter.format (getLines ());

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