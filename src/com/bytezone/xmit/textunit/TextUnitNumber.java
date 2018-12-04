package com.bytezone.xmit.textunit;

public class TextUnitNumber extends TextUnit
{
  long number;

  public TextUnitNumber (byte[] buffer, int ptr)
  {
    super (buffer, ptr);

    Data data = dataList.get (0);
    number = 0;
    for (int i = 0; i < data.length; i++)
    {
      number *= 256;
      number += (data.data[i] & 0xFF);
    }
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%-8s  %,d", mnemonics[keyId], number);
  }
}
