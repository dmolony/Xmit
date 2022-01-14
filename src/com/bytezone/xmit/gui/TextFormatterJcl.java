package com.bytezone.xmit.gui;

import java.util.List;

import com.bytezone.xmit.Utility;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

// -----------------------------------------------------------------------------------//
class TextFormatterJcl extends XmitTextFormatter
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  @Override
  public List<Text> format (List<String> lines)
  // ---------------------------------------------------------------------------------//
  {
    if (usingFilter () || !Utility.isJCL (lines))     // truncation buggers this
      return super.format (lines);

    textList.clear ();

    int lineNo = 1;
    for (String line : lines)
    {
      if (showLineNumbers)
        addText (String.format ("%06d ", lineNo++), numberColor);

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

      if (highlightAfter (line, "DSN=", Color.DEEPPINK))
        continue;

      if (highlightAfter (line, "PGM=", Color.BLUE))
        continue;

      addTextNewLine (line, baseColor);
    }

    removeLastNewLine ();

    return textList;
  }
}
