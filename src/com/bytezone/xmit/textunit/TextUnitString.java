package com.bytezone.xmit.textunit;

public class TextUnitString extends TextUnit
{
  String text;

  public TextUnitString (byte[] buffer, int ptr)
  {
    super (buffer, ptr);

    text = dataList.get (0).text;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%-8s  %s", mnemonics[keyId], text);
  }
}
