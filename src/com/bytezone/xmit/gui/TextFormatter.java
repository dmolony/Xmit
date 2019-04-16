package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.Filter.FilterMode;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

// ---------------------------------------------------------------------------------//
class TextFormatter
// ---------------------------------------------------------------------------------//
{
  final List<Text> textList = new ArrayList<> ();
  final Color baseColor = Color.GREEN;
  final Color numberColor = Color.LIGHTSEAGREEN;
  boolean showLines;
  private String filter = "";
  boolean fullFilter;
  FilterMode filterMode;

  // ---------------------------------------------------------------------------------//
  public void setShowLines (boolean showLines)
  // ---------------------------------------------------------------------------------//
  {
    this.showLines = showLines;
  }

  // ---------------------------------------------------------------------------------//
  public boolean getShowLines ()
  // ---------------------------------------------------------------------------------//
  {
    return showLines;
  }

  // ---------------------------------------------------------------------------------//
  public void setFilter (String filter, boolean fullFilter, FilterMode filterMode)
  // ---------------------------------------------------------------------------------//
  {
    this.filter = filter;
    this.fullFilter = fullFilter;
    this.filterMode = filterMode;
  }

  // ---------------------------------------------------------------------------------//
  public String getFilter ()
  // ---------------------------------------------------------------------------------//
  {
    return filter;
  }

  // ---------------------------------------------------------------------------------//
  public boolean usingFilter ()
  // ---------------------------------------------------------------------------------//
  {
    return !filter.isEmpty ();
  }

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

    if (filter.isEmpty () || filterMode != FilterMode.ON)
      plainFormat (lines);
    else
      filterFormat (lines);

    return textList;
  }

  // ---------------------------------------------------------------------------------//
  void plainFormat (List<String> lines)
  // ---------------------------------------------------------------------------------//
  {
    int lineNo = 1;
    for (String line : lines)
    {
      if (showLines)
        addText (String.format ("%06d ", lineNo++), numberColor);

      addTextNewLine (line, baseColor);
    }
    removeLastNewLine ();
  }

  // ---------------------------------------------------------------------------------//
  void filterFormat (List<String> lines)
  // ---------------------------------------------------------------------------------//
  {
    int lineNo = 1;
    for (String line : lines)
    {
      if (showLines)
        addText (String.format ("%06d ", lineNo++), numberColor);

      if (highlight (line, filter, Color.RED))
        continue;

      if (fullFilter)
      {
        if (showLines)
          textList.remove (textList.size () - 1);
      }
      else
        addTextNewLine (line, baseColor);
    }
    removeLastNewLine ();
  }

  // ---------------------------------------------------------------------------------//
  boolean highlight (String line, String text, Color color)
  // ---------------------------------------------------------------------------------//
  {
    int pos = line.indexOf (text);
    if (pos < 0)
      return false;

    addText (line.substring (0, pos), baseColor);
    int pos2 = pos + text.length ();
    if (pos2 < line.length ())
    {
      addText (line.substring (pos, pos2), color);
      addTextNewLine (line.substring (pos2), baseColor);
    }
    else
      addTextNewLine (line.substring (pos), color);

    return true;
  }

  // ---------------------------------------------------------------------------------//
  boolean highlightAfter (String line, String text, Color color)
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

    if (pos2 < 0)
      addTextNewLine (line.substring (pos), color);
    else
    {
      addText (line.substring (pos, pos2), color);
      addTextNewLine (line.substring (pos2), baseColor);
    }

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
