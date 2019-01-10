package com.bytezone.xmit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;

public class CatalogEntry
{
  final Reader reader;

  private Member member;                                  // contains DataBlocks
  final List<Segment> segments = new ArrayList<> ();      // contains DataBlocks

  final List<String> lines = new ArrayList<> ();

  int lrecl;
  int recfm;

  boolean isPdse;
  private final String name;
  private String userName = "";
  private String aliasName = "";

  private int size;
  private int init;
  private int mod;

  private int vv;
  private int mm;

  private LocalDate dateCreated;
  private LocalDate dateModified;
  private String time = "";

  private final int blockFrom;
  private int dataLength;

  private final byte[] directoryData;
  private final int extra;

  private byte[] ttl = new byte[5];
  private CopyR1 copyR1;
  private CopyR2 copyR2;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CatalogEntry (Reader reader, byte[] buffer, int ptr, int lrecl, int recfm)
  {
    name = Utility.getString (buffer, ptr, 8);
    blockFrom = (int) Utility.getValue (buffer, ptr + 8, 3);
    this.lrecl = lrecl;
    this.recfm = recfm;
    this.reader = reader;

    extra = buffer[ptr + 11] & 0xFF;

    switch (extra)
    {
      case 0x0F:
        basic (buffer, ptr);
        break;

      case 0x14:
        basic (buffer, ptr);
        break;

      case 0x2B:                    //
        break;

      case 0x2C:                    // FILE035
        break;

      case 0x2E:                    // FILE035
        break;

      case 0x31:                    // FILE242
        //        dateCreated = Utility.getLocalDate (buffer, ptr + 34);  NFE
        break;

      case 0x36:                    // file242    // 0xB6 alias of itself?
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0x37:                    // FILE135
        String someName = Utility.getString (buffer, ptr + 46, 8);
        break;

      case 0x8F:                    //
        System.out.printf ("%02X  %s  %s%n", extra, reader.getName (), name);
        basic (buffer, ptr);       // alias without the alias' name ??
        break;

      case 0xB1:                    // alias of 0x2C
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0xB3:                    // FILE035
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0xB6:      // file242    // alias of 0x31
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        Optional<LocalDate> opt = Utility.getLocalDate (buffer, ptr + 44);
        if (opt.isPresent ())
          dateCreated = opt.get ();
        break;

      case 0xD3:
        System.out.printf ("%02X  %s  %s%n", extra, reader.getName (), name);
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0x4E:
        System.out.printf ("%02X  %s  %s%n", extra, reader.getName (), name);
        break;

      case 0xCB:
        System.out.printf ("%02X  %s  %s%n", extra, reader.getName (), name);
        break;

      case 0x4B:
        System.out.printf ("%02X  %s  %s%n", extra, reader.getName (), name);
        break;

      case 0:
        break;

      default:
        System.out.printf ("********************** Unknown extra: %02X in %s%n", extra,
            name);
    }

    int extraLength = 12 + (extra & 0x0F) * 2 + ((extra & 0x10) >> 4) * 32;
    directoryData = new byte[extraLength];
    System.arraycopy (buffer, ptr, directoryData, 0, directoryData.length);
  }

  // ---------------------------------------------------------------------------------//
  // debugLine
  // ---------------------------------------------------------------------------------//

  public String debugLine ()
  {
    String hex = "";
    String t1 = "";

    if (extra == 0x2E)
      hex =
          Utility.getHexValues (directoryData, 12, 22) + "                              "
              + Utility.getHexValues (directoryData, 34, 6);
    else if (extra == 0x31)
      hex =
          Utility.getHexValues (directoryData, 12, 22) + "                              "
              + Utility.getHexValues (directoryData, 34, 12);
    else
      hex = Utility.getHexValues (directoryData, 12, directoryData.length - 12);

    if (extra == 0xB6)
      t1 = Utility.getString (directoryData, 48, 8);

    return String
        .format ("%02X %-8s %-8s %06X %-129s %8s %8s", directoryData[11],
            getMemberName (), getUserName (), blockFrom, hex, getAliasName (), t1)
        .trim ();
  }

  // ---------------------------------------------------------------------------------//
  // basic
  // ---------------------------------------------------------------------------------//

  private void basic (byte[] buffer, int offset)
  {
    userName = Utility.getString (buffer, offset + 32, 8);
    size = Utility.getTwoBytes (buffer, offset + 26);
    init = Utility.getTwoBytes (buffer, offset + 28);
    mod = Utility.getTwoBytes (buffer, offset + 30);

    vv = buffer[offset + 12] & 0xFF;
    mm = buffer[offset + 13] & 0xFF;

    Optional<LocalDate> opt = Utility.getLocalDate (buffer, offset + 16);
    if (opt.isPresent ())
      dateCreated = opt.get ();

    opt = Utility.getLocalDate (buffer, offset + 20);
    if (opt.isPresent ())
      dateModified = opt.get ();

    time = String.format ("%02X:%02X:%02X", buffer[offset + 24], buffer[offset + 25],
        buffer[offset + 15]);

    if (false)
    {
      String vvmmText = String.format ("%02d.%02d", vv, mm);
      String date1Text = String.format ("%td %<tb %<tY", dateCreated).replace (".", "");
      String date2Text = String.format ("%td %<tb %<tY", dateModified).replace (".", "");
      System.out.println (String.format ("%-8s  %6d  %6d %4d  %13s  %13s  %s  %5s  %s",
          name, size, init, mod, date1Text, date2Text, time, vvmmText, userName));
    }
  }

  // ---------------------------------------------------------------------------------//
  // setMember
  // ---------------------------------------------------------------------------------//

  void setMember (Member member)
  {
    this.member = member;

    //    for (DataBlock dataBlock : member)
    //      addBlockPointerList (dataBlock.blockPointerList);
  }

  // ---------------------------------------------------------------------------------//
  // addBlockPointerList
  // ---------------------------------------------------------------------------------//

  private void addSegment (Segment segment)
  {
    segments.add (segment);
    dataLength += segment.getDataLength ();
  }

  // ---------------------------------------------------------------------------------//
  // setCopyRecords
  // ---------------------------------------------------------------------------------//

  void setCopyRecords (CopyR1 copyR1, CopyR2 copyR2)
  {
    isPdse = copyR1.isPdse ();
    this.copyR1 = copyR1;
    this.copyR2 = copyR2;
  }

  private long nothing ()
  {
    int extents = copyR2.buffer[0] & 0xFF;
    //    int mult = copyR2.buffer[31];   // 0x0F;
    int mult = 0x0F;

    //    for (int index = 0; index < extents; index++)
    //    {
    // blockFrom:            xxxx  ff
    // copyR2   :     yyyy   zzzz
    // address  :       qq
    //               ----------------
    //               dd dd  00 rr  ff

    // qq = (xxxx + zzzz) / 15
    // rr = (xxxx + zzzz) % 15

    //    if (false)
    //    {
    //      int xxxx = (blockFrom & 0xFFFF00) >>> 8;
    //      int yyyy = Utility.getTwoBytes (copyR2.buffer, 22);
    //      int zzzz = Utility.getTwoBytes (copyR2.buffer, 24);
    //
    //      int dddd = (xxxx + zzzz) / 15 + yyyy;
    //
    //      ttl[0] = (byte) ((dddd & 0xFF00) >>> 8);
    //      ttl[1] = (byte) ((dddd & 0x00FF) >>> 0);
    //      ttl[2] = 0;
    //      ttl[3] = (byte) ((xxxx + zzzz) % 15);
    //      ttl[4] = (byte) (blockFrom & 0x0000FF);
    //    }

    int index = ((blockFrom & 0x00FF00) >>> 8) / mult;

    long lo = Utility.getFourBytes (copyR2.buffer, index * 16 + 22);
    long hi = Utility.getFourBytes (copyR2.buffer, index * 16 + 26);
    //      System.out.printf ("%02X  Range: %08X : %08X%n", index, lo, hi);

    byte[] temp = convert (index, mult);

    long val = Utility.getValue (temp, 0, 4);     // ignore last byte
    System.out.printf ("%02X  %06X -> %s ", index, blockFrom,
        Utility.getHexValues (temp));

    if (lo <= val && hi >= val)
    {
      System.out.println ("OK");
      ttl = temp;
      //      break;
    }
    else
    {
      System.out.printf (": Out of range%n");
    }
    //    }

    return Utility.getValue (ttl, 0, 5);
  }

  // ---------------------------------------------------------------------------------//
  // convert
  // ---------------------------------------------------------------------------------//

  private byte[] convert (int index, int mult)
  {
    byte[] tt = new byte[5];

    int xxxx = (blockFrom & 0xFFFF00) >>> 8;
    int yyyy = Utility.getTwoBytes (copyR2.buffer, index * 16 + 22);
    int zzzz = Utility.getTwoBytes (copyR2.buffer, index * 16 + 24);

    int dddd = (xxxx + zzzz) / mult + yyyy - index;

    tt[0] = (byte) ((dddd & 0xFF00) >>> 8);
    tt[1] = (byte) ((dddd & 0x00FF) >>> 0);
    tt[2] = 0;
    tt[3] = (byte) ((xxxx + zzzz) % mult);
    tt[4] = (byte) (blockFrom & 0x0000FF);

    return tt;
  }

  // ---------------------------------------------------------------------------------//
  // length
  // ---------------------------------------------------------------------------------//

  int length ()
  {
    return directoryData.length;
  }

  // ---------------------------------------------------------------------------------//
  // getMemberName
  // ---------------------------------------------------------------------------------//

  public String getMemberName ()
  {
    return name;
  }

  // ---------------------------------------------------------------------------------//
  // getUserName
  // ---------------------------------------------------------------------------------//

  public String getUserName ()
  {
    return userName;
  }

  // ---------------------------------------------------------------------------------//
  // isAlias
  // ---------------------------------------------------------------------------------//

  public boolean isAlias ()
  {
    return !aliasName.isEmpty ();
  }

  // ---------------------------------------------------------------------------------//
  // getAliasName
  // ---------------------------------------------------------------------------------//

  public String getAliasName ()
  {
    return aliasName;
  }

  // ---------------------------------------------------------------------------------//
  // getSize
  // ---------------------------------------------------------------------------------//

  public int getSize ()
  {
    return size;
  }

  // ---------------------------------------------------------------------------------//
  // getInit
  // ---------------------------------------------------------------------------------//

  public int getInit ()
  {
    return init;
  }

  // ---------------------------------------------------------------------------------//
  // getDateCreated
  // ---------------------------------------------------------------------------------//

  public LocalDate getDateCreated ()
  {
    return dateCreated;
  }

  // ---------------------------------------------------------------------------------//
  // getDateModified
  // ---------------------------------------------------------------------------------//

  public LocalDate getDateModified ()
  {
    return dateModified;
  }

  // ---------------------------------------------------------------------------------//
  // getTime
  // ---------------------------------------------------------------------------------//

  public String getTime ()
  {
    return time;
  }

  // ---------------------------------------------------------------------------------//
  // getVersion
  // ---------------------------------------------------------------------------------//

  public String getVersion ()
  {
    if (vv == 0 & mm == 0)
      return "";
    return String.format ("%02d.%02d", vv, mm);
  }

  // ---------------------------------------------------------------------------------//
  // getOffset
  // ---------------------------------------------------------------------------------//

  public int getOffset ()
  {
    return blockFrom;
  }

  // ---------------------------------------------------------------------------------//
  // getDataLength
  // ---------------------------------------------------------------------------------//

  public long getDataLength ()
  {
    return dataLength;
  }

  // ---------------------------------------------------------------------------------//
  // getLines
  // ---------------------------------------------------------------------------------//

  public String getLines (boolean showLines)
  {
    if (lines.size () == 0)
    {
      if (segments.size () == 0)
        lines.add ("No data");
      else if (isXmit ())
        xmitList ();
      else if (segments.size () > 100)
        partialDump (10);      // slow!!
      else if (recfm == 0x5000 && isRdw ())
        rdw ();
      else if (segments.get (0).isBinary ())
        hexDump ();
      else
      {
        byte[] buffer = getDataBuffer ();
        createDataLines (buffer);
      }
    }

    StringBuilder text = new StringBuilder ();
    int lineNo = 0;

    for (String line : lines)
      if (showLines)
        text.append (String.format ("%05d %s%n", ++lineNo, line));
      else
        text.append (String.format ("%s%n", line));

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // getDataBuffer
  // ---------------------------------------------------------------------------------//

  public byte[] getDataBuffer ()
  {
    if (isPdse)       // recalculate data length
    {
      dataLength = 0;
      for (Segment segment : segments)
      {
        dataLength += segment.getDataLength ();
        if (segment.isLastBlock ())        // PDSEs end early
          break;
      }
    }

    byte[] dataBuffer = new byte[dataLength];
    int ptr = 0;

    for (Segment segment : segments)
    {
      ptr = segment.getDataBuffer (dataBuffer, ptr);
      if (segment.isLastBlock ())        // PDSEs end early
        break;
    }
    assert ptr == dataLength;

    return dataBuffer;
  }

  // ---------------------------------------------------------------------------------//
  // list
  // ---------------------------------------------------------------------------------//

  public String list ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (this);
    text.append ("\n\n");

    for (Segment segment : segments)
    {
      for (DataBlock dataBlock : segment)
      {
        text.append ("   ");
        text.append (dataBlock);
        text.append ("\n");
      }
    }

    int count = 0;
    for (Segment segment : segments)
    {
      text.append ("\n");
      text.append (String.format (
          "-----------------------< Segment %d of %d >-----------------------\n\n",
          ++count, segments.size ()));

      text.append (segment.listHeaders ());
      text.append ("\n");
    }

    while (text.length () > 0 && text.charAt (text.length () - 1) == '\n')
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // printLine
  // ---------------------------------------------------------------------------------//

  String getPrintLine ()
  {
    return String.format ("%-126s %8s %8s %5d %5d %5d",
        Utility.getHexValues (directoryData), name, userName, size, init, mod);
  }

  // ---------------------------------------------------------------------------------//
  // createDataLines
  // ---------------------------------------------------------------------------------//

  void createDataLines (byte[] buffer)
  {
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

  // ---------------------------------------------------------------------------------//
  // hexDump
  // ---------------------------------------------------------------------------------//

  void hexDump ()
  {
    if (segments.size () == 0)
      return;

    if (segments.get (0).isXmit ())
      lines.add ("Appears to be XMIT");

    // FILE600.XMI
    byte[] buffer = getDataBuffer ();
    lines.add (Utility.getHexDump (buffer));
  }

  // ---------------------------------------------------------------------------------//
  // isRdw
  // ---------------------------------------------------------------------------------//

  boolean isRdw ()
  {
    if (segments.size () == 0)
      return false;

    for (Segment segment : segments)
    {
      if (segment.isLastBlock ())        // PDSEs end early
        break;
      byte[] buffer = segment.getDataBuffer ();
      if (buffer.length == 0)
        continue;

      int len = Utility.getTwoBytes (buffer, 0);
      if (len != buffer.length)
        return false;
    }

    return true;
  }

  // ---------------------------------------------------------------------------------//
  // rdw
  // ---------------------------------------------------------------------------------//

  void rdw ()         // see SOURCE.XMI
  {
    for (Segment segment : segments)
    {
      if (segment.isLastBlock ())        // PDSEs end early
        break;
      byte[] buffer = segment.getDataBuffer ();
      if (buffer.length == 0)
        continue;
      int ptr = 4;
      while (ptr < buffer.length)
      {
        int len = Utility.getTwoBytes (buffer, ptr);
        lines.add (Utility.getString (buffer, ptr + 4, len - 4));
        ptr += len;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // isXmit
  // ---------------------------------------------------------------------------------//

  public boolean isXmit ()
  {
    return segments.size () > 0 && segments.get (0).isXmit ();
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

      for (ControlRecord controlRecord : reader.getControlRecords ())
        lines.add (String.format ("%s", controlRecord));

      Dataset dataset = reader.getActiveDataset ();
      if (dataset.getOrg () == Dsorg.Org.PDS)
      {
        List<CatalogEntry> members = ((PdsDataset) dataset).getMembers ();
        lines.add (String.format ("Members: %s%n", members.size ()));
        lines.add (" Member     User      Size  Offset     Date        Time     Alias");
        lines.add ("--------  --------  ------  ------  -----------  --------  --------");
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

  void partialDump (int max)
  {
    lines.add ("Data too large to display");
    lines.add ("");
    lines.add ("Showing first " + max + " of " + segments.size () + " buffers");
    lines.add ("");

    if (segments.get (0).isXmit ())
      lines.add ("Appears to be XMIT");

    for (int i = 0; i < max; i++)
    {
      Segment segment = segments.get (i);
      if (segment.getDataLength () > 0)
        lines.add (Utility.getHexDump (segment.getDataBuffer ()));
    }
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    String date1Text = dateCreated == null ? ""
        : String.format ("%td %<tb %<tY", dateCreated).replace (".", "");
    return String.format ("%8s  %8s  %,6d  %06X  %s  %s  %s  %8s", name, userName, size,
        blockFrom, Utility.getHexValues (ttl), date1Text, time, aliasName);
  }
}
