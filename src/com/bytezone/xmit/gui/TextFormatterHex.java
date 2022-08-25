package com.bytezone.xmit.gui;

import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

// -----------------------------------------------------------------------------------//
class TextFormatterHex extends XmitTextFormatter
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  @Override
  public List<Text> format (List<String> lines)
  // ---------------------------------------------------------------------------------//
  {
    textList.clear ();

    for (String line : lines)
    {
      String offset = line.substring (0, 6);
      String hex = line.substring (6, 55);
      String text = line.substring (55);

      addText (offset, Color.GREEN);
      addText (hex, Color.BROWN);
      addTextNewLine (text, Color.GREEN);
    }

    removeLastNewLine ();

    return textList;
  }
}
