package com.bytezone.xmit;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.bytezone.xmit.gui.XmitApp;

// -----------------------------------------------------------------------------------//
public class Utility
// -----------------------------------------------------------------------------------//
{
  static final String nameStart = "[A-Z@$#]";
  static final String validChar = "[A-Z0-9@$#]";
  public static final String validPart = nameStart + validChar + "{0,7}";
  public static final String validName = validPart + "(?:\\." + validPart + "){0,4}";
  static final Pattern validPDSName =
      Pattern.compile ("^(" + validName + ")\\((" + validPart + ")\\)$");
  static final Pattern validDatasetName = Pattern.compile (validName);
  public static final Pattern jobCardPattern =
      Pattern.compile ("//([A-Z]" + validChar + "{0,7})\\s+JOB");

  private static CodePage codePage;
  private static Map<String, CodePage> codePageMap = new HashMap<> ();

  private static final byte[][] signatures = {
      { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1 },                                // DOC
      { 0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E },   // PDF
      { 0x50, 0x4B, 0x03, 0x04 },                     // ZIP
      { 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07 },         // RAR
      { (byte) 0x89, 0x50, 0x4E, 0x47 },              // PNG
      { 0x7B, 0x5C, 0x72, 0x74, 0x66 },               // RTF
      { 0x5A, 0x00, 0x12, (byte) 0xD3, (byte) 0xA8, (byte) 0xA8, 0x00, 0x00 }, // AFP
  };

  // ---------------------------------------------------------------------------------//
  public enum FileType
  // ---------------------------------------------------------------------------------//
  {
    DOC, PDF, ZIP, RAR, PNG, RTF, AFP, BIN, XMI     // must match signatures array
  }

  // ---------------------------------------------------------------------------------//
  public static CodePage getCodePage ()
  // ---------------------------------------------------------------------------------//
  {
    return codePage;
  }

  // ---------------------------------------------------------------------------------//
  public static void setCodePage (String codePageName)
  // ---------------------------------------------------------------------------------//
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
  public static FileType getFileType (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    int count = 0;
    for (byte[] signature : signatures)
    {
      if (matches (signature, buffer, 0))
        return FileType.values ()[count];
      ++count;
    }

    return FileType.BIN;
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isBinary (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    return isBinary (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isBinary (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    int max = Math.min (buffer.length, offset + length);
    for (int i = offset; i < max; i++)
    {
      int b = buffer[i] & 0xFF;
      if (b == 0 || b == 0xFF || b == 0x20)        // ascii space character
        //      if ((b < 0x40 && b != 0x05 && b != 0x0C) || b == 0xFF)
        return true;
    }
    return false;
  }

  // ---------------------------------------------------------------------------------//
  public static void removeTrailingNewlines (StringBuilder text)
  // ---------------------------------------------------------------------------------//
  {
    while (text.length () > 0 && text.charAt (text.length () - 1) == '\n')
      text.deleteCharAt (text.length () - 1);
  }

  // ---------------------------------------------------------------------------------//
  static String getString (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    return getString (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  public static String getString (byte[] buffer, int ptr, int length)
  // ---------------------------------------------------------------------------------//
  {
    assert ptr + length <= buffer.length;

    StringBuilder text = new StringBuilder ();

    for (int i = 0; i < length; i++)
    {
      int c = buffer[ptr + i] & 0xFF;
      if (c == 0x05)                 // tab
        text.append ("  ");
      else if (c == 0x0C)            // form feed    
        text.append ("\n");
      else if (c == 0x22)            // FS field separator
      {

      }
      else if (isUnicode (buffer, ptr + i))
      {
        text.append (getUnicode (buffer, ptr + i));
        i += 5;
      }
      //      else if (c == 0x66 || c == 0x63 || (c == 0x67 && buffer[ptr + i + 1] != 0x22))
      //        System.out.println (Utility.getHexValues (buffer, i + ptr, 2));
      else
        text.append (c < 0x40 || c == 0xFF ? "." : (char) codePage.ebc2asc[c]);
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  static byte[] convertCodePage (byte[] buffer, int ptr, int length)
  // ---------------------------------------------------------------------------------//
  {
    byte[] line = new byte[length];
    for (int i = 0, j = ptr; i < length; i++, j++)
      line[i] = (byte) codePage.ebc2asc[buffer[j] & 0xFF];
    return line;
  }

  // ---------------------------------------------------------------------------------//
  static String translateUnicode (byte[] buffer, int ptr, int length)
  // ---------------------------------------------------------------------------------//
  {
    return translateUnicode (new String (convertCodePage (buffer, ptr, length)));
  }

  // ---------------------------------------------------------------------------------//
  static String translateUnicode (String s)
  // ---------------------------------------------------------------------------------//
  {
    String[] chunks = s.split ("\\\\u\\d{3} ");       // this could be improved
    if (chunks.length == 1)
      return s;

    StringBuilder text = new StringBuilder (chunks[0]);
    int ptr = chunks[0].length ();

    for (int i = 1; i < chunks.length; i++)
    {
      text.append (
          Character.toString (Integer.parseInt (new String (s.substring (ptr + 2, ptr + 5)))));
      text.append (chunks[i]);
      ptr += chunks[i].length () + 6;
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private static boolean isUnicode (byte[] buffer, int i)
  // ---------------------------------------------------------------------------------//
  {
    if (i + 5 >= buffer.length)
      return false;
    if (buffer[i++] != (byte) 0xE0)
      return false;
    if (buffer[i++] != (byte) 0xA4)
      return false;
    for (int j = 0; j < 3; j++)
      if (!isDigit (buffer[i++] & 0xFF))
        return false;
    if (buffer[i] != (byte) 0x40)
      return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  private static String getUnicode (byte[] buffer, int i)
  // ---------------------------------------------------------------------------------//
  {
    String number = getString (buffer, i + 2, 3);             // could be 4?
    return Character.toString (Integer.parseInt (number));
  }

  // ---------------------------------------------------------------------------------//
  public static boolean matches (byte[] key, byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    if (ptr + key.length > buffer.length)
      return false;

    for (int i = 0; i < key.length; i++)
      if (key[i] != buffer[ptr + i])
        return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  public static boolean matches (byte[] key, int ptr1, byte[] buffer, int ptr2, int length)
  // ---------------------------------------------------------------------------------//
  {
    if (ptr1 + length > key.length || ptr2 + length > buffer.length)
      return false;

    for (int i = 0; i < length; i++)
      if (key[ptr1 + i] != buffer[ptr2 + i])
        return false;

    return true;
  }

  //----------------------------------------------------------------------------------- //
  public static String stripLineNumber (String line)
  //----------------------------------------------------------------------------------- //
  {
    if (line.length () < 72 || line.length () > 80)
      return line;
    String numbers = line.substring (72);
    for (char c : numbers.toCharArray ())
      if ((c < '0' || c > '9') && c != ' ')
        return line;
    return line.substring (0, 72).stripTrailing ();
  }

  // ---------------------------------------------------------------------------------//
  public static int getTwoBytes (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return ((buffer[ptr] & 0xFF) << 8) | (buffer[++ptr] & 0xFF);
  }

  // ---------------------------------------------------------------------------------//
  public static int getTwoBytesReversed (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return (buffer[ptr] & 0xFF) | ((buffer[++ptr] & 0xFF) << 8);
  }

  // ---------------------------------------------------------------------------------//
  public static long getFourBytes (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return ((buffer[ptr] & 0xFF) << 24) | ((buffer[++ptr] & 0xFF) << 16)
        | ((buffer[++ptr] & 0xFF) << 8) | (buffer[++ptr] & 0xFF);
  }

  // ---------------------------------------------------------------------------------//
  public static long getValue (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    long value = 0;
    while (length-- > 0)
      value = value << 8 | buffer[offset++] & 0xFF;
    return value;
  }

  // ---------------------------------------------------------------------------------//
  public static Optional<LocalDate> getLocalDate (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
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
    catch (DateTimeException dte)
    {
      System.out.printf ("DTE: %s%n", Utility.getHexValues (buffer, offset, 4));
      return Optional.empty ();
    }
  }

  // ---------------------------------------------------------------------------------//
  public static String getHexValues (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    return getHexValues (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  public static String getHexValues (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    for (int i = offset, max = offset + length; i < max; i++)
      text.append (String.format ("%02X ", buffer[i]));
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public static String getHexValuesWithText (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    return getHexValuesWithText (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  public static String getHexValuesWithText (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    for (int i = offset, max = offset + length; i < max; i++)
    {
      int c = buffer[i] & 0xFF;
      if (isLetter (c) || isDigit (c) || isOther (c))
        text.append (String.format ("%s. ", (char) codePage.ebc2asc[c]));
      else
        text.append (String.format ("%02X ", c));
    }
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private static boolean isDigit (int c)
  // ---------------------------------------------------------------------------------//
  {
    return c >= 0xF0 && c <= 0xF9;
  }

  // ---------------------------------------------------------------------------------//
  private static boolean isLetter (int c)
  // ---------------------------------------------------------------------------------//
  {
    return (c >= 0xC1 && c <= 0xC9) || (c >= 0xD1 && c <= 0xD9) || (c >= 0xE2 && c <= 0xE9);
  }

  // ---------------------------------------------------------------------------------//
  private static boolean isOther (int c)
  // ---------------------------------------------------------------------------------//
  {
    // space, $, -, #, @
    return c == 0x40 || c == 0x5B || c == 0x60 || c == 0x7B || c == 0x7C;
  }

  // ---------------------------------------------------------------------------------//
  public static String getHexDump (byte[] b)
  // ---------------------------------------------------------------------------------//
  {
    return getHexDump (b, 0, b.length);
  }

  // ---------------------------------------------------------------------------------//
  public static String getHexDump (byte[] b, int displayOffset)
  // ---------------------------------------------------------------------------------//
  {
    return getHexDump (b, 0, b.length, displayOffset);
  }

  // ---------------------------------------------------------------------------------//
  public static String getHexDump (byte[] b, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    return getHexDump (b, offset, length, 0);
  }

  // ---------------------------------------------------------------------------------//
  public static String getHexDump (byte[] b, int offset, int length, int displayOffset)
  // ---------------------------------------------------------------------------------//
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
      text.append (String.format ("%06X  %-48s %s%n", displayOffset + ptr, hexLine.toString (),
          textLine.toString ()));
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public static List<String> getHexDumpLines (byte[] b, int displayOffset)
  // ---------------------------------------------------------------------------------//
  {
    return getHexDumpLines (b, 0, b.length, displayOffset);
  }

  // ---------------------------------------------------------------------------------//
  public static List<String> getHexDumpLines (byte[] b, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    return getHexDumpLines (b, offset, length, 0);
  }

  // ---------------------------------------------------------------------------------//
  public static List<String> getHexDumpLines (byte[] b, int offset, int length, int displayOffset)
  // ---------------------------------------------------------------------------------//
  {
    final int lineSize = 16;

    List<String> lines = new ArrayList<> ();
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
      lines.add (String.format ("%06X  %-48s %s", displayOffset + ptr, hexLine.toString (),
          textLine.toString ()));
    }

    return lines;
  }

  // ---------------------------------------------------------------------------------//
  public static String getLocalCodePage (String name)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    String line;

    DataInputStream inputStream = new DataInputStream (
        XmitApp.class.getClassLoader ().getResourceAsStream ("com/bytezone/xmit/codepages.txt"));
    try (BufferedReader in = new BufferedReader (new InputStreamReader (inputStream)))
    {
      int count = 0;
      boolean inPage = false;
      String id = "[" + name + "]";
      while ((line = in.readLine ().trim ()) != null)
      {
        if (!inPage)
        {
          if (line.equals (id))
            inPage = true;
          continue;
        }

        text.append (line);
        text.append (" ");
        if (++count == 8)
          break;
      }
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  static int find (byte[] haystack, byte[] needle)
  // ---------------------------------------------------------------------------------//
  // see also: https://gist.github.com/coderodde/7f8ec57c13a265b51115ceb082d273e0
  {
    loop: for (int i = 0; i <= haystack.length - needle.length; ++i)
    {
      for (int j = 0; j < needle.length; ++j)
        if (haystack[i + j] != needle[j])
          continue loop;

      return i;
    }

    return -1;
  }

  //----------------------------------------------------------------------------------- //
  public static boolean isJCL (List<String> lines)
  //----------------------------------------------------------------------------------- //
  {
    return jobCardPattern.matcher (getFirstNonComment (lines)).find ();
  }

  //----------------------------------------------------------------------------------- //
  private static String getFirstNonComment (List<String> lines)
  //----------------------------------------------------------------------------------- //
  {
    for (String line : lines)
      if (!line.startsWith ("//*"))
        return line;
    return "";
  }

  // ---------------------------------------------------------------------------------//
  public static void printStackTrace ()
  // ---------------------------------------------------------------------------------//
  {
    for (StackTraceElement ste : Thread.currentThread ().getStackTrace ())
      System.out.println (ste);
  }
}