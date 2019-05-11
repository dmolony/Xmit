package com.bytezone.xmit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

// ---------------------------------------------------------------------------------//
public class AwsTape
// ---------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public static void main (String[] args)
  // ---------------------------------------------------------------------------------//
  {
    File file = new File ("/Users/denismolony/Downloads/rpf171.aws");
    Utility.setCodePage ("CP037");
    try
    {
      byte[] buffer = Files.readAllBytes (file.toPath ());
      int ptr = 0;
      int record = 0;
      while (ptr < buffer.length)
      {
        int next = bif (buffer, ptr);
        int prev = bif (buffer, ptr + 2);
        int flag = bif (buffer, ptr + 4);
        record++;
        System.out.printf ("%,6d  %06X: %04X  %04X  %04X%n", record, ptr, next, prev,
            flag);
        if (prev == 0 && next == 0)
          break;
        System.out.println (Utility.getHexDump (buffer, ptr + 6, next));
        if (record > 10)
          break;
        ptr += next + 6;
      }
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private static int bif (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return (buffer[ptr] & 0xFF) | ((buffer[ptr + 1] & 0xFF) << 8);
  }
}
