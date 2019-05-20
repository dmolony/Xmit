package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// ---------------------------------------------------------------------------------//
class AwsTapeDataset
//---------------------------------------------------------------------------------//
{
  private static final int DIR_BLOCK_LENGTH = 0x114;

  String name;
  String serialNumber;
  String volSeq;
  String dsnSeq;
  String genNumber;
  String genVersionNumber;
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
  AwsTapeReader reader;

  private final List<BlockPointer> blockPointers = new ArrayList<> ();
  private final List<BlockPointer> headers = new ArrayList<> ();
  private final List<BlockPointer> trailers = new ArrayList<> ();

  private final List<CatalogEntry> catalogEntries = new ArrayList<> ();

  private CopyR1 copyR1;
  private CopyR2 copyR2;

  // ---------------------------------------------------------------------------------//
  AwsTapeDataset (AwsTapeReader reader, BlockPointer hdr1, BlockPointer hdr2)
  // ---------------------------------------------------------------------------------//
  {
    this.reader = reader;
    addHeader1 (hdr1);
    addHeader2 (hdr2);
  }

  // ---------------------------------------------------------------------------------//
  private void addHeader1 (BlockPointer hdr1)
  // ---------------------------------------------------------------------------------//
  {
    assert hdr1.length == 0x50;

    headers.add (hdr1);

    name = hdr1.getString (4, 17);
    serialNumber = hdr1.getString (21, 6);
    volSeq = hdr1.getString (27, 4);
    dsnSeq = hdr1.getString (31, 4);
    genNumber = hdr1.getString (35, 4);
    genVersionNumber = hdr1.getString (39, 2);
    creationDate = hdr1.getString (41, 6);      // space=1900, 0=2000, 1=2100
    expirationDate = hdr1.getString (47, 6);    // space=1900, 0=2000, 1=2100
    datasetSecurity = hdr1.getString (53, 1);
    blockCountLo = hdr1.getString (54, 6);
    systemCode = hdr1.getString (60, 13);
    String notUsed = hdr1.getString (73, 3);
    blockCountHi = hdr1.getString (76, 4);
  }

  // ---------------------------------------------------------------------------------//
  private void addHeader2 (BlockPointer hdr2)
  // ---------------------------------------------------------------------------------//
  {
    assert hdr2.length == 0x50;

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
  void addTrailer (BlockPointer blockPointer)
  // ---------------------------------------------------------------------------------//
  {
    assert blockPointer.length == 0x50;
    trailers.add (blockPointer);
  }

  // ---------------------------------------------------------------------------------//
  void addData (BlockPointer blockPointer)
  // ---------------------------------------------------------------------------------//
  {
    blockPointers.add (blockPointer);
    dataLength += blockPointer.length;
  }

  // ---------------------------------------------------------------------------------//
  //  @Override
  void allocateSegments ()
  // ---------------------------------------------------------------------------------//
  {
    int segmentNbr = 0;

    // convert first two DataBlock entries
    copyR1 = new CopyR1 (blockPointers.get (segmentNbr++).getData (8));
    copyR2 = new CopyR2 (blockPointers.get (segmentNbr++).getData (8));
    disposition.setPdse (copyR1.isPdse ());

    // read catalog entries
    Map<Long, List<CatalogEntry>> catalogMap = new TreeMap<> ();
    while (segmentNbr < blockPointers.size ())
    {
      BlockPointer segment = blockPointers.get (segmentNbr++);
      if (!addCatalogEntries (segment.getData (8), catalogMap))
        break;
    }
  }

  // ---------------------------------------------------------------------------------//
  List<DataBlock> createDataBlocks ()                     // used only for data blocks
  // ---------------------------------------------------------------------------------//
  {
    // convert BlockPointers to DataBlocks
    List<DataBlock> dataBlocks = new ArrayList<> ();
    int count = 0;
    for (int i = 2; i < blockPointers.size (); i++)
    {
      BlockPointer blockPointer = blockPointers.get (i);

      int ptr = blockPointer.offset + 8;
      if (blockPointer.buffer[ptr + 9] == 0x08)          // skip catalog entries
        continue;

      while (ptr < blockPointer.offset + blockPointer.length)
      {
        System.out.printf ("%3d  %s%n", ++count,
            Utility.getHexDump (blockPointer.buffer, ptr, 12));
        int len = Utility.getTwoBytes (blockPointer.buffer, ptr + 10);
        ptr += len + 12;
      }
    }
    return dataBlocks;
  }

  // ---------------------------------------------------------------------------------//
  void dump ()
  // ---------------------------------------------------------------------------------//
  {
    CopyR1 r1 = new CopyR1 (blockPointers.get (0).getData (8));
    CopyR2 r2 = new CopyR2 (blockPointers.get (1).getData (8));
    System.out.println (r1);
    //    System.out.println (r2);

    for (int i = 2; i < blockPointers.size (); i++)
    {
      BlockPointer blockPointer = blockPointers.get (i);
      //      byte[] buffer = blockPointer.getData (8);
      System.out.println (
          Utility.getHexDump (blockPointer.buffer, blockPointer.offset + 8, 12));
    }
  }

  // ---------------------------------------------------------------------------------//
  private boolean addCatalogEntries (byte[] buffer,
      Map<Long, List<CatalogEntry>> catalogMap)
  // ---------------------------------------------------------------------------------//
  {
    int ptr1 = 0;
    while (ptr1 + 22 < buffer.length)
    {
      int ptr2 = ptr1 + 22;

      while (true)
      {
        if (buffer[ptr2] == (byte) 0xFF)
          return false;                                     // member list finished

        CatalogEntry catalogEntry = CatalogEntry.instanceOf (buffer, ptr2);
        catalogEntries.add (catalogEntry);
        addToMap (catalogEntry, catalogMap);

        // check for last member
        if (Utility.matches (buffer, ptr2, buffer, ptr1 + 12, 8))
          break;

        ptr2 += catalogEntry.getEntryLength ();
      }

      ptr1 += DIR_BLOCK_LENGTH;
    }

    return true;                                            // member list not finished
  }

  // ---------------------------------------------------------------------------------//
  // addToMap
  // ---------------------------------------------------------------------------------//

  private void addToMap (CatalogEntry catalogEntry,
      Map<Long, List<CatalogEntry>> catalogMap)
  {
    long ttr = catalogEntry.getTtr ();
    List<CatalogEntry> catalogEntriesTtr = catalogMap.get (ttr);

    if (catalogEntriesTtr == null)
    {
      catalogEntriesTtr = new ArrayList<> ();
      catalogMap.put (ttr, catalogEntriesTtr);
    }

    if (catalogEntry.isAlias ())
      catalogEntriesTtr.add (catalogEntry);       // retain original sequence
    else
      catalogEntriesTtr.add (0, catalogEntry);    // insert at the head of the list
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
        genVersionNumber, creationDate, expirationDate, datasetSecurity, blockCountLo,
        systemCode, blockCountHi, largeBlockLength);
  }
}
