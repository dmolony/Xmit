package com.bytezone.xmit.textunit;

public class Recfm extends TextUnit
{
  public enum RecordFormat
  {
    F, FB, FBA, FS, FBS, FBM, V, VB, VBA, VS, VBS, VBM
  }

  // seen: 0001, 4802, 9000

  String type;

  public Recfm (byte[] buffer, int ptr)
  {
    super (buffer, ptr);
    type = getType (dataList.get (0).data[0] & 0xFF);
  }

  String getType (int code)
  {
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

      case 0x80:
        return "F";
      case 0x82:
        return "FM";
      case 0x84:
        return "FA";
      case 0x90:
        return "FB";
      case 0x92:
        return "FBM";
      case 0x94:
        return "FBA";

      case 0xC0:
        return "U";
      case 0xC2:
        return "UM";
      case 0xC4:
        return "UA";

      default:
        return "";
    }
  }

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
    return type.isEmpty () ? super.toString ()
        : String.format ("%04X  %-8s  %s", keys[keyId], mnemonics[keyId], type);
  }
}
