package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.Utility.FileType;
import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;

public abstract class Member implements Comparable<Member>
{
  String name = "???";
  final Disposition disposition;

  int dataLength = 0;

  final List<String> lines = new ArrayList<> ();
  CodePage codePage;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  Member (Disposition disposition)
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
  // getDataBuffer
  // ---------------------------------------------------------------------------------//

  public abstract byte[] getDataBuffer ();

  // ---------------------------------------------------------------------------------//
  // isXmit
  // ---------------------------------------------------------------------------------//

  public abstract boolean isXmit ();

  // ---------------------------------------------------------------------------------//
  // isRdw
  // ---------------------------------------------------------------------------------//

  abstract boolean isRdw ();

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

  abstract byte[] getEightBytes ();

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
  // FILE910 - xmit/xmit/PS
  // FILE784 - PAXFILE FB1
  // FILE600 - XMITPDSC VB
  // FILE185 - FILE234I - incomplete

  private void createDataLines ()
  {
    codePage = Utility.codePage;
    lines.clear ();

    if (isXmit ())
      xmitList ();
    else if (disposition.recfm == 0xC000 || disposition.lrecl <= 1)
      hexDump ();
    //    else if (member.getDataLength () > 100000)
    //      partialDump (1);
    else if ((disposition.recfm == 0x5000 || disposition.recfm == 0x5200) && isRdw ())
      rdw ();
    else if (getFileType () != FileType.BIN)
      extractMessage ();
    else
    {
      createLines ();
      //      if (disposition.dsorg == Org.PS)
      //        ps ();
      //      else
      //        pds ();
    }
  }

  abstract void createLines ();

  // ---------------------------------------------------------------------------------//
  // hexDump
  // ---------------------------------------------------------------------------------//

  abstract void hexDump ();

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

  abstract void rdw ();         // see SOURCE.XMI

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

      if (dataset.getDisposition ().getOrg () == Dsorg.Org.PDS)
      {
        lines.add (String.format ("Members: %s%n", ((PdsDataset) dataset).size ()));
        lines.add (" Member     User      Size     Date        Time     Alias");
        lines.add ("--------  --------  ------  -----------  --------  --------");
        for (Member member : (PdsDataset) dataset)
          lines.add (((PdsMember) member).getCatalogEntry ().toString ());
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
  // compareTo
  // ---------------------------------------------------------------------------------//

  @Override
  public int compareTo (Member o)
  {
    return this.name.compareTo (o.name);
  }
}
