package com.bytezone.xmit;

public class CopyR2
{
  private final byte[] buffer;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CopyR2 (byte[] buffer)
  {
    this.buffer = buffer;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    int max = (buffer[0] & 0xFF) + 1;
    for (int i = 0; i < max; i++)
      text.append (String.format ("%s%n", Utility.getHexValues (buffer, i * 16, 16)));

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}
