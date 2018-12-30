package com.bytezone.xmit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;

public class CatalogEntry implements Comparable<CatalogEntry>
{
  final List<BlockPointerList> blockPointerLists = new ArrayList<> ();
  final List<String> lines = new ArrayList<> ();
  int lrecl;
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

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CatalogEntry (byte[] buffer, int ptr, int lrecl)
  {
    name = Utility.getString (buffer, ptr, 8);
    blockFrom = (int) Utility.getValue (buffer, ptr + 8, 3);
    this.lrecl = lrecl;

    extra = buffer[ptr + 11] & 0xFF;

    switch (extra)
    {
      case 0x0F:
        basic (buffer, ptr);
        break;

      case 0x14:
        basic (buffer, ptr);
        break;

      case 0x2B:
        break;

      case 0x2C:                    // load module?
        break;

      case 0x2E:
        break;

      case 0x31:                    // load module?
        dateCreated = Utility.getLocalDate (buffer, ptr + 34);
        break;

      case 0x37:
        break;

      case 0x8F:
        basic (buffer, ptr);       // alias without the alias' name ??
        break;

      case 0xB1:                    // alias of 0x2C
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0xB3:
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0x36:      // file242    // 0xB6 alias of itself?
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0xB6:      // file242    // alias of 0x31
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        dateCreated = Utility.getLocalDate (buffer, ptr + 44);
        break;

      case 0xD3:
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0x4E:
        break;

      case 0xCB:
        break;

      case 0x4B:
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
    if (extra == 0x31)
      hex =
          Utility.getHexValues (directoryData, 12, 22) + "                              "
              + Utility.getHexValues (directoryData, 34, 12);
    else
      hex = Utility.getHexValues (directoryData, 12, directoryData.length - 12);

    if (extra == 0xB6)
      t1 = Utility.getString (directoryData, 48, 8);

    return String.format ("%02X %-8s %06X %-129s %8s %8s %8s", directoryData[11],
        getMemberName (), blockFrom, hex, getUserName (), getAliasName (), t1);
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

    dateCreated = Utility.getLocalDate (buffer, offset + 16);
    dateModified = Utility.getLocalDate (buffer, offset + 20);
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
  // setPdse
  // ---------------------------------------------------------------------------------//

  void setPdse (boolean value)
  {
    isPdse = value;
  }

  // ---------------------------------------------------------------------------------//
  // addBlockPointerList
  // ---------------------------------------------------------------------------------//

  boolean addBlockPointerList (BlockPointerList blockPointerList)
  {
    if (blockPointerLists.size () == 0)
      if (!blockPointerList.sortKeyMatches (directoryData[10]))
      {
        System.out.println ("Mismatch in " + name);
        return false;
      }

    blockPointerLists.add (blockPointerList);
    dataLength += blockPointerList.getDataLength ();

    blockPointerList.setCatalogEntry (this);
    return true;
  }

  // ---------------------------------------------------------------------------------//
  // getLines
  // ---------------------------------------------------------------------------------//

  public String getLines (boolean showLines)
  {
    if (lines.size () == 0)
    {
      if (isAlias ())
        lines.add ("Alias of " + aliasName);
      else if (blockPointerLists.size () == 0)
        lines.add ("No data");
      else if (isXmit ())
        xmitList ();
      else if (blockPointerLists.size () > 200)
        partialDump (100);      // slow!!
      else if (isRdw ())        // slow!!
        rdw ();
      else if (blockPointerLists.get (0).isBinary ())
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
      for (BlockPointerList blockPointerList : blockPointerLists)
      {
        dataLength += blockPointerList.getDataLength ();
        if (blockPointerList.isLastBlock ())        // PDSEs end early
          break;
      }
    }

    byte[] dataBuffer = new byte[dataLength];
    int ptr = 0;

    for (BlockPointerList blockPointerList : blockPointerLists)
    {
      ptr = blockPointerList.getDataBuffer (dataBuffer, ptr);
      if (blockPointerList.isLastBlock ())        // PDSEs end early
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

    int count = 0;
    for (BlockPointerList blockPointerList : blockPointerLists)
    {
      text.append (String.format (
          "-----------------------< BlockPointerList %d of %d >-----------------------\n",
          ++count, blockPointerLists.size ()));

      text.append (blockPointerList.listHeaders ());
      text.append ("\n\n");
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
      int len = Math.min (lrecl, length);
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
    if (blockPointerLists.size () == 0)
      return;

    if (blockPointerLists.get (0).isXmit ())
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
    if (blockPointerLists.size () == 0)
      return false;

    for (BlockPointerList bpl : blockPointerLists)
    {
      byte[] buffer = bpl.getDataBuffer ();
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
    for (BlockPointerList bpl : blockPointerLists)
    {
      byte[] buffer = bpl.getDataBuffer ();
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

  boolean isXmit ()
  {
    return blockPointerLists.size () > 0 && blockPointerLists.get (0).isXmit ();
  }

  // ---------------------------------------------------------------------------------//
  // xmitList
  // ---------------------------------------------------------------------------------//

  void xmitList ()
  {
    byte[] xmitBuffer = getDataBuffer ();
    try
    {
      Reader reader = new Reader (xmitBuffer);

      for (ControlRecord controlRecord : reader.getControlRecords ())
        lines.add (String.format ("%s", controlRecord));

      Dataset dataset = reader.getCrappoCurrentDataset ();
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
    lines.add (toString ());
    lines.add ("");
    lines.add ("Data too large to display");
    lines.add ("");
    lines.add ("Showing first " + max + " of " + blockPointerLists.size () + " buffers");
    lines.add ("");

    if (blockPointerLists.get (0).isXmit ())
      lines.add ("Appears to be XMIT");

    for (int i = 0; i < max; i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (bpl.getDataLength () > 0)
        createDataLines (bpl.getDataBuffer ());
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
    return String.format ("%8s  %8s  %,6d  %06X  %s  %s  %8s", name, userName, size,
        blockFrom, date1Text, time, aliasName);
  }

  // ---------------------------------------------------------------------------------//
  // compareTo
  // ---------------------------------------------------------------------------------//

  @Override
  public int compareTo (CatalogEntry o)
  {
    if (this.blockFrom == o.blockFrom)
    {
      if (!this.isAlias () && o.isAlias ())
        return -1;
      if (!o.isAlias () && this.isAlias ())
        return 1;
      return this.name.compareTo (o.name);
    }

    return blockFrom < o.blockFrom ? -1 : 1;
  }
}
