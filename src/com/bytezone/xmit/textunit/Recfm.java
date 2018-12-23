package com.bytezone.xmit.textunit;

public class Recfm extends TextUnitNumber
{
  // seen: 0001, 4802, 9000

  /*
   * X'0001' Shortened VBS format used for transmission records
   * X'xx02' Varying length records without the 4-byte header
   * X'0200' Data includes machine code printer control characters
   * X'0400' Data contains ASA printer control characters
   * X'0800' Standard fixed records or spanned variable records
   * X'1000' Blocked records
   * X'2000' Track overflow or variable ASCII records
   * X'4000' Variable-length records
   * X'8000' Fixed-length records
   * X'C000' Undefined records
   */

  String type;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Recfm (byte[] buffer, int ptr)
  {
    super (buffer, ptr);

    type = "?";

    if (number == 0xC000)
      type = "Undefined";
    else
    {
      if ((number & 0x8000) != 0)
        type = "F";
      if ((number & 0x4000) != 0)
        type = "V";
      if ((number & 0x1000) != 0)
        type = type + "B";
      if ((number & 0x0400) != 0)
        type = type + "A";
      if ((number & 0x0800) != 0)
        type = type + " standard/spanned";
      if ((number & 0x002) != 0)
        type = type + " (no 4-byte header)";
      if ((number & 0x001) != 0)
        type = "Shortened VBS format used for transmission records";
    }
  }

  // ---------------------------------------------------------------------------------//
  // getString
  // ---------------------------------------------------------------------------------//

  @Override
  public String getString ()
  {
    return type;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return type.isEmpty () ? super.toString () : String.format ("%04X  %-8s  %04X  %s",
        keys[keyId], mnemonics[keyId], number, type);
  }
}
