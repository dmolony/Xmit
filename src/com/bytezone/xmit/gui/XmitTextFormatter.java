package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.appbase.TextFormatter;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

// -----------------------------------------------------------------------------------//
class XmitTextFormatter implements TextFormatter
// -----------------------------------------------------------------------------------//
{
  protected final List<Text> textList = new ArrayList<> ();
  protected final Color baseColor = Color.GREEN;
  protected final Color numberColor = Color.LIGHTSEAGREEN;
  protected boolean showLineNumbers;
  protected Font font;

  private final FilterStatus filterStatus = new FilterStatus ();

  // ---------------------------------------------------------------------------------//
  @Override
  public List<Text> format (String line)
  // ---------------------------------------------------------------------------------//
  {
    textList.clear ();

    addTextNewLine (line, baseColor);

    return textList;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<Text> format (List<String> lines)
  // ---------------------------------------------------------------------------------//
  {
    textList.clear ();

    if (filterStatus.filterValue.isEmpty () || !filterStatus.filterActive)
      plainFormat (lines);
    else
      filterFormat (lines);

    return textList;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    this.font = font;
  }

  // ---------------------------------------------------------------------------------//
  public void setShowLineNumbers (boolean showLineNumbers)
  // ---------------------------------------------------------------------------------//
  {
    this.showLineNumbers = showLineNumbers;
  }

  // ---------------------------------------------------------------------------------//
  public boolean getShowLineNumbers ()
  // ---------------------------------------------------------------------------------//
  {
    return showLineNumbers;
  }

  // ---------------------------------------------------------------------------------//
  public void setFilter (FilterStatus filterStatus)
  // ---------------------------------------------------------------------------------//
  {
    this.filterStatus.copy (filterStatus);
  }

  // ---------------------------------------------------------------------------------//
  public boolean usingFilter ()
  // ---------------------------------------------------------------------------------//
  {
    return !filterStatus.filterValue.isEmpty () && filterStatus.filterActive;
  }

  // ---------------------------------------------------------------------------------//
  void plainFormat (List<String> lines)
  // ---------------------------------------------------------------------------------//
  {
    int lineNo = 1;
    for (String line : lines)
    {
      if (showLineNumbers)
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
      if (showLineNumbers)
        addText (String.format ("%06d ", lineNo++), numberColor);

      if (highlight (line, filterStatus.filterValue, Color.RED))
        continue;

      if (filterStatus.filterExclusion)
      {
        if (showLineNumbers)
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
    text.setFont (font);
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
