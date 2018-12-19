package com.bytezone.xmit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CatalogEntry implements Comparable<CatalogEntry>
{
  private final String memberName;
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

  private final List<String> lines = new ArrayList<> ();
  private final byte[] directoryData;
  private final List<BlockPointerList> blockPointerLists = new ArrayList<> ();

  private int dataLength;

  private final LogicalBuffer logicalBuffer = new LogicalBuffer ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CatalogEntry (byte[] buffer, int ptr)
  {
    memberName = Reader.getString (buffer, ptr, 8);
    blockFrom = (int) Utility.getValue (buffer, ptr + 8, 3);

    int extra = buffer[ptr + 11] & 0xFF;
    int extraLength = 12 + (extra & 0x0F) * 2 + ((extra & 0x10) >> 4) * 32;
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

      case 0x2C:
        break;

      case 0x2E:
        break;

      case 0x31:
        break;

      case 0x37:
        break;

      case 0x8F:
        basic (buffer, ptr);       // alias without the alias' name ??
        break;

      case 0xB1:
        aliasName = Reader.getString (buffer, ptr + 36, 8);
        break;

      case 0xB3:
        aliasName = Reader.getString (buffer, ptr + 36, 8);
        break;

      case 0:
        break;

      default:
        System.out.printf ("********************** Unknown extra: %02X in %s%n", extra,
            memberName);
    }

    directoryData = new byte[extraLength];
    System.arraycopy (buffer, ptr, directoryData, 0, directoryData.length);

    if (false)
      System.out.printf ("%02X %-8s %06X %-129s %8s %8s%n", extra, getMemberName (),
          blockFrom, Reader.getHexString (buffer, ptr + 12, length () - 12),
          getUserName (), getAliasName ());
  }

  // ---------------------------------------------------------------------------------//
  // basic
  // ---------------------------------------------------------------------------------//

  private void basic (byte[] buffer, int offset)
  {
    userName = Reader.getString (buffer, offset + 32, 8);
    size = Reader.getWord (buffer, offset + 26);
    init = Reader.getWord (buffer, offset + 28);
    mod = Reader.getWord (buffer, offset + 30);

    vv = buffer[offset + 12] & 0xFF;
    mm = buffer[offset + 13] & 0xFF;

    dateCreated = getLocalDate (buffer, offset + 16);
    dateModified = getLocalDate (buffer, offset + 20);
    time = String.format ("%02X:%02X:%02X", buffer[offset + 24], buffer[offset + 25],
        buffer[offset + 15]);

    if (false)
    {
      String vvmmText = String.format ("%02d.%02d", vv, mm);
      String date1Text = String.format ("%td %<tb %<tY", dateCreated).replace (".", "");
      String date2Text = String.format ("%td %<tb %<tY", dateModified).replace (".", "");
      System.out.println (String.format ("%-8s  %6d  %6d %4d  %13s  %13s  %s  %5s  %s",
          memberName, size, init, mod, date1Text, date2Text, time, vvmmText, userName));
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
    return memberName;
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
  // getBufferLength
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
  // addBlockPointerList
  // ---------------------------------------------------------------------------------//

  boolean addBlockPointerList (BlockPointerList blockPointerList)
  {
    if (blockPointerLists.size () == 0
        && !blockPointerList.sortKeyMatches (directoryData[0 + 10]))
    {
      System.out.println ("Mismatch in " + memberName);
      return false;
    }

    logicalBuffer.addBlockPointerList (blockPointerList);

    blockPointerLists.add (blockPointerList);
    dataLength += blockPointerList.getDataLength ();

    blockPointerList.setCatalogEntry (this);
    return true;
  }

  // ---------------------------------------------------------------------------------//
  // getText
  // ---------------------------------------------------------------------------------//

  public String getText ()
  {
    if (lines.size () == 0)
    {
      if (isAlias ())
        return "Alias of " + aliasName;
      if (blockPointerLists.size () == 0)
        return "No data";

      if (blockPointerLists.get (0).isXmit ())
        return xmitList ();

      if (blockPointerLists.size () > 200)
        return partialDump ();
      if (blockPointerLists.get (0).isBinary ())
        return hexDump ();

      for (BlockPointerList blockPointerList : blockPointerLists)
        //        createLines (blockPointerList);
        createDataLines (blockPointerList);
    }

    StringBuilder text = new StringBuilder ();
    int lineNo = 0;
    for (String line : lines)
      text.append (String.format ("%05d0 %s%n", ++lineNo, line));
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // createDataLines
  // ---------------------------------------------------------------------------------//

  private void createDataLines (BlockPointerList blockPointerList)
  {
    byte[] buffer = blockPointerList.getDataBuffer ();

    int ptr = 0;
    int length = buffer.length;
    while (length > 0)
    {
      int len = Math.min (80, length);
      lines.add (Reader.getString (buffer, ptr, len));
      ptr += len;
      length -= len;
    }
  }

  // ---------------------------------------------------------------------------------//
  // createLines
  // ---------------------------------------------------------------------------------//

  // this should be able to build lines directly from the original buffer
  //  private void createLines (BlockPointerList blockPointerList)
  //  {
  //    byte[] buffer = blockPointerList.getBuffer ();
  //    int dataLength = Reader.getWord (buffer, 10);       // bpl.dataLength
  //
  //    int remainder = buffer.length - dataLength;
  //    if (remainder != 12 && remainder != 24)
  //      System.out.printf ("Unexpected remainder in %s: %d", memberName, remainder);
  //
  //    int ptr = 12;               // header
  //    while (dataLength > 0)
  //    {
  //      int len = Math.min (80, dataLength);
  //      lines.add (Reader.getString (buffer, ptr, len));
  //      ptr += len;
  //      dataLength -= len;
  //    }
  //  }

  // ---------------------------------------------------------------------------------//
  // xmitList
  // ---------------------------------------------------------------------------------//

  private byte[] getXmitBufferOld ()
  {
    byte[] xmitBuffer = new byte[dataLength];
    int fullPtr = 0;
    for (BlockPointerList blockPointerList : blockPointerLists)
    {
      byte[] data = blockPointerList.getBuffer ();
      int ptr = 0;

      while (ptr < data.length)
      {
        int dataLength = Reader.getWord (data, ptr + 10);
        System.arraycopy (data, ptr + 12, xmitBuffer, fullPtr, dataLength);
        fullPtr += dataLength;
        ptr += 12 + dataLength;
      }
    }
    assert fullPtr == dataLength;
    return xmitBuffer;
  }

  // ---------------------------------------------------------------------------------//
  // getXmitBuffer
  // ---------------------------------------------------------------------------------//

  private byte[] getXmitBuffer ()
  {
    byte[] xmitBuffer = new byte[dataLength];
    int ptr = 0;
    for (BlockPointerList blockPointerList : blockPointerLists)
    {
      byte[] dataBuffer = blockPointerList.getDataBuffer ();
      System.arraycopy (dataBuffer, 0, xmitBuffer, ptr, dataBuffer.length);
      ptr += dataBuffer.length;
    }
    assert ptr == dataLength;
    return xmitBuffer;
  }

  // ---------------------------------------------------------------------------------//
  // xmitList
  // ---------------------------------------------------------------------------------//

  private String xmitList ()
  {
    StringBuilder text = new StringBuilder ();
    text.append ("XMIT file:\n\n");
    byte[] xmitBuffer = getXmitBuffer ();
    try
    {
      Reader reader = new Reader (xmitBuffer);
      for (ControlRecord controlRecord : reader.controlRecords)
      {
        text.append (controlRecord);
        text.append ("\n");
      }
      text.append (String.format ("Members: %s%n%n", reader.catalogEntries.size ()));
      text.append (" Member     User     Alias      Size     Date       Time\n");
      text.append ("--------  --------  --------  ------  -----------  --------\n");
      for (CatalogEntry catalogEntry : reader.catalogEntries)
        text.append (catalogEntry.toString () + "\n");
      text.deleteCharAt (text.length () - 1);
    }
    catch (Exception e)
    {
      text.append ("Data length: " + dataLength + "\n");
      text.append (e.getMessage ());
      text.append ("\n\n");
      text.append (Utility.toHex (xmitBuffer));
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // hexDump
  // ---------------------------------------------------------------------------------//

  private String hexDump ()
  {
    StringBuilder text = new StringBuilder ();

    if (blockPointerLists.get (0).isXmit ())
      text.append ("Appears to be XMIT\n\n");

    for (int i = 0; i < blockPointerLists.size (); i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (bpl.getDataLength () > 0)
      {
        //        byte[] buffer = bpl.getBuffer ();
        //        int length = Reader.getWord (buffer, 10);
        //        text.append (Utility.toHex (buffer, 12, length));
        text.append (Utility.toHex (bpl.getDataBuffer ()));
        text.append ("\n\n");
      }
    }

    if (text.length () > 2)
    {
      text.deleteCharAt (text.length () - 1);
      text.deleteCharAt (text.length () - 1);
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // partialDump
  // ---------------------------------------------------------------------------------//

  private String partialDump ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (toString ());
    text.append ("\n\n");
    text.append ("Member data too large to display\n");
    int max = 5;
    text.append (
        "Showing first " + max + " of " + blockPointerLists.size () + " buffers\n\n");

    if (blockPointerLists.get (0).isXmit ())
      text.append ("Appears to be XMIT\n\n");

    //    for (int i = 0; i < max; i++)
    //    {
    //      BlockPointerList bpl = blockPointerLists.get (i);
    //      if (bpl.getDataLength () > 0)
    //      {
    //        byte[] buffer = bpl.getBuffer ();
    //        int length = Reader.getWord (buffer, 10);
    //        text.append (Utility.toHex (buffer, 12, length));
    //        if (i < max - 1)
    //          text.append ("\n\n");
    //      }
    //    }
    for (int i = 0; i < max; i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (bpl.getDataLength () > 0)
      {
        text.append (Utility.toHex (bpl.getDataBuffer ()));
        if (i < max - 1)
          text.append ("\n\n");
      }
    }

    return text.toString ();
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

    text.deleteCharAt (text.length () - 1);
    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // printLine
  // ---------------------------------------------------------------------------------//

  String getPrintLine ()
  {
    return String.format ("%-126s %8s %8s %5d %5d %5d",
        Reader.getHexString (directoryData), memberName, userName, size, init, mod);
  }

  // ---------------------------------------------------------------------------------//
  // getLocalDate
  // ---------------------------------------------------------------------------------//

  private LocalDate getLocalDate (byte[] buffer, int offset)
  {
    String date1 = String.format ("%02X%02X%02X%02X", buffer[offset], buffer[offset + 1],
        buffer[offset + 2], (buffer[offset + 3] & 0xF0));
    int d1 = Integer.parseInt (date1) / 10;
    return LocalDate.ofYearDay (1900 + d1 / 1000, d1 % 1000);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    String date1Text = dateCreated == null ? ""
        : String.format ("%td %<tb %<tY", dateCreated).replace (".", "");
    return String.format ("%8s  %8s  %8s  %,6d  %s  %s", memberName, userName, aliasName,
        size, date1Text, time);
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
      return this.memberName.compareTo (o.memberName);
    }

    return blockFrom < o.blockFrom ? -1 : 1;
  }
}
