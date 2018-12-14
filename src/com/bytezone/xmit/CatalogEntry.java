package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class CatalogEntry implements Comparable<CatalogEntry>
{
  private static String line = "====== ---------+---------+---------+---------+"
      + "---------+---------+---------+---------+";

  private final String memberName;
  private String userName = "";
  private String aliasName = "";

  private int size1;
  private int size2;
  private int size3;

  final long blockFrom;
  final long blockTo;

  private final List<String> lines = new ArrayList<> ();
  private final byte[] directoryData;
  private final List<BlockPointerList> blockPointerLists = new ArrayList<> ();

  private long bufferLength;
  private long dataLength;

  private final LogicalBuffer logicalBuffer = new LogicalBuffer ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CatalogEntry (byte[] buffer, int offset)
  {
    memberName = Reader.getString (buffer, offset, 8);
    blockFrom = Utility.getValue (buffer, offset + 8, 3);
    blockTo = Utility.getValue (buffer, offset + 12, 3);

    int extra = buffer[offset + 11] & 0xFF;
    switch (extra)
    {
      case 0x0F:
        userName = Reader.getString (buffer, offset + 32, 8);
        size1 = Reader.getWord (buffer, offset + 26);
        size2 = Reader.getWord (buffer, offset + 28);
        size3 = Reader.getWord (buffer, offset + 30);
        directoryData = new byte[42];
        break;

      case 0x14:
        userName = Reader.getString (buffer, offset + 32, 8);
        directoryData = new byte[52];
        break;

      case 0x2C:
        directoryData = new byte[36];
        break;

      case 0x2E:
        directoryData = new byte[40];
        break;

      case 0x37:
        directoryData = new byte[58];
        break;

      case 0xB1:                // alias
        aliasName = Reader.getString (buffer, offset + 36, 8);
        directoryData = new byte[46];       // not tested yet
        break;

      case 0xB3:                // alias
        aliasName = Reader.getString (buffer, offset + 36, 8);
        directoryData = new byte[50];
        break;

      case 0:
        directoryData = new byte[12];
        break;

      default:
        System.out.printf ("********************** Unknown extra: %02X%n", extra);
        directoryData = new byte[12];
    }
    System.arraycopy (buffer, offset, directoryData, 0, directoryData.length);

    if (true)
      System.out.printf ("%02X %-8s %06X %06X %-129s %8s %8s%n", extra, getMemberName (),
          blockFrom, blockTo, Reader.getHexString (buffer, offset + 15, length () - 15),
          getUserName (), getAliasName ());
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
  // getBufferLength
  // ---------------------------------------------------------------------------------//

  public long getBufferLength ()
  {
    return bufferLength;
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

  void addBlockPointerList (BlockPointerList blockPointerList)
  {
    logicalBuffer.addBlockPointerList (blockPointerList);

    this.blockPointerLists.add (blockPointerList);
    bufferLength += blockPointerList.getBufferLength ();
    dataLength += blockPointerList.getDataLength ();
  }

  // ---------------------------------------------------------------------------------//
  // getText
  // ---------------------------------------------------------------------------------//

  public String getText ()
  {
    //    logicalBuffer.walk ();

    if (lines.size () == 0)
    {
      if (isAlias ())
        return "Alias of " + aliasName;
      if (blockPointerLists.size () == 0)
        return "No data";
      if (blockPointerLists.size () > 200)
        return partialDump ();
      if (blockPointerLists.get (0).isBinary ())
        return hexDump ();

      //      for (BlockPointerList blockPointerList : blockPointerLists)
      //        System.out.println (Utility.toHex (blockPointerList.getBuffer ()));
      for (BlockPointerList blockPointerList : blockPointerLists)
        addLines (blockPointerList);
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
  // addLines
  // ---------------------------------------------------------------------------------//

  // this should be able to build lines directly from the original buffer
  private void addLines (BlockPointerList blockPointerList)
  {
    byte[] buffer = blockPointerList.getBuffer ();
    int dataLength = Reader.getWord (buffer, 10);       // bpl.dataLength
    int remainder = buffer.length - dataLength;
    if (remainder != 12 && remainder != 24)
      System.out.printf ("Unexpected remainder in %s: %d", memberName, remainder);

    int ptr = 12;
    while (dataLength > 0)
    {
      int len = Math.min (80, dataLength);
      lines.add (Reader.getString (buffer, ptr, len));
      ptr += len;
      dataLength -= len;
    }
  }

  private String hexDump ()
  {
    StringBuilder text = new StringBuilder ();

    for (int i = 0; i < blockPointerLists.size (); i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (bpl.getDataLength () > 0)
      {
        byte[] buffer = bpl.getBuffer ();
        int length = Reader.getWord (buffer, 10);
        text.append (Utility.toHex (buffer, 12, length));
        text.append ("\n\n");
      }
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
    for (int i = 0; i < max; i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (bpl.getDataLength () > 0)
      {
        byte[] buffer = bpl.getBuffer ();
        int length = Reader.getWord (buffer, 10);
        text.append (Utility.toHex (buffer, 12, length));
        if (i < max - 1)
          text.append ("\n\n");
      }
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // list
  // ---------------------------------------------------------------------------------//

  public void list ()
  {
    System.out.println (line);
    System.out.printf ("Member : %s%n", memberName);
    System.out.printf ("User   : %s%n", userName);
    System.out.println (line);

    int lineNo = 0;
    for (String line : lines)
      System.out.printf ("%05d0 %s%n", ++lineNo, line);
    System.out.println (line);
  }

  // ---------------------------------------------------------------------------------//
  // printLine
  // ---------------------------------------------------------------------------------//

  String getPrintLine ()
  {
    return String.format ("%-126s %8s %8s %5d %5d %5d",
        Reader.getHexString (directoryData), memberName, userName, size1, size2, size3);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%8s  %8s  %8s  %06X  %06X", memberName, userName, aliasName,
        blockFrom, blockTo);
  }

  // ---------------------------------------------------------------------------------//
  // compareTo
  // ---------------------------------------------------------------------------------//

  @Override
  public int compareTo (CatalogEntry o)
  {
    if (this.blockFrom == o.blockFrom)
      return this.memberName.compareTo (o.memberName);
    return blockFrom < o.blockFrom ? -1 : 1;
  }
}
