package com.bytezone.xmit;

import java.io.UnsupportedEncodingException;

// https://en.wikipedia.org/wiki/EBCDIC_code_pages

// ---------------------------------------------------------------------------------//
public class CodePage
//---------------------------------------------------------------------------------//
{
  private static byte[] values;
  public final int[] ebc2asc = new int[256];

  static
  {
    values = new byte[256];
    for (int i = 0; i < 256; i++)
      values[i] = (byte) i;
  }

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CodePage (String codePage)
  {
    try
    {
      String s = new String (values, codePage);
      char[] chars = s.toCharArray ();
      for (int i = 0; i < 256; i++)
      {
        int val = chars[i];
        ebc2asc[i] = val;
      }
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
  }
}
