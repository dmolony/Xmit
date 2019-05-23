package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class CopyR2
// -----------------------------------------------------------------------------------//
{
  final byte[] buffer;

  // ---------------------------------------------------------------------------------//
  CopyR2 (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    this.buffer = buffer;
  }

  // ---------------------------------------------------------------------------------//
  public List<String> toLines ()
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = new ArrayList<> ();

    int max = (buffer[0] & 0xFF) + 1;
    lines.add ("-----------------------------------------------------------");
    for (int i = 0; i < max; i++)
      lines.add (Utility.getHexValues (buffer, i * 16, 16));
    lines.add ("-----------------------------------------------------------");

    return lines;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    for (String line : toLines ())
    {
      text.append (line);
      text.append ("\n");
    }

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}
