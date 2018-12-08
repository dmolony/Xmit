package com.bytezone.xmit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utility
{
  //  public static final String EBCDIC = "CP1047";
  public static final String EBCDIC = "CP037";

  public static int getWord (byte[] buffer, int ptr)
  {
    int a = (buffer[ptr + 1] & 0xFF) << 8;
    int b = buffer[ptr] & 0xFF;
    return a + b;
  }

  public static int getLong (byte[] buffer, int ptr)
  {
    return getWord (buffer, ptr) + getWord (buffer, ptr + 2) * 0x10000;
  }

  public static String getHex (byte[] buffer, int offset, int length)
  {
    StringBuilder text = new StringBuilder ();
    for (int i = offset, max = offset + length; i < max; i++)
      text.append (String.format ("%02X ", buffer[i]));
    return text.toString ();
  }

  public static String getHex (String s)
  {
    if (s == null)
      return "Null";
    StringBuilder text = new StringBuilder ();
    for (char c : s.toCharArray ())
      text.append (String.format ("%02X ", (byte) c));
    return text.toString ();
  }

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
    return toHex (b, 0, b.length, null, displayOffset);
  }

  public static String toHex (byte[] b, int offset, int length)
  {
    return toHex (b, offset, length, null, 0);
  }

  public static String toHex (byte[] b, int offset, int length, String codePage,
      int displayOffset)
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

        if (codePage == null)
        {
          final int val = b[z] & 0x7F;
          if (val >= 0x00 && val <= 0x20)
            textLine.append ('.');
          else
            textLine.append ((char) val);
        }
        else
        {
          final int val = b[z] & 0xFF;
          if (val >= 0x00 && val <= 0x3F)
            textLine.append ('.');
          else
            try
            {
              textLine.append (new String (b, z, 1, codePage));
            }
            catch (UnsupportedEncodingException e)
            {
              e.printStackTrace ();
            }
        }
      }
      text.append (String.format ("%06X  %-48s %s%n", displayOffset + ptr,
          hexLine.toString (), textLine.toString ()));
    }
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  public static void hexDump (String fileName) throws IOException
  {
    System.out.printf ("%nReading: %s", fileName);
    byte[] originalBuffer = Files.readAllBytes (Paths.get (fileName));
    hexDump (originalBuffer);
    System.out.printf ("%nTotal bytes: %,d%n", originalBuffer.length);
  }

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