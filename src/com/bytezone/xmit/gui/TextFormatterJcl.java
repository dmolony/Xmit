package com.bytezone.xmit.gui;

import java.util.List;

import com.bytezone.xmit.Utility;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

// ---------------------------------------------------------------------------------//
public class TextFormatterJcl extends TextFormatter
// ---------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  @Override
  public List<Text> format (List<String> lines)
  // ---------------------------------------------------------------------------------//
  {
    textList.clear ();

    if (!Utility.isJCL (lines))
      return super.format (lines);

    for (String line : lines)
    {
      if (line.length () > 72)
        line = line.substring (0, 72);            // remove line numbers

      if (line.startsWith ("//*"))                // line comment
      {
        addTextNewLine (line, Color.GRAY);
        continue;
      }

      if (line.matches ("^[^/].*?"))              // non-JCL (instream data)
      {
        addTextNewLine (line, Color.CHOCOLATE);
        continue;
      }

      if (highlight (line, "DSN=", Color.RED))
        continue;

      if (highlight (line, "PGM=", Color.BLUE))
        continue;

      addTextNewLine (line, baseColor);
    }

    removeLastNewLine ();

    return textList;
  }
}
