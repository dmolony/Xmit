package com.bytezone.xmit.textunit;

// -----------------------------------------------------------------------------------//
public class Recfm extends TextUnitNumber
// -----------------------------------------------------------------------------------//
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
  private static final char[] types1 = { '?', 'V', 'F', 'U' };
  private static final char[] types2 = { '?', 'B', '.', '?' };
  private static final char[] types3 = { '?', 'A', 'A', '?', 'S', '?', '?', '?' };

  // recfm=F/V/D/U (fixed, variable, ascii variable, undefined)

  public String type;

  // ---------------------------------------------------------------------------------//
  public Recfm (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    super (buffer, ptr);

    type = "?";

    if (number == 0xC000)
      type = "U";
    else
    {
      if ((number & 0x8000) != 0)
        type = "F";
      else if ((number & 0x4000) != 0)
        type = "V";

      if ((number & 0x1000) != 0)
        type += "B";

      if ((number & 0x0400) != 0 || (number & 0x0200) != 0)
        type += "A";

      if ((number & 0x0800) != 0)
      {
        if (type.startsWith ("V"))
          type += "S";                    // spanned
        else if (type.startsWith ("F"))
          type += " last block may be short";
      }

      if ((number & 0x0002) != 0)
        type += " (no 4-byte header)";

      if ((number & 0x0001) != 0)
        type = "Shortened VBS format used for transmission records";
    }

    if (false)
    {
      int t1 = (int) ((number & 0xC000) >>> 14);
      int t2 = (int) ((number & 0x3000) >>> 12);
      int t3 = (int) ((number & 0x0E00) >>> 9);
      System.out.printf ("%04X  %s  %s  %s%n", number, types1[t1], types2[t2],
          types3[t3]);
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getString ()
  // ---------------------------------------------------------------------------------//
  {
    return type;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return type.isEmpty () ? super.toString () : String.format ("%04X  %-8s  %04X  %s",
        keys[keyId], mnemonics[keyId], number, type);
  }
}
