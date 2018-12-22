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

  public ControlRecord (byte[] buffer)
  {
    int ptr = 0;
    name = Reader.getString (buffer, ptr, 6);
    int max = ptr + buffer.length;
    ptr += 6;

    if ("INMR02".equals (name))
    {
      long fileNbr = Utility.getFourBytes (buffer, ptr);     // need to save this
      //      System.out.printf (" (file %d)", fileNbr);
      ptr += 4;
    }

    while (ptr < max)
    {
      TextUnit textUnit = createTextUnit (buffer, ptr);
      if (textUnit == null)
        break;
      textUnits.add (textUnit);
      ptr += 4 + textUnit.length ();
    }
  }

  // ---------------------------------------------------------------------------------//
  // createTextUnit
  // ---------------------------------------------------------------------------------//

  private TextUnit createTextUnit (byte[] buffer, int ptr)
  {
    int key = Utility.getTwoBytes (buffer, ptr);
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

      case TextUnit.INMRECFM:
        return new Recfm (buffer, ptr);

      case TextUnit.INMDSORG:
        return new Dsorg (buffer, ptr);

      case TextUnit.INMTYPE:
      case TextUnit.INMFACK:
        return new TextUnit (buffer, ptr);

      case TextUnit.INMMEMBR:
        return new Member (buffer, ptr);

      default:
        System.out.printf ("Unknown key: %04X%n", key);
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

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Control Record: %s%n%n", name));
    for (TextUnit textUnit : textUnits)
      text.append (textUnit + "\n");

    return text.toString ();
  }
}
