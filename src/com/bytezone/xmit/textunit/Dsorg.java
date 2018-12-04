package com.bytezone.xmit.textunit;

public class Dsorg extends TextUnit
{
  String type;

  public Dsorg (byte[] buffer, int ptr)
  {
    super (buffer, ptr);

    type = getType (dataList.get (0).data[0]);
  }

  private String getType (byte code)
  {
    int recfm = code & 0xD6;      // ???
    //    System.out.printf ("RECFM: %02X%n", code);
    switch (code)
    {
      case 0x40:
        return "V";
      case 0x42:
        return "VM";
      case 0x44:
        return "VA";
      case 0x50:
        return "VB";
      case 0x52:
        return "VBM";
      case 0x54:
        return "VBA";
      case (byte) 0x80:
        return "F";
      case (byte) 0x82:
        return "FM";
      case (byte) 0x84:
        return "FA";
      case (byte) 0x90:
        return "FB";
      case (byte) 0x92:
        return "FBM";
      case (byte) 0x94:
        return "FBA";
      case (byte) 0xC0:
        return "U";
      default:
        return "Unknown";
    }
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%-8s  %s", mnemonics[keyId], type);
  }
}
