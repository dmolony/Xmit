package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.*;

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

    //    System.out.printf ("%s", name);

    if ("INMR02".equals (name))
    {
      int fileNbr = Reader.getDoubleWord (buffer, ptr);     // need to save this
      //      System.out.printf (" (file %d)", fileNbr);
      ptr += 4;
    }

    //    System.out.println ();

    while (ptr < max)
    {
      TextUnit textUnit = createTextUnit (buffer, ptr);
      textUnits.add (textUnit);
      //      System.out.println ("   " + textUnit);
      ptr += 4 + textUnit.length ();
    }
    System.out.println ();
  }

  private TextUnit createTextUnit (byte[] buffer, int ptr)
  {
    int key = Reader.getWord (buffer, ptr);
    switch (key)
    {
      case TextUnit.INMDSNAM:
        return new Dsnam (buffer, ptr);

      case TextUnit.INMUTILN:
      case TextUnit.INMFNODE:
      case TextUnit.INMFUID:
      case TextUnit.INMTNODE:
      case TextUnit.INMTUID:
        return new TextUnitString (buffer, ptr);

      case TextUnit.INMNUMF:
      case TextUnit.INMLRECL:
      case TextUnit.INMBLKSZ:
      case TextUnit.INMSIZE:
      case TextUnit.INMDIR:
        return new TextUnitNumber (buffer, ptr);

      case TextUnit.INMFTIME:
        return new TextUnitTime (buffer, ptr);

      //      case TextUnit.INMRECFM:
      //        return new Recfm (buffer, ptr);

      case TextUnit.INMDSORG:
        return new Dsorg (buffer, ptr);

      default:
        return new TextUnit (buffer, ptr);
    }
  }

  // ---------------------------------------------------------------------------------//
  // getTextUnit
  // ---------------------------------------------------------------------------------//

  TextUnit getTextUnit (int keyId)
  {
    for (TextUnit textUnit : textUnits)
      if (textUnit.matches (keyId))
        return textUnit;

    return null;
  }
}
