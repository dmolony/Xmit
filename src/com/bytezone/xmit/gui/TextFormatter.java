package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

// ---------------------------------------------------------------------------------//
public class TextFormatter
// ---------------------------------------------------------------------------------//
{
  List<Text> textList = new ArrayList<> ();
  Color baseColor = Color.GREEN;

  // ---------------------------------------------------------------------------------//
  public List<Text> format (String line)
  // ---------------------------------------------------------------------------------//
  {
    textList.clear ();

    addTextNewLine (line, baseColor);

    return textList;
  }

  // ---------------------------------------------------------------------------------//
  public List<Text> format (List<String> lines)
  // ---------------------------------------------------------------------------------//
  {
    textList.clear ();

    for (String line : lines)
      addTextNewLine (line, baseColor);
    removeLastNewLine ();

    return textList;
  }

  // ---------------------------------------------------------------------------------//
  boolean highlight (String line, String text, Color color)
  // ---------------------------------------------------------------------------------//
  {
    int pos = line.indexOf (text);
    if (pos < 0)
      return false;

    pos += text.length ();
    addText (line.substring (0, pos), baseColor);

    int pos2 = line.indexOf (',', pos);
    if (pos2 < 0)
      pos2 = line.indexOf (' ', pos);

    if (pos2 > 0)
    {
      addText (line.substring (pos, pos2), color);
      addTextNewLine (line.substring (pos2), baseColor);
    }
    else
      addTextNewLine (line.substring (pos), color);

    return true;
  }

  // ---------------------------------------------------------------------------------//
  void addTextNewLine (String line, Color color)
  // ---------------------------------------------------------------------------------//
  {
    addText (line, color);
    addText ("\n", color);
  }

  // ---------------------------------------------------------------------------------//
  void addText (String line, Color color)
  // ---------------------------------------------------------------------------------//
  {
    if (line.isEmpty ())
      return;

    Text text = new Text (line);
    text.setFill (color);
    textList.add (text);
  }

  // ---------------------------------------------------------------------------------//
  void removeLastNewLine ()
  // ---------------------------------------------------------------------------------//
  {
    if (textList.size () == 0)
      return;

    Text text = textList.get (textList.size () - 1);
    String line = text.getText ();
    if (line.length () == 1)
      textList.remove (textList.size () - 1);
    else
      text.setText (line.substring (0, line.length () - 1));
  }
}
