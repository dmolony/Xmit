package com.bytezone.xmit.gui;

import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

// -----------------------------------------------------------------------------------//
class TextFormatterHex extends TextFormatter
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
      //      String hex1 = line.substring (6, 20);
      //      String hex2 = line.substring (20, 32);
      //      String hex3 = line.substring (32, 44);
      //      String hex4 = line.substring (44, 55);
      String text = line.substring (55);

      addText (offset, Color.GREEN);
      addText (hex, Color.BROWN);
      //      addText (hex1, Color.BROWN);
      //      addText (hex2, Color.BROWN);
      //      addText (hex3, Color.BROWN);
      //      addText (hex4, Color.BROWN);
      addTextNewLine (text, Color.GREEN);
    }

    removeLastNewLine ();

    return textList;
  }
}
