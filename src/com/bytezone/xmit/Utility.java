package com.bytezone.xmit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

  //  static String ebc2asc (byte[] buffer)
  //  {
  //    byte[] newBuffer = new byte[buffer.length];
  //    int ptr = 0;
  //    for (int i = 0; i < buffer.length; i++)
  //      //      if (buffer[i] != 0)                                       // suppress nulls
  //      newBuffer[ptr++] = (byte) ebc2asc[buffer[i] & 0xFF];
  //
  //    return new String (newBuffer);
  //  }

  public static int getWord (byte[] buffer, int ptr)        // 2 bytes
  {
    int a = (buffer[ptr + 1] & 0xFF) << 8;
    int b = buffer[ptr] & 0xFF;
    return a + b;
  }

  public static int getLong (byte[] buffer, int ptr)        // 4 bytes
  {
    return getWord (buffer, ptr) + getWord (buffer, ptr + 2) * 0x10000;
  }

  public static long getValue (byte[] buffer, int offset, int length)
  {
    long value = 0;
    while (length-- > 0)
      value = value << 8 | buffer[offset++] & 0xFF;
    return value;
  }

  public static String getHex (byte[] buffer, int offset, int length)
  {
    StringBuilder text = new StringBuilder ();
    for (int i = offset, max = offset + length; i < max; i++)
      text.append (String.format ("%02X ", buffer[i]));
    return text.toString ();
  }

  //  public static String getHex (String s)
  //  {
  //    if (s == null)
  //      return "Null";
  //    StringBuilder text = new StringBuilder ();
  //    for (char c : s.toCharArray ())
  //      text.append (String.format ("%02X ", (byte) c));
  //    return text.toString ();
  //  }

  public static String toBits (int x, int width)
  {
    String bits = "000000000000" + Integer.toBinaryString (x);
    return String.format ("Value: %03X (%s)%n", x,
        bits.substring (bits.length () - width));
  }

  public static String toHex (byte[] b)
  {
    return toHex (b, 0, b.length);
  }

  public static String toHex (byte[] b, int displayOffset)
  {
    return toHex (b, 0, b.length, displayOffset);
  }

  public static String toHex (byte[] b, int offset, int length)
  {
    return toHex (b, offset, length, 0);
  }

  public static String toHex (byte[] b, int offset, int length, int displayOffset)
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
        textLine.append (val <= 0x3F ? '.' : (char) ebc2asc[val]);
      }
      text.append (String.format ("%06X  %-48s %s%n", displayOffset + ptr,
          hexLine.toString (), textLine.toString ()));
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  //  public static void hexDump (String fileName) throws IOException
  //  {
  //    System.out.printf ("%nReading: %s", fileName);
  //    byte[] originalBuffer = Files.readAllBytes (Paths.get (fileName));
  //    hexDump (originalBuffer);
  //    System.out.printf ("%nTotal bytes: %,d%n", originalBuffer.length);
  //  }

  public static void hexDump (byte[] buffer)
  {
    int max = buffer.length;

    for (int i = 0; i < max; i++)
    {
      if (i % 16 == 0)
        System.out.printf ("%n%06X: ", i);

      System.out.printf ("%02X ", buffer[i]);
    }
  }

  public static void bitDump (String fileName) throws IOException
  {
    System.out.printf ("%nReading: %s", fileName);
    byte[] originalBuffer = Files.readAllBytes (Paths.get (fileName));
    bitDump (originalBuffer);
    System.out.printf ("%nTotal bytes: %,d%n", originalBuffer.length);
  }

  public static void bitDump (byte[] buffer)
  {
    int max = buffer.length;

    for (int i = 0; i < max; i++)
    {
      if (i % 16 == 0)
        System.out.printf ("%n%06X: ", i);

      String s1 = String.format ("%8s", Integer.toBinaryString (buffer[i] & 0xFF))
          .replace (' ', '0');
      System.out.printf ("%s ", s1);
    }
  }

  public static void printStackTrace ()
  {
    for (StackTraceElement ste : Thread.currentThread ().getStackTrace ())
      System.out.println (ste);
  }
}