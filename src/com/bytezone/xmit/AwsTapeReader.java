package com.bytezone.xmit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// ---------------------------------------------------------------------------------//
public class AwsTapeReader
{
  private final List<Segment> segments = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public AwsTapeReader (File file)
  // ---------------------------------------------------------------------------------//
  {
    Utility.setCodePage ("CP037");
    Segment currentSegment = null;
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
            currentSegment = new Segment (blockPointer);
            segments.add (currentSegment);
            tapeMarkCount = 0;
            break;

          case "HDR2":
            currentSegment.addHeader2 (blockPointer);
            break;

          case "EOF1":
          case "EOF2":
            currentSegment.trailers.add (blockPointer);
            break;

          case "VOL1":
            break;

          default:
            System.out.println ("Unknown header: " + header);
        }
      }
    }

    System.out.println ();
    for (Segment segment : segments)
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

  // ---------------------------------------------------------------------------------//
  class Segment
  // ---------------------------------------------------------------------------------//
  {
    String name;
    String serialNumber;
    String volSeq;
    String dsnSeq;
    String genNumber;
    String versionNumber;
    String creationDate;
    String expirationDate;
    String datasetSecurity;
    String blockCountLo;
    String blockCountHi;
    String systemCode;

    String recfm;
    String blockLength;
    String recordLength;
    String tapeDensity;
    String datasetPosition;
    String jobName;
    String slash;
    String jobStep;
    String tapeRecordingTechnique;
    String controlCharacter;
    String blockAttribute;
    String deviceSerialNumber;
    String checkpointDatasetIdentifier;
    String largeBlockLength;

    int dataLength;
    Disposition disposition;

    private final List<BlockPointer> blockPointers = new ArrayList<> ();
    private final List<BlockPointer> headers = new ArrayList<> ();
    private final List<BlockPointer> trailers = new ArrayList<> ();

    // ---------------------------------------------------------------------------------//
    Segment (BlockPointer hdr1)
    // ---------------------------------------------------------------------------------//
    {
      addHeader1 (hdr1);
    }

    // ---------------------------------------------------------------------------------//
    void addHeader1 (BlockPointer hdr1)
    // ---------------------------------------------------------------------------------//
    {
      headers.add (hdr1);

      name = hdr1.getString (4, 17);
      serialNumber = hdr1.getString (21, 6);
      volSeq = hdr1.getString (27, 4);
      dsnSeq = hdr1.getString (31, 4);
      genNumber = hdr1.getString (35, 4);
      versionNumber = hdr1.getString (39, 2);
      creationDate = hdr1.getString (41, 6);      // space=1900, 0=2000, 1=2100
      expirationDate = hdr1.getString (47, 6);    // space=1900, 0=2000, 1=2100
      datasetSecurity = hdr1.getString (53, 1);
      blockCountLo = hdr1.getString (54, 6);
      systemCode = hdr1.getString (60, 13);
      String notUsed = hdr1.getString (73, 3);
      blockCountHi = hdr1.getString (76, 4);
    }

    // ---------------------------------------------------------------------------------//
    void addHeader2 (BlockPointer hdr2)
    // ---------------------------------------------------------------------------------//
    {
      headers.add (hdr2);

      recfm = hdr2.getString (4, 1);
      blockLength = hdr2.getString (5, 5);
      recordLength = hdr2.getString (10, 5);

      tapeDensity = hdr2.getString (15, 1);
      datasetPosition = hdr2.getString (16, 1);
      jobName = hdr2.getString (17, 8);
      slash = hdr2.getString (25, 1);
      jobStep = hdr2.getString (26, 8);
      tapeRecordingTechnique = hdr2.getString (34, 2);
      controlCharacter = hdr2.getString (36, 1);
      String notUsed = hdr2.getString (37, 1);
      blockAttribute = hdr2.getString (38, 1);
      String notUsed2 = hdr2.getString (39, 2);
      deviceSerialNumber = hdr2.getString (41, 6);
      checkpointDatasetIdentifier = hdr2.getString (47, 1);
      String notUsed3 = hdr2.getString (48, 22);
      largeBlockLength = hdr2.getString (70, 10);

      disposition = new Disposition (recfm, recordLength, blockLength);
      //      System.out.println (disposition);
    }

    // ---------------------------------------------------------------------------------//
    void addData (BlockPointer blockPointer)
    // ---------------------------------------------------------------------------------//
    {
      blockPointers.add (blockPointer);
      dataLength += blockPointer.length;
    }

    // ---------------------------------------------------------------------------------//
    void dump ()
    // ---------------------------------------------------------------------------------//
    {
      byte[] buffer = new byte[dataLength];
      int ptr = 0;
      for (BlockPointer blockPointer : blockPointers)
      {
        System.arraycopy (blockPointer.buffer, blockPointer.offset, buffer, ptr,
            blockPointer.length);
        ptr += blockPointer.length;
      }
      assert ptr == dataLength;
      System.out.println (Utility.getHexDump (buffer));
    }

    // ---------------------------------------------------------------------------------//
    String header2 ()
    // ---------------------------------------------------------------------------------//
    {
      return String.format ("%s  %s  %s  %s  %s  %s  %s  %s  %s  %s  %s  %s", recfm,
          blockLength, recordLength, tapeDensity, datasetPosition, jobName, jobStep,
          tapeRecordingTechnique, controlCharacter, blockAttribute, deviceSerialNumber,
          checkpointDatasetIdentifier);
    }

    // ---------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // ---------------------------------------------------------------------------------//
    {
      return String.format (
          "%s  %s  %s  %s  %s %,6d %,10d  [%s]  [%s]  %s  %s  %s  %s  %s  %s", name,
          serialNumber, volSeq, dsnSeq, genNumber, blockPointers.size (), dataLength,
          versionNumber, creationDate, expirationDate, datasetSecurity, blockCountLo,
          systemCode, blockCountHi, largeBlockLength);
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
