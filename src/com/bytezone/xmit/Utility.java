package com.bytezone.xmit;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Utility
{
  static CodePage codePage;
  static Map<String, CodePage> codePageMap = new HashMap<> ();

  private static final byte[] doc = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
                                      (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1 };
  private static final byte[] pdf = { 0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E };
  private static final byte[] zip = { 0x50, 0x4B, 0x03, 0x04 };
  private static final byte[] rar = { 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07 };
  private static final byte[] png = { (byte) 0x89, 0x50, 0x4E, 0x47 };
  private static final byte[] rtf = { 0x7B, 0x5C, 0x72, 0x74, 0x66 };

  public enum FileType
  {
    DOC, PDF, ZIP, RAR, PNG, RTF, BIN, XMIT
  }

  // ---------------------------------------------------------------------------------//
  // setCodePage
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
  // getFileType
  // ---------------------------------------------------------------------------------//

  public static FileType getFileType (byte[] buffer)
  {
    if (matches (pdf, buffer, 0))
      return FileType.PDF;
    if (matches (doc, buffer, 0))
      return FileType.DOC;
    if (matches (zip, buffer, 0))
      return FileType.ZIP;
    if (matches (rar, buffer, 0))
      return FileType.RAR;
    if (matches (png, buffer, 0))
      return FileType.PNG;
    if (matches (rtf, buffer, 0))
      return FileType.RTF;

    return FileType.BIN;
  }

  // ---------------------------------------------------------------------------------//
  // isBinary
  // ---------------------------------------------------------------------------------//

  public static boolean isBinary (byte[] buffer)
  {
    return isBinary (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  // isBinary
  // ---------------------------------------------------------------------------------//

  public static boolean isBinary (byte[] buffer, int offset, int length)
  {
    int max = Math.min (buffer.length, offset + length);
    for (int i = offset; i < max; i++)
    {
      int b = buffer[i] & 0xFF;
      if (b < 0x40 || b == 0xFF)
        return true;
    }
    return false;
  }

  // ---------------------------------------------------------------------------------//
  // removeTrailingNewlines
  // ---------------------------------------------------------------------------------//

  public static void removeTrailingNewlines (StringBuilder text)
  {
    while (text.length () > 0 && text.charAt (text.length () - 1) == '\n')
      text.deleteCharAt (text.length () - 1);
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

  public static boolean matches (byte[] key, byte[] buffer, int ptr)
  {
    if (ptr + key.length > buffer.length)
      return false;

    for (int i = 0; i < key.length; i++)
      if (key[i] != buffer[ptr + i])
        return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  // matches
  // ---------------------------------------------------------------------------------//

  public static boolean matches (byte[] key, int ptr1, byte[] buffer, int ptr2,
      int length)
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

  public static Optional<LocalDate> getLocalDate (byte[] buffer, int offset)
  {
    String date1 = String.format ("%02X%02X%02X%02X", buffer[offset], buffer[offset + 1],
        buffer[offset + 2], (buffer[offset + 3] & 0xF0));
    try
    {
      int d1 = Integer.parseInt (date1) / 10;
      return Optional.of (LocalDate.ofYearDay (1900 + d1 / 1000, d1 % 1000));
    }
    catch (NumberFormatException nfe)
    {
      System.out.printf ("NFE: %s%n", Utility.getHexValues (buffer, offset, 4));
      return Optional.empty ();
    }
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