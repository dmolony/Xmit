package com.bytezone.xmit.gui;

import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

// ---------------------------------------------------------------------------------//
abstract class XmitTextTab extends XmitTab
//---------------------------------------------------------------------------------//
{
  private final TextFlow textFlow;
  final ScrollPane scrollPane;

  TextFormatter textFormatter = new TextFormatter ();

  // ---------------------------------------------------------------------------------//
  public XmitTextTab (String title, KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    super (title, keyCode);

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
  @Override
  void clear ()
  // ---------------------------------------------------------------------------------//
  {
    textFlow.getChildren ().clear ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
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
  @Override
  void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    super.setFont (font);

    for (Node node : textFlow.getChildren ())
      ((Text) node).setFont (font);
  }
}