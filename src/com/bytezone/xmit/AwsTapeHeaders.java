package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class AwsTapeHeaders
// -----------------------------------------------------------------------------------//
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
  String reserved1;

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
  String reserved2;
  String reserved3;
  String reserved4;

  Disposition disposition;
  AwsTapeReader reader;

  private final List<BlockPointer> headers = new ArrayList<> ();
  private final List<BlockPointer> trailers = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  AwsTapeHeaders (AwsTapeReader reader, BlockPointer hdr1, BlockPointer hdr2)
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
    reserved1 = hdr1.getString (73, 3);
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
    reserved2 = hdr2.getString (37, 1);
    blockAttribute = hdr2.getString (38, 1);
    reserved3 = hdr2.getString (39, 2);
    deviceSerialNumber = hdr2.getString (41, 6);
    checkpointDatasetIdentifier = hdr2.getString (47, 1);
    reserved4 = hdr2.getString (48, 22);
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
  public String header1 ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Dataset Identifier ......... %s%n", name));
    text.append (String.format ("Dataset serial number ...... %s%n", serialNumber));
    text.append (String.format ("Volume sequence number ..... %s%n", volSeq));
    text.append (String.format ("Dataset sequence number .... %s%n", dsnSeq));
    text.append (String.format ("Generation number .......... %s%n", genNumber));
    text.append (String.format ("Version number ............. %s%n", genVersionNumber));
    text.append (String.format ("Creation date .............. %s%n", creationDate));
    text.append (String.format ("Expiration date ............ %s%n", expirationDate));
    text.append (String.format ("Dataset security ........... %s%n", datasetSecurity));
    text.append (String.format ("Block count, Low order ..... %s%n", blockCountLo));
    text.append (String.format ("System code ................ %s%n", systemCode));
    text.append (String.format ("Reserved ................... %s%n", reserved1));
    text.append (String.format ("Block count, High order .... %s%n", blockCountHi));

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public String header2 ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Record format .............. %s%n", recfm));
    text.append (String.format ("Block length ............... %s%n", blockLength));
    text.append (String.format ("Record length .............. %s%n", recordLength));
    text.append (String.format ("Tape density ............... %s%n", tapeDensity));
    text.append (String.format ("Dataset position ........... %s%n", datasetPosition));
    text.append (String.format ("Job name ................... %s%n", jobName));
    text.append (String.format ("Job step ................... %s%n", jobStep));
    text.append (
        String.format ("Tape recording technique ... %s%n", tapeRecordingTechnique));
    text.append (String.format ("Control character .......... %s%n", controlCharacter));
    text.append (String.format ("Reserved ................... %s%n", reserved2));
    text.append (String.format ("Block attribute ............ %s%n", blockAttribute));
    text.append (String.format ("Reserved ................... %s%n", reserved3));
    text.append (String.format ("Device serial number ....... %s%n", deviceSerialNumber));
    text.append (
        String.format ("Checkpoint dataset id ...... %s%n", checkpointDatasetIdentifier));
    text.append (String.format ("Reserved ................... %s%n", reserved4));
    text.append (String.format ("Large block length ......... %s%n", largeBlockLength));

    return text.toString ();
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
