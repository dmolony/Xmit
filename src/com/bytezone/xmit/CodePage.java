package com.bytezone.xmit;

import java.io.UnsupportedEncodingException;

public class CodePage
{
  public final int[] ebc2asc = new int[256];
  public final int[] asc2ebc = new int[256];

  public CodePage (String codePage)
  {
    byte[] values = new byte[256];
    for (int i = 0; i < 256; i++)
      values[i] = (byte) i;

    try
    {
      String s = new String (values, codePage);      // CP500, CP1047, CP037, CP285
      char[] chars = s.toCharArray ();
      for (int i = 0; i < 256; i++)
      {
        int val = chars[i];
        ebc2asc[i] = val;
        asc2ebc[val] = i;
      }
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
  }
}
