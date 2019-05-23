package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

// ---------------------------------------------------------------------------------//
class AwsTapeDataset
//---------------------------------------------------------------------------------//
{
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

  Disposition disposition;
  AwsTapeReader reader;

  private final List<BlockPointer> headers = new ArrayList<> ();
  private final List<BlockPointer> trailers = new ArrayList<> ();

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
  }

  // ---------------------------------------------------------------------------------//
  void addTrailer (BlockPointer blockPointer)
  // ---------------------------------------------------------------------------------//
  {
    assert blockPointer.length == 0x50;
    trailers.add (blockPointer);
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
    return String.format ("%s  %s  %s  %s  %s  [%s]  [%s]  %s  %s  %s  %s  %s  %s", name,
        serialNumber, volSeq, dsnSeq, genNumber, genVersionNumber, creationDate,
        expirationDate, datasetSecurity, blockCountLo, systemCode, blockCountHi,
        largeBlockLength);
  }
}
