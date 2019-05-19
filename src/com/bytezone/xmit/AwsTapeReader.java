package com.bytezone.xmit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// ---------------------------------------------------------------------------------//
public class AwsTapeReader
{
  private static final byte[] header = { (byte) 0xCA, 0x6D, 0x0F };
  private final List<AwsTapeDataset> datasets = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public AwsTapeReader (File file)
  // ---------------------------------------------------------------------------------//
  {
    Utility.setCodePage ("CP037");
    AwsTapeDataset currentDataset = null;
    BlockPointer hdr1 = null;
    BlockPointer hdr2 = null;
    int tapeMarkCount = 0;

    byte[] buffer = readFile (file);

    int ptr = 0;
    //    int record = 0;

    while (ptr < buffer.length)
    {
      int next = Utility.getTwoBytesReversed (buffer, ptr);
      int prev = Utility.getTwoBytesReversed (buffer, ptr + 2);
      int flag = Utility.getTwoBytesReversed (buffer, ptr + 4);

      //      record++;
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
      {
        if (currentDataset == null)
        {
          // why is there no dsorg?
          if (Utility.matches (header, buffer, blockPointer.offset + 9))
          {
            currentDataset = new AwsTapeDataset (this, hdr1, hdr2);     // PDS
            datasets.add (currentDataset);
          }
          else
          {
            System.out.println ("flat file??");
            break;
          }
          hdr1 = null;
          hdr2 = null;
        }
        currentDataset.addData (blockPointer);
      }
      else
      {
        String header = blockPointer.getString (0, 4);

        switch (header)
        {
          case "HDR1":
            hdr1 = blockPointer;
            tapeMarkCount = 0;
            break;

          case "HDR2":
            hdr2 = blockPointer;
            break;

          case "EOF1":
            currentDataset.addTrailer (blockPointer);
            break;

          case "EOF2":
            currentDataset.addTrailer (blockPointer);
            currentDataset = null;
            break;

          case "VOL1":
            break;

          default:
            System.out.println ("Unknown header: " + header);
        }
      }
    }

    System.out.println ();
    for (AwsTapeDataset dataset : datasets)
    {
      System.out.println (dataset);
      System.out.println (dataset.header2 ());
      dataset.dump2 ();
    }

    //    AwsTapeDataset dataset = datasets.get (4);
    //    dataset.dump2 ();
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
