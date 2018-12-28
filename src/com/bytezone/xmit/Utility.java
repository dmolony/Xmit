package com.bytezone.xmit;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Utility
{
  static CodePage codePage;
  static Map<String, CodePage> codePageMap = new HashMap<> ();

  // ---------------------------------------------------------------------------------//
  //
  // ---------------------------------------------------------------------------------//

  public static void setCodePage (String codePageName)
  {
    if (codePageMap.containsKey (codePageName))
      codePage = codePageMap.get (codePageName);
    else
    {
      codePage = new CodePage (codePageName);
      codePageMap.put (codePageName, codePage);
    }
  }

  // ---------------------------------------------------------------------------------//
  // getString
  // ---------------------------------------------------------------------------------//

  static String getString (byte[] buffer)
  {
    return getString (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  // getString
  // ---------------------------------------------------------------------------------//

  public static String getString (byte[] buffer, int ptr, int length)
  {
    assert ptr + length <= buffer.length;

    StringBuilder text = new StringBuilder ();

    for (int i = 0; i < length; i++)
    {
      int c = buffer[ptr + i] & 0xFF;
      text.append (c < 0x40 || c == 0xFF ? "." : (char) codePage.ebc2asc[c]);
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // matches
  // ---------------------------------------------------------------------------------//

  static boolean matches (byte[] key, byte[] buffer, int ptr)
  {
    if (ptr + key.length >= buffer.length)
      return false;

    for (int i = 0; i < key.length; i++)
      if (key[i] != buffer[ptr + i])
        return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  // matches
  // ---------------------------------------------------------------------------------//

  static boolean matches (byte[] key, int ptr1, byte[] buffer, int ptr2, int length)
  {
    if (ptr1 + length > key.length || ptr2 + length > buffer.length)
      return false;

    for (int i = 0; i < length; i++)
      if (key[ptr1 + i] != buffer[ptr2 + i])
        return false;

    return true;
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

  public static long getFourBytes (byte[] buffer, int ptr)
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
  // getLocalDate
  // ---------------------------------------------------------------------------------//

  public static LocalDate getLocalDate (byte[] buffer, int offset)
  {
    String date1 = String.format ("%02X%02X%02X%02X", buffer[offset], buffer[offset + 1],
        buffer[offset + 2], (buffer[offset + 3] & 0xF0));
    int d1 = Integer.parseInt (date1) / 10;
    return LocalDate.ofYearDay (1900 + d1 / 1000, d1 % 1000);
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
    final StringBuilder hexLine = new StringBuilder ();
    final StringBuilder textLine = new StringBuilder ();

    for (int ptr = offset, max = offset + length; ptr < max; ptr += lineSize)
    {
      hexLine.setLength (0);
      textLine.setLength (0);

      for (int linePtr = 0; linePtr < lineSize; linePtr++)
      {
        int z = ptr + linePtr;
        if (z >= max)
          break;

        hexLine.append (String.format ("%02X ", b[z]));

        int c = b[z] & 0xFF;
        textLine.append (c < 0x40 || c == 0xFF ? '.' : (char) codePage.ebc2asc[c]);
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