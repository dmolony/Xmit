package com.bytezone.xmit;

import com.bytezone.common.Utility;

class Data
{
  int length;
  byte[] data;
  String text;
  boolean printable = true;

  Data (byte[] buffer, int ptr)
  {
    length = Reader.getWord (buffer, ptr);
    data = new byte[length];
    System.arraycopy (buffer, ptr + 2, data, 0, length);

    for (byte b : data)
      if ((b & 0xFF) <= 0x3F)
      {
        printable = false;
        break;
      }
    text = printable ? Reader.getString (data, 0, length) : "";
  }

  @Override
  public String toString ()
  {
    return String.format ("%04X %s : %s", length, Utility.getHex (data, 0, data.length),
        text);
  }
}
