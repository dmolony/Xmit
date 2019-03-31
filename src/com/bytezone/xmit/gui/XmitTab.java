package com.bytezone.xmit.gui;

import java.util.function.Supplier;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

// ---------------------------------------------------------------------------------//
class XmitTab
//---------------------------------------------------------------------------------//
{
  final Tab tab;
  //  final TextArea textArea;
  final TextFlow textFlow;
  final KeyCode keyCode;
  //  final ScrollBarState scrollBarState;
  final Supplier<String> textSupplier;
  final ScrollPane scrollPane;
  private Font font;

  // ---------------------------------------------------------------------------------//
  public XmitTab (String title, KeyCode keyCode, Supplier<String> textSupplier)
  // ---------------------------------------------------------------------------------//
  {
    //    this.textArea = textArea;
    this.textFlow = new TextFlow ();
    textFlow.setLineSpacing (1);
    scrollPane = new ScrollPane (textFlow);
    scrollPane.setPadding (new Insets (5, 5, 5, 10));
    scrollPane.setStyle ("-fx-background: white;-fx-border-color: lightgray;");
    this.keyCode = keyCode;
    this.textSupplier = textSupplier;
    //    scrollBarState = new ScrollBarState (textArea, Orientation.VERTICAL);

    tab = new Tab (title, scrollPane);
    tab.setUserData (this);
  }

  // ---------------------------------------------------------------------------------//
  void update ()
  // ---------------------------------------------------------------------------------//
  {
    //    if (textArea.getText ().isEmpty ())
    //    {
    //      textArea.setText (textSupplier.get ());
    textFlow.getChildren ().clear ();
    addText (textSupplier.get (), Color.GREEN);
    //    }
  }

  // ---------------------------------------------------------------------------------//
  private void addText (String line, Color color)
  // ---------------------------------------------------------------------------------//
  {
    Text text = new Text (line);
    text.setFill (color);
    text.setFont (font);
    textFlow.getChildren ().add (text);
  }

  // ---------------------------------------------------------------------------------//
  void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    //    textArea.setFont (font);
    this.font = font;
    for (Node node : textFlow.getChildren ())
      ((Text) node).setFont (font);
  }

  // ---------------------------------------------------------------------------------//
  //  void saveScrollBar ()
  // ---------------------------------------------------------------------------------//
  //  {
  //    scrollBarState.save ();
  //  }

  // ---------------------------------------------------------------------------------//
  //  void restoreScrollBar ()
  // ---------------------------------------------------------------------------------//
  //  {
  //    scrollBarState.restore ();
  //  }
}