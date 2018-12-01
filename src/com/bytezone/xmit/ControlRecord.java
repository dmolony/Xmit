package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class ControlRecord
{
  final String name;
  List<TextUnit> textUnits = new ArrayList<> ();

  public ControlRecord (byte[] buffer, int ptr, int length)
  {
    name = Reader.getString (buffer, ptr, 6);
    int max = ptr + length;
    ptr += 6;

    System.out.printf ("%n%s", name);

    if ("INMR02".equals (name))
    {
      int fileNbr = Reader.getDouble (buffer, ptr);
      System.out.printf (" (file %d)", fileNbr);
      ptr += 4;
    }

    System.out.println ();

    while (ptr < max)
    {
      TextUnit textUnit = new TextUnit (buffer, ptr);
      textUnits.add (textUnit);
      System.out.println ("   " + textUnit);
      ptr += 4 + textUnit.length;
    }
  }
}
