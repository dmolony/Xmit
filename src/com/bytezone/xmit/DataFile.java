package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bytezone.xmit.Utility.FileType;
import com.bytezone.xmit.textunit.ControlRecord;

// ---------------------------------------------------------------------------------//
public abstract class DataFile implements Comparable<DataFile>
//---------------------------------------------------------------------------------//
{
  private static final int MAX_BUFFER = 500_000;

  private String name = "";
  private final Disposition disposition;
  private final Reader reader;

  int dataLength = 0;

  final List<String> lines = new ArrayList<> ();
  private CodePage codePage;

  // ---------------------------------------------------------------------------------//
  DataFile (Dataset dataset, Disposition disposition)
  // ---------------------------------------------------------------------------------//
  {
    this.disposition = disposition;
    this.reader = dataset.reader;
  }

  // ---------------------------------------------------------------------------------//
  public int getLevel ()
  // ---------------------------------------------------------------------------------//
  {
    return reader.getLevel () + 1;
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
  public FileType getFileType ()
  // ---------------------------------------------------------------------------------//
  {
    return isXmit () ? FileType.XMI : Utility.getFileType (getEightBytes ());
  }

  // ---------------------------------------------------------------------------------//
  public String getLines (boolean showLines, boolean stripLines, boolean truncate)
  // ---------------------------------------------------------------------------------//
  {
    if (lines.size () == 0 || codePage != Utility.getCodePage ())
      createDataLines ();

    StringBuilder text = new StringBuilder ();
    int lineNo = 0;

    if (showLines)
      for (String line : lines)
      {
        if (stripLines)
          line = strip (line);
        text.append (String.format ("%05d %s%n", ++lineNo, line));
      }
    else
      for (String line : lines)
      {
        if (stripLines)
          line = strip (line);
        if (truncate && line.length () > 0)
          text.append (String.format ("%s%n", line.substring (1)));
        else
          text.append (String.format ("%s%n", line));
      }

    Utility.removeTrailingNewlines (text);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private String strip (String line)
  // ---------------------------------------------------------------------------------//
  {
    if (line.length () != 80)
      return line;
    String numbers = line.substring (72);
    for (char c : numbers.toCharArray ())
      if (c < 48 || c > 57)
        return line;
    return line.substring (0, 72).stripTrailing ();
  }

  // ---------------------------------------------------------------------------------//
  // test files
  // ---------------------------------------------------------------------------------//
  // FILE185 - FILE234I - incomplete
  // FILE313 - many extractable files
  // FILE404 - $$PREZ18 - possibly corrupt
  // FILE600 - XMITPDSC - VB
  // FILE706 - java bytecode
  // FILE714 - tar
  // FILE765 - embedded xmit PS file
  // FILE784 - PAXFILE  - PS FB1 / 23778
  // FILE859 - $OBJECT  - Object Deck
  // FILE880 - CPP      - PDSE
  // FILE880 - HPP      - requires different codepages
  // FILE898 - PDSEDITL - level 4
  // FILE910 - xmit/xmit/PS
  // FILE972 - two datasets in each xmit member

  // ---------------------------------------------------------------------------------//
  private void createDataLines ()
  // ---------------------------------------------------------------------------------//
  {
    codePage = Utility.getCodePage ();
    lines.clear ();

    if (isXmit ())
      xmitLines ();
    else if (disposition.recfm == 0xC000        // undefined
        || disposition.lrecl <= 1)
      hexDump ();
    else if ((disposition.recfm == 0x5000       // VB
        || disposition.recfm == 0x5200          // VBA
        || disposition.recfm == 0x5400)         // VBA
        && isRdw ())
      rdwLines ();
    else if (disposition.recfm == 0x5002)       // flat file
    {
      rdwLines ();
      //      createTextLines (getDataBuffer ());
    }
    else if (disposition.recfm == 0x9200)       // FBA
      createTextLines (getDataBuffer ());
    else if (isObjectDeck ())
      objectDeck ();
    else if (getFileType () != FileType.BIN)
      showExtractMessage ();
    else
      createLines ();
  }

  // ---------------------------------------------------------------------------------//
  private void createLines ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = getDataBuffer (MAX_BUFFER);

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
      lines.add (Utility.getString (buffer, ptr, len).stripTrailing ());
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
    byte[] buffer = getDataBuffer (MAX_BUFFER);
    lines.add ("Object Deck Output:");
    lines.add ("");

    lines.addAll (Arrays.asList (Utility.getHexDump (buffer).split ("\n")));

    //    ObjectDeck objectDeck = new ObjectDeck (getDataBuffer (), disposition.lrecl);
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
  private void hexDump ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = getDataBuffer (MAX_BUFFER);
    for (String line : Arrays.asList (Utility.getHexDump (buffer).split ("\n")))
      lines.add (line);
  }

  // ---------------------------------------------------------------------------------//
  private void xmitLines ()
  // ---------------------------------------------------------------------------------//
  {
    try
    {
      Reader reader = new Reader (this);
      Dataset dataset = reader.getActiveDataset ();

      for (ControlRecord controlRecord : reader.getControlRecords ())
        lines.add (String.format ("%s", controlRecord));

      if (dataset.isPds ())
      {
        lines.add (String.format ("Members: %s%n", ((PdsDataset) dataset).size ()));
        lines.add (" Member     User      Size     Date        Time     Alias");
        lines.add ("--------  --------  ------  -----------  --------  --------");
        for (PdsMember member : (PdsDataset) dataset)
          for (CatalogEntry catalogEntry : member.getCatalogEntries ())
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
}
