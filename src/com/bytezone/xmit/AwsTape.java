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
        int next = Utility.getTwoBytesReversed (buffer, ptr);
        int prev = Utility.getTwoBytesReversed (buffer, ptr + 2);
        int flag = Utility.getTwoBytesReversed (buffer, ptr + 4);
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

  // from FILE533.XMI -> M370VTT2
  /*
   *           HEADER TYPE       ACTION                       **
  **           ------ ----       ------                       **
  **           X'80'             Initialize buffer address.   **
  **                             Write chunk of data to       **
  **                              the buffer.                 **
  **                                                          **
  **           X'00'             Write another chunk of data  **
  **                              to the end of the previous  **
  **                              chunk in the buffer.        **
  **                                                          **
  **           X'20'             Add the chunk of data to     **
  **                              the buffer.                 **
  **                             Write out the entire buffer. **
  **                             Initialize the start of      **
  **                              buffer address.             **
  **                                                          **
  **           X'A0'             X'80' and X'20' combined.    **
  **                                                          **
  **           X'40'             Write a tape mark.           **
  **                             Finalize the tape file.      **
  **                             Initialize the output buffer **
  **                              location.                   **
   */
}
