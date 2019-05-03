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
  private final TextFlow textFlow = new TextFlow ();
  private final ScrollPane scrollPane = new ScrollPane (textFlow);

  TextFormatter textFormatter = new TextFormatter ();

  // ---------------------------------------------------------------------------------//
  public XmitTextTab (String title, KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    super (title, keyCode);

    textFlow.setLineSpacing (1);

    scrollPane.setPadding (new Insets (5, 5, 5, 5));
    scrollPane.setStyle ("-fx-background: white;-fx-border-color: lightgray;");

    setContent (scrollPane);
  }

  // ---------------------------------------------------------------------------------//
  abstract List<String> getLines ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  @Override
  void update ()
  // ---------------------------------------------------------------------------------//
  {
    if (valid)
      return;

    valid = true;

    textFlow.getChildren ().setAll (textFormatter.format (getLines ()));

    scrollPane.setVvalue (0);
    scrollPane.setHvalue (0);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    super.setFont (font);

    textFormatter.setFont (font);

    for (Node node : textFlow.getChildren ())
      ((Text) node).setFont (font);
  }
}