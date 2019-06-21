package com.bytezone.xmit.textunit;

// -----------------------------------------------------------------------------------//
public class DatasetType extends TextUnitNumber
// -----------------------------------------------------------------------------------//
{
/*
 * X'80' Data library
 * X'40' Program library
 * X'04' Extended format sequential data set
 * X'01' Large format sequential data set
 */

  String type;

  // ---------------------------------------------------------------------------------//
  public DatasetType (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    super (buffer, ptr);

    type = "";
    if ((number & 0x80) != 0)
      type = "Data library";
    else if ((number & 0x40) != 0)
      type = "Program library";
    if ((number & 0x04) != 0)
      type = type + " - Extended format sequential data set";
    else if ((number & 0x01) != 0)
      type = type + " - Large format sequential data set";
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
    return type.isEmpty () ? super.toString () : String.format ("%04X  %-8s  %02X  %s",
        keys[keyId], mnemonics[keyId], number, type);
  }
}
