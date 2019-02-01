package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bytezone.xmit.Utility.FileType;
import com.bytezone.xmit.textunit.ControlRecord;

// ---------------------------------------------------------------------------------//
// DataFile
// ---------------------------------------------------------------------------------//

public abstract class DataFile implements Comparable<DataFile>
{
  private static final int MAX_BUFFER = 200_000;

  private String name = "";
  private final Disposition disposition;

  int dataLength = 0;

  final List<String> lines = new ArrayList<> ();
  private CodePage codePage;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  DataFile (Disposition disposition)
  {
    this.disposition = disposition;
  }

  // ---------------------------------------------------------------------------------//
  // setName
  // ---------------------------------------------------------------------------------//

  void setName (String name)
  {
    this.name = name;
  }

  // ---------------------------------------------------------------------------------//
  // getName
  // ---------------------------------------------------------------------------------//

  public String getName ()
  {
    return name;
  }

  // ---------------------------------------------------------------------------------//
  // getDataLength
  // ---------------------------------------------------------------------------------//

  public int getDataLength ()
  {
    return dataLength;
  }

  // ---------------------------------------------------------------------------------//
  // getFileType
  // ---------------------------------------------------------------------------------//

  public FileType getFileType ()
  {
    return isXmit () ? FileType.XMIT : Utility.getFileType (getEightBytes ());
  }

  // ---------------------------------------------------------------------------------//
  // getLines
  // ---------------------------------------------------------------------------------//

  public String getLines (boolean showLines, boolean truncate)
  {
    if (lines.size () == 0 || codePage != Utility.getCodePage ())
      createDataLines ();

    StringBuilder text = new StringBuilder ();
    int lineNo = 0;

    if (showLines)
      for (String line : lines)
        text.append (String.format ("%05d %s%n", ++lineNo, line));
    else
      for (String line : lines)
        if (truncate && line.length () > 0)
          text.append (String.format ("%s%n", line.substring (1)));
        else
          text.append (String.format ("%s%n", line));

    Utility.removeTrailingNewlines (text);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // createDataLines
  // ---------------------------------------------------------------------------------//
  // FILE185 - FILE234I - incomplete
  // FILE404 - $$PREZ18 - possibly corrupt
  // FILE600 - XMITPDSC - VB
  // FILE706 - java bytecode
  // FILE714 - tar
  // FILE765 - embedded xmit PS file
  // FILE784 - PAXFILE  - PS FB1 / 23778
  // FILE859 - $OBJECT  - Object Deck
  // FILE880 - CPP      - PDSE
  // FILE880 - HPP      - requires different codepages
  // FILE910 - xmit/xmit/PS
  // FILE972 - two datasets in each xmit member

  private void createDataLines ()
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
  // createLines
  // ---------------------------------------------------------------------------------//

  void createLines ()
  {
    byte[] buffer = getDataBuffer (MAX_BUFFER);

    if (Utility.isBinary (buffer, 0, 128))
      for (String line : Arrays.asList (Utility.getHexDump (buffer).split ("\n")))
        lines.add (line);
    else
      createTextLines (buffer);
  }

  // ---------------------------------------------------------------------------------//
  // createTextLines
  // ---------------------------------------------------------------------------------//

  private void createTextLines (byte[] buffer)
  {
    int ptr = 0;
    int length = buffer.length;
    while (length > 0)
    {
      int len = Math.min (disposition.lrecl == 0 ? 80 : disposition.lrecl, length);
      lines.add (Utility.getString (buffer, ptr, len).stripTrailing ());
      ptr += len;
      length -= len;
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

  boolean isObjectDeck ()
  {
    return Utility.matches (object, getEightBytes (), 0);
  }

  // ---------------------------------------------------------------------------------//
  // object
  // ---------------------------------------------------------------------------------//

  void objectDeck ()
  {
    byte[] buffer = getDataBuffer (MAX_BUFFER);
    lines.add ("Object Deck Output:");
    lines.add ("");

    lines.addAll (Arrays.asList (Utility.getHexDump (buffer).split ("\n")));

    //    ObjectDeck objectDeck = new ObjectDeck (getDataBuffer (), disposition.lrecl);
  }

  // ---------------------------------------------------------------------------------//
  // extractMessage
  // ---------------------------------------------------------------------------------//

  void showExtractMessage ()
  {
    lines.add ("File type: " + getFileType ());
    lines.add ("");
    lines.add ("Use File->Extract to save a copy in the correct format,");
    lines.add ("      or use the HEX tab to view the raw file.");
  }

  // ---------------------------------------------------------------------------------//
  // hexDump
  // ---------------------------------------------------------------------------------//

  void hexDump ()
  {
    byte[] buffer = getDataBuffer (MAX_BUFFER);
    for (String line : Arrays.asList (Utility.getHexDump (buffer).split ("\n")))
      lines.add (line);
  }

  // ---------------------------------------------------------------------------------//
  // xmitLines
  // ---------------------------------------------------------------------------------//

  void xmitLines ()
  {
    byte[] xmitBuffer = getDataBuffer ();
    try
    {
      Reader reader = new Reader (name, xmitBuffer);
      Dataset dataset = reader.getActiveDataset ();

      for (ControlRecord controlRecord : reader.getControlRecords ())
        lines.add (String.format ("%s", controlRecord));

      if (dataset.isPds ())
      {
        lines.add (String.format ("Members: %s%n", ((PdsDataset) dataset).size ()));
        lines.add (" Member     User      Size     Date        Time     Alias");
        lines.add ("--------  --------  ------  -----------  --------  --------");
        for (PdsMember member : (PdsDataset) dataset)
          lines.add (member.getCatalogEntry ().toString ());
      }
    }
    catch (Exception e)
    {
      lines.add ("Data length: " + xmitBuffer.length);
      lines.add (e.getMessage ());
      lines.add (Utility.getHexDump (xmitBuffer));
    }
  }

  // ---------------------------------------------------------------------------------//
  // compareTo
  // ---------------------------------------------------------------------------------//

  @Override
  public int compareTo (DataFile o)
  {
    return this.name.compareTo (o.name);
  }
}
