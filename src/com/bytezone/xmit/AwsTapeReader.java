package com.bytezone.xmit;

import java.io.File;

// ---------------------------------------------------------------------------------//
public class AwsTapeReader extends Reader
// ---------------------------------------------------------------------------------//
{
  private static final byte[] header = { (byte) 0xCA, 0x6D, 0x0F };

  // ---------------------------------------------------------------------------------//
  public AwsTapeReader (File file)
  // ---------------------------------------------------------------------------------//
  {
    super (file.getName (), ReaderType.TAPE);

    read (readFile (file));
  }

  // ---------------------------------------------------------------------------------//
  private void read (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    Utility.setCodePage ("CP037");

    Dataset currentDataset = null;
    AwsTapeHeaders currentAwsTapeDataset = null;
    BlockPointer hdr1 = null;
    BlockPointer hdr2 = null;
    int tapeMarkCount = 0;

    int ptr = 0;

    while (ptr < buffer.length)
    {
      int next = Utility.getTwoBytesReversed (buffer, ptr);
      int prev = Utility.getTwoBytesReversed (buffer, ptr + 2);
      int flag = Utility.getTwoBytesReversed (buffer, ptr + 4);

      ptr += 6;

      if (next == 0)            // tape mark
      {
        ++tapeMarkCount;
        continue;
      }

      BlockPointer blockPointer = new BlockPointer (buffer, ptr, next);
      AwsTapeSegment segment = new AwsTapeSegment ();
      segment.addBlockPointer (blockPointer);
      ptr += next;

      if (tapeMarkCount == 1)
      {
        if (currentDataset == null)
        {
          // why is there no dsorg?
          if (Utility.matches (header, buffer, blockPointer.offset + 9))
          {
            currentAwsTapeDataset = new AwsTapeHeaders (this, hdr1, hdr2);
            currentDataset = new PdsDataset (this, currentAwsTapeDataset);
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
        currentDataset.addSegment (segment);
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
            currentAwsTapeDataset.addTrailer (blockPointer);
            break;

          case "EOF2":
            currentAwsTapeDataset.addTrailer (blockPointer);
            currentDataset = null;
            break;

          case "VOL1":
            break;

          default:
            System.out.println ("Unknown header: " + header);
        }
      }
    }

    // allocate the data records
    for (Dataset dataset : datasets)
      dataset.allocateSegments ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("Tape Reader: %s", getFileName ());
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
