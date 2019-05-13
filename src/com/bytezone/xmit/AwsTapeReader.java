package com.bytezone.xmit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// ---------------------------------------------------------------------------------//
public class AwsTapeReader
{
  private final List<AwsSegment> segments = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public AwsTapeReader (File file)
  // ---------------------------------------------------------------------------------//
  {
    Utility.setCodePage ("CP037");
    AwsSegment currentSegment = null;
    int tapeMarkCount = 0;

    byte[] buffer = readFile (file);

    int ptr = 0;
    int record = 0;

    while (ptr < buffer.length)
    {
      int next = Utility.getTwoBytesReversed (buffer, ptr);
      int prev = Utility.getTwoBytesReversed (buffer, ptr + 2);
      int flag = Utility.getTwoBytesReversed (buffer, ptr + 4);

      record++;
      ptr += 6;
      //      System.out.printf ("%,6d  %d  %04X  %04X  %04X%n", record, tapeMarkCount, next,
      //          prev, flag);

      if (next == 0)            // tape mark
      {
        ++tapeMarkCount;
        continue;
      }

      BlockPointer blockPointer = new BlockPointer (buffer, ptr, next);
      ptr += next;

      if (tapeMarkCount == 1)
        currentSegment.addData (blockPointer);
      else
      {
        String header = blockPointer.getString (0, 4);

        switch (header)
        {
          case "HDR1":
            currentSegment = new AwsSegment (blockPointer);
            segments.add (currentSegment);
            tapeMarkCount = 0;
            break;

          case "HDR2":
            currentSegment.addHeader2 (blockPointer);
            break;

          case "EOF1":
          case "EOF2":
            currentSegment.addTrailer (blockPointer);
            break;

          case "VOL1":
            break;

          default:
            System.out.println ("Unknown header: " + header);
        }
      }
    }

    System.out.println ();
    for (AwsSegment segment : segments)
    {
      System.out.println (segment);
      System.out.println (segment.header2 ());
    }

    //    segments.get (2).dump ();
  }

  // ---------------------------------------------------------------------------------//
  private byte[] readFile (File file)
  // ---------------------------------------------------------------------------------//
  {
    try
    {
      return Files.readAllBytes (file.toPath ());
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      return null;
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
  public static void main (String[] args)
  {
    File file = new File ("/Users/denismolony/Downloads/rpf171.aws");
    new AwsTapeReader (file);
  }
}
