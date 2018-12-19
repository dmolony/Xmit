package com.bytezone.xmit;

import java.io.UnsupportedEncodingException;

public class Utility
{
  public static final int[] ebc2asc = new int[256];
  public static final int[] asc2ebc = new int[256];

  static
  {
    byte[] values = new byte[256];
    for (int i = 0; i < 256; i++)
      values[i] = (byte) i;

    try
    {
      String s = new String (values, "CP037");      // CP500, CP1047, CP037, CP285
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

  // ---------------------------------------------------------------------------------//
  // getTwoBytes
  // ---------------------------------------------------------------------------------//

  public static int getTwoBytes (byte[] buffer, int ptr)
  {
    int a = (buffer[ptr] & 0xFF) << 8;
    int b = (buffer[ptr + 1] & 0xFF);
    return a + b;
  }

  // ---------------------------------------------------------------------------------//
  // getFourBytes
  // ---------------------------------------------------------------------------------//

  static long getFourBytes (byte[] buffer, int ptr)
  {
    long a = getTwoBytes (buffer, ptr) << 16;
    long b = getTwoBytes (buffer, ptr + 2);
    return a + b;
  }

  // ---------------------------------------------------------------------------------//
  // getValue
  // ---------------------------------------------------------------------------------//

  public static long getValue (byte[] buffer, int offset, int length)
  {
    long value = 0;
    while (length-- > 0)
      value = value << 8 | buffer[offset++] & 0xFF;
    return value;
  }

  // ---------------------------------------------------------------------------------//
  // getHexValues
  // ---------------------------------------------------------------------------------//

  public static String getHexValues (byte[] buffer)
  {
    return getHexValues (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  // getHexValues
  // ---------------------------------------------------------------------------------//

  public static String getHexValues (byte[] buffer, int offset, int length)
  {
    StringBuilder text = new StringBuilder ();
    for (int i = offset, max = offset + length; i < max; i++)
      text.append (String.format ("%02X ", buffer[i]));
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // getHexDump
  // ---------------------------------------------------------------------------------//

  public static String getHexDump (byte[] b)
  {
    return getHexDump (b, 0, b.length);
  }

  // ---------------------------------------------------------------------------------//
  // getHexDump
  // ---------------------------------------------------------------------------------//

  public static String getHexDump (byte[] b, int displayOffset)
  {
    return getHexDump (b, 0, b.length, displayOffset);
  }

  // ---------------------------------------------------------------------------------//
  // getHexDump
  // ---------------------------------------------------------------------------------//

  public static String getHexDump (byte[] b, int offset, int length)
  {
    return getHexDump (b, offset, length, 0);
  }

  // ---------------------------------------------------------------------------------//
  // getHexDump
  // ---------------------------------------------------------------------------------//

  public static String getHexDump (byte[] b, int offset, int length, int displayOffset)
  {
    final int lineSize = 16;
    StringBuilder text = new StringBuilder ();

    for (int ptr = offset, max = offset + length; ptr < max; ptr += lineSize)
    {
      final StringBuilder hexLine = new StringBuilder ();
      final StringBuilder textLine = new StringBuilder ();

      for (int linePtr = 0; linePtr < lineSize; linePtr++)
      {
        int z = ptr + linePtr;
        if (z >= max)
          break;

        hexLine.append (String.format ("%02X ", b[z]));

        final int val = b[z] & 0xFF;
        textLine.append (val <= 0x3F || val == 0xFF ? '.' : (char) ebc2asc[val]);
      }
      text.append (String.format ("%06X  %-48s %s%n", displayOffset + ptr,
          hexLine.toString (), textLine.toString ()));
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // printStackTrace
  // ---------------------------------------------------------------------------------//

  public static void printStackTrace ()
  {
    for (StackTraceElement ste : Thread.currentThread ().getStackTrace ())
      System.out.println (ste);
  }
}