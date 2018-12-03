package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class ControlRecord
{
  final String name;
  List<TextUnit> textUnits = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public ControlRecord (byte[] buffer, int ptr, int length)
  {
    name = Reader.getString (buffer, ptr, 6);
    int max = ptr + length;
    ptr += 6;

    System.out.printf ("%s", name);

    if ("INMR02".equals (name))
    {
      int fileNbr = Reader.getDouble (buffer, ptr);
      System.out.printf (" (file %d)", fileNbr);
      ptr += 4;
    }

    System.out.println ();

    while (ptr < max)
    {
      TextUnit textUnit = createTextUnit (buffer, ptr);
      textUnits.add (textUnit);
      System.out.println ("   " + textUnit);
      ptr += 4 + textUnit.length;
    }
    System.out.println ();
  }

  private TextUnit createTextUnit (byte[] buffer, int ptr)
  {
    int key = Reader.getWord (buffer, ptr);
    switch (key)
    {
      case TextUnit.INMDSNAM:
        return new TextUnit (buffer, ptr);
      default:
        return new TextUnit (buffer, ptr);
    }
  }
}
