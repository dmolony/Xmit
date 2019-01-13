package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.xmit.Utility.FileType;
import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;
import com.bytezone.xmit.textunit.Dsorg.Org;

public class Member implements Iterable<DataBlock>
{
  String name;
  final Org org;
  final int lrecl;
  final int recfm;

  private final List<DataBlock> dataBlocks = new ArrayList<> ();
  private final List<DataBlock> extraDataBlocks = new ArrayList<> ();     // PDSE
  private int dataLength = 0;

  final List<String> lines = new ArrayList<> ();
  CodePage codePage;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  Member (Org org, int lrecl, int recfm)        // will be used for PS files too
  {
    this.org = org;
    this.lrecl = lrecl;
    this.recfm = recfm;
  }

  // ---------------------------------------------------------------------------------//
  // setName
  // ---------------------------------------------------------------------------------//

  void setName (String name)
  {
    this.name = name;
  }

  // ---------------------------------------------------------------------------------//
  // addPdsDataBlock
  // ---------------------------------------------------------------------------------//

  void addPdsDataBlock (DataBlock dataBlock)
  {
    byte type = dataBlock.getType ();
    if (type == 0x00 || type == (byte) 0x80)      // basic PDS data
    {
      dataBlocks.add (dataBlock);
      dataLength += dataBlock.getSize ();
    }
    else                                          // additional PDSE blocks
      extraDataBlocks.add (dataBlock);
  }

  // ---------------------------------------------------------------------------------//
  // getDataLength
  // ---------------------------------------------------------------------------------//

  public int getDataLength ()
  {
    return dataLength;
  }

  // ---------------------------------------------------------------------------------//
  // getExtraDataBlocks
  // ---------------------------------------------------------------------------------//

  List<DataBlock> getExtraDataBlocks ()
  {
    return extraDataBlocks;
  }

  // ---------------------------------------------------------------------------------//
  // getDataBuffer
  // ---------------------------------------------------------------------------------//

  public byte[] getDataBuffer ()
  {
    byte[] buffer = new byte[dataLength];
    int ptr = 0;
    for (DataBlock dataBlock : dataBlocks)
      ptr = dataBlock.packBuffer (buffer, ptr);
    assert ptr == dataLength;
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  // isXmit
  // ---------------------------------------------------------------------------------//

  public boolean isXmit ()
  {
    return dataBlocks.get (0).isXmit ();
  }

  // ---------------------------------------------------------------------------------//
  // isRdw
  // ---------------------------------------------------------------------------------//

  boolean isRdw ()
  {
    for (DataBlock dataBlock : dataBlocks)
      if (dataBlock.getTwoBytes () != dataBlock.getSize ())
        return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  // getFileType
  // ---------------------------------------------------------------------------------//

  public FileType getFileType ()
  {
    if (isXmit ())
      return FileType.XMIT;

    byte[] buffer = getEightBytes ();
    return Utility.getFileType (buffer);
  }

  // ---------------------------------------------------------------------------------//
  // getEightBytes
  // ---------------------------------------------------------------------------------//

  byte[] getEightBytes ()
  {
    return dataBlocks.get (0).getEightBytes ();
  }

  // ---------------------------------------------------------------------------------//
  // size
  // ---------------------------------------------------------------------------------//

  int size ()
  {
    return dataBlocks.size ();
  }

  // ---------------------------------------------------------------------------------//
  // getLines
  // ---------------------------------------------------------------------------------//

  public String getLines (boolean showLines)
  {
    if (lines.size () == 0 || codePage != Utility.codePage)
      createDataLines ();

    StringBuilder text = new StringBuilder ();
    int lineNo = 0;

    for (String line : lines)
      if (showLines)
        text.append (String.format ("%05d %s%n", ++lineNo, line));
      else
        text.append (String.format ("%s%n", line));

    Utility.removeTrailingNewlines (text);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // createDataLines
  // ---------------------------------------------------------------------------------//
  // FILE706 - java bytecode
  // FILE765 - embedded xmit PS file
  // FILE714 - tar

  private void createDataLines ()
  {
    codePage = Utility.codePage;
    lines.clear ();

    if (isXmit ())
      xmitList ();
    else if (recfm == 0xC000)
      hexDump ();
    //    else if (member.getDataLength () > 100000)
    //      partialDump (1);
    else if ((recfm == 0x5000 || recfm == 0x5200) && isRdw ())
      rdw ();
    else if (getFileType () != FileType.BIN)
      extractMessage ();
    else
    {
      byte[] buffer = getDataBuffer ();
      int ptr = 0;
      int length = buffer.length;
      while (length > 0)
      {
        int len = Math.min (lrecl == 0 ? 80 : lrecl, length);
        lines.add (Utility.getString (buffer, ptr, len).stripTrailing ());
        ptr += len;
        length -= len;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // hexDump
  // ---------------------------------------------------------------------------------//

  private void hexDump ()
  {
    // FILE600.XMI
    //    byte[] buffer = member.getDataBuffer ();

    for (DataBlock dataBlock : dataBlocks)
    {
      byte[] buffer = dataBlock.getBuffer ();
      String[] chunks = Utility.getHexDump (buffer).split ("\n");
      for (String chunk : chunks)
        lines.add (chunk);
      if (lines.size () > 500)
        break;
    }
  }

  // ---------------------------------------------------------------------------------//
  // extractMessage
  // ---------------------------------------------------------------------------------//

  void extractMessage ()
  {
    lines.add ("File type: " + getFileType ());
    lines.add ("");
    lines.add ("Use File->Extract to save a copy in the correct format,");
    lines.add ("      or use the HEX tab to view the raw file.");
  }

  // ---------------------------------------------------------------------------------//
  // rdw
  // ---------------------------------------------------------------------------------//

  void rdw ()         // see SOURCE.XMI
  {
    for (DataBlock dataBlock : dataBlocks)
    {
      byte[] buffer = dataBlock.getBuffer ();

      int ptr = 4;
      while (ptr < buffer.length && lines.size () < 2000)
      {
        int len = Utility.getTwoBytes (buffer, ptr);
        lines.add (Utility.getString (buffer, ptr + 4, len - 4));
        ptr += len;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // xmitList
  // ---------------------------------------------------------------------------------//

  void xmitList ()
  {
    byte[] xmitBuffer = getDataBuffer ();
    try
    {
      Reader reader = new Reader (name, xmitBuffer);
      Dataset dataset = reader.getActiveDataset ();

      for (ControlRecord controlRecord : reader.getControlRecords ())
        lines.add (String.format ("%s", controlRecord));

      if (dataset.getOrg () == Dsorg.Org.PDS)
      {
        List<CatalogEntry> members = ((PdsDataset) dataset).getMembers ();
        lines.add (String.format ("Members: %s%n", members.size ()));
        lines.add (" Member     User      Size     Date        Time     Alias");
        lines.add ("--------  --------  ------  -----------  --------  --------");
        for (CatalogEntry catalogEntry : members)
          lines.add (catalogEntry.toString ());
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
  // partialDump
  // ---------------------------------------------------------------------------------//

  //  private void partialDump (int max)
  //  {
  //    lines.add ("Data too large to display");
  //    lines.add ("");
  //    lines.add ("Showing first " + max + " of " + member.size () + " blocks");
  //    lines.add ("");
  //
  //    int count = 0;
  //    for (DataBlock dataBlock : member)
  //    {
  //      if (dataBlock.getSize () > 0)
  //        lines.add (Utility.getHexDump (dataBlock.getBuffer ()));
  //      if (++count > max)
  //        break;
  //    }
  //  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    int count = 0;
    int total = 0;
    for (DataBlock dataBlock : dataBlocks)
    {
      total += dataBlock.getSize ();
      text.append (String.format ("   %3d  %s%n", count++, dataBlock));
    }

    text.append (String.format ("%44.44s %s%n", "", "------ ---------"));
    text.append (String.format ("%44.44s %06X %<,9d%n%n", "", total));

    for (DataBlock dataBlock : extraDataBlocks)
      text.append (String.format ("   %3d  %s%n", count++, dataBlock));

    Utility.removeTrailingNewlines (text);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // iterator
  // ---------------------------------------------------------------------------------//

  @Override
  public Iterator<DataBlock> iterator ()
  {
    return dataBlocks.iterator ();
  }
}
