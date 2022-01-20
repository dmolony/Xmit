package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bytezone.xmit.Utility.FileType;
import com.bytezone.xmit.textunit.ControlRecord;

// -----------------------------------------------------------------------------------//
public abstract class DataFile implements Comparable<DataFile>
// -----------------------------------------------------------------------------------//
{
  private String name = "";
  private final Disposition disposition;
  private final Dataset dataset;

  private int dataLength = 0;

  final List<String> lines = new ArrayList<> ();      // current formatted output...
  private CodePage codePage;                          // .. built using this codepage

  // ---------------------------------------------------------------------------------//
  DataFile (Dataset dataset, Disposition disposition)
  // ---------------------------------------------------------------------------------//
  {
    this.disposition = disposition;
    this.dataset = dataset;
  }

  // ---------------------------------------------------------------------------------//
  public Disposition getDisposition ()
  // ---------------------------------------------------------------------------------//
  {
    return disposition;
  }

  // ---------------------------------------------------------------------------------//
  public Dataset getDataset ()
  // ---------------------------------------------------------------------------------//
  {
    return dataset;
  }

  // ---------------------------------------------------------------------------------//
  void setName (String name)
  // ---------------------------------------------------------------------------------//
  {
    this.name = name;
  }

  // ---------------------------------------------------------------------------------//
  public String getName ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }

  // ---------------------------------------------------------------------------------//
  public int getDataLength ()
  // ---------------------------------------------------------------------------------//
  {
    return dataLength;
  }

  // ---------------------------------------------------------------------------------//
  void incrementDataLength (int increment)
  // ---------------------------------------------------------------------------------//
  {
    dataLength += increment;
  }

  // ---------------------------------------------------------------------------------//
  public FileType getFileType ()
  // ---------------------------------------------------------------------------------//
  {
    return isXmit () ? FileType.XMI : Utility.getFileType (getEightBytes ());
  }

  // ---------------------------------------------------------------------------------//
  boolean isVB ()
  // ---------------------------------------------------------------------------------//
  {
    return (disposition.recfm == 0x5000       // VB
        || disposition.recfm == 0x5200        // VBA
        || disposition.recfm == 0x5400);      // VBA
  }

  // ---------------------------------------------------------------------------------//
  public boolean contains (String key)
  // ---------------------------------------------------------------------------------//
  {
    byte[] ebcdicKey = key.getBytes ();
    CodePage codePage = Utility.getCodePage ();

    // convert key to ebcdic
    for (int i = 0; i < ebcdicKey.length; i++)
      ebcdicKey[i] = (byte) codePage.asc2ebc[ebcdicKey[i] & 0xFF];

    return Utility.find (getDataBuffer (), ebcdicKey) >= 0;
  }

  // ---------------------------------------------------------------------------------//
  public List<String> getLines ()
  // ---------------------------------------------------------------------------------//
  {
    if (lines.size () == 0 || codePage != Utility.getCodePage ())
      createDataLines ();

    return lines;
  }

  // ---------------------------------------------------------------------------------//
  private void createDataLines ()
  // ---------------------------------------------------------------------------------//
  {
    codePage = Utility.getCodePage ();
    lines.clear ();

    if (isXmit ())
      xmitLines ();
    else if (getFileType () != FileType.BIN)    // ZIP/DOC/PDF etc
      showExtractMessage ();
    else if (disposition.recfm == 0xC000        // undefined - usually a load file
        || disposition.lrecl <= 1)
      undefined ();
    else if (isVB () && isRdw ())
      rdwLines ();
    else if (disposition.recfm == 0x5002)       // VB flat file
      rdwLines ();
    else if (disposition.recfm == 0x9200)       // FBA
      createTextLines (getDataBuffer ());
    else if (isObjectDeck ())
      objectDeck ();
    else
      createLines ();
  }

  // ---------------------------------------------------------------------------------//
  private void createLines ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = getDataBuffer ();

    if (Utility.isBinary (buffer, 0, 128))
      for (String line : Arrays.asList (Utility.getHexDump (buffer).split ("\n")))
        lines.add (line);
    else
      createTextLines (buffer);
  }

  // ---------------------------------------------------------------------------------//
  private void createTextLines (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = 0;
    int remaining = buffer.length;
    int lrecl = disposition.lrecl == 0 ? 80 : disposition.lrecl;
    while (remaining > 0)
    {
      int len = Math.min (lrecl, remaining);
      String line = Utility.translateUnicode (buffer, ptr, len).stripTrailing ();
      lines.add (line);
      ptr += len;
      remaining -= len;
    }
  }

  // ---------------------------------------------------------------------------------//
  // abstract methods
  // ---------------------------------------------------------------------------------//

  abstract byte[] getEightBytes ();

  public abstract byte[] getDataBuffer ();

  public abstract byte[] getDataBuffer (int limit);

  public abstract boolean isXmit ();

  abstract boolean isRdw ();

  abstract void rdwLines ();         // see SOURCE.XMI

  // ---------------------------------------------------------------------------------//
  // isObjectDeck
  // ---------------------------------------------------------------------------------//

  // https://www.ibm.com/support/knowledgecenter/en
  //         /SSLTBW_2.1.0/com.ibm.zos.v2r1.asma100/object.htm
  static private final byte[] object =
      { 0x02, (byte) 0xC5, (byte) 0xE2, (byte) 0xC4, 0x40, 0x40, 0x40, 0x40 };

  // ---------------------------------------------------------------------------------//
  boolean isObjectDeck ()
  // ---------------------------------------------------------------------------------//
  {
    return Utility.matches (object, getEightBytes (), 0);
  }

  // ---------------------------------------------------------------------------------//
  private void objectDeck ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = getDataBuffer ();
    lines.add ("Object Deck Output:");
    lines.add ("");

    lines.addAll (Arrays.asList (Utility.getHexDump (buffer).split ("\n")));

    //    ObjectDeck objectDeck = new ObjectDeck (getDataBuffer (), disposition.lrecl);
  }

  // ---------------------------------------------------------------------------------//
  void undefined ()
  // ---------------------------------------------------------------------------------//
  {
    hexDump ();
  }

  // ---------------------------------------------------------------------------------//
  private void showExtractMessage ()
  // ---------------------------------------------------------------------------------//
  {
    lines.add ("File type: " + getFileType ());
    lines.add ("");
    lines.add ("Use File->Extract to save a local copy in the correct format,");
    lines.add ("      or use the HEX tab to view the raw file.");
  }

  // ---------------------------------------------------------------------------------//
  void hexDump ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = getDataBuffer ();
    for (String line : Arrays.asList (Utility.getHexDump (buffer).split ("\n")))
      lines.add (line);
  }

  // ---------------------------------------------------------------------------------//
  private void xmitLines ()
  // ---------------------------------------------------------------------------------//
  {
    try
    {
      XmitReader reader = new XmitReader (this);        // fix this!!

      for (ControlRecord controlRecord : reader.getControlRecords ())
        lines.add (String.format ("%s", controlRecord));

      if (dataset.isPartitionedDataset ())
      {
        lines.add (String.format ("Members: %s%n", ((PdsDataset) dataset).size ()));
        lines.add (" Member     User      Size     Date        Time     Alias");
        lines.add ("--------  --------  ------  -----------  --------  --------");
        for (CatalogEntry catalogEntry : (PdsDataset) dataset)
          lines.add (catalogEntry.toString ());
      }
    }
    catch (Exception e)
    {
      byte[] xmitBuffer = getDataBuffer ();
      lines.add ("Data length: " + xmitBuffer.length);
      lines.add (e.getMessage ());
      lines.add (Utility.getHexDump (xmitBuffer));
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int compareTo (DataFile o)
  // ---------------------------------------------------------------------------------//
  {
    return this.name.compareTo (o.name);
  }

  // ---------------------------------------------------------------------------------//
  // test files
  // ---------------------------------------------------------------------------------//
  // FILE182 - EDITXMIT - load library with doco in U format member
  // FILE185 - FILE234I - incomplete
  // FILE313 - many extractable files
  // FILE404 - $$PREZ18 - possibly corrupt
  // FILE600 - XMITPDSC - VB
  // FILE706 - java bytecode, also very large hex display
  // FILE714 - tar
  // FILE765 - embedded xmit PS file
  // FILE784 - PAXFILE  - PS FB1 / 23778
  // FILE859 - $OBJECT  - Object Deck
  // FILE880 - CPP      - PDSE
  // FILE880 - HPP      - requires different codepages
  // FILE898 - PDSEDITL - level 4
  // FILE910 - xmit/xmit/PS
  // FILE972 - two datasets in each xmit member
}
