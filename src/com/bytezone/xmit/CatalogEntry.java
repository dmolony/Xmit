package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class CatalogEntry
{
  private static String line = "====== ---------+---------+---------+---------+"
      + "---------+---------+---------+---------+";

  private final String memberName;
  private String userName = "";
  private String aliasName = "";

  private int size1;
  private int size2;
  private int size3;

  private final List<String> lines = new ArrayList<> ();
  private final byte[] directoryData;
  private final List<BlockPointerList> blockPointerLists = new ArrayList<> ();

  private long bufferLength;
  private long dataLength;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CatalogEntry (byte[] buffer, int offset)
  {
    memberName = Reader.getString (buffer, offset, 8);
    //    System.out.println (Utility.getHex (buffer, offset, 42));
    int extra = buffer[offset + 11] & 0xFF;
    //    assert extra == 0 || extra == 15 || extra == 20 : "Extra " + extra;

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
        System.out.println ("********************** Unknown extra: " + extra);
        directoryData = new byte[12];
    }
    System.arraycopy (buffer, offset, directoryData, 0, directoryData.length);

    if (true)
      System.out.printf ("%-129s %s %s%n",
          Reader.getHexString (buffer, offset, length ()), getMemberName (),
          getUserName ());
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
  //addBlock
  // ---------------------------------------------------------------------------------//

  void addBlockPointerList (BlockPointerList blockPointerList)
  {
    this.blockPointerLists.add (blockPointerList);
    bufferLength += blockPointerList.getBufferLength ();
    dataLength += blockPointerList.getDataLength ();
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

    //    System.out.println (Utility.toHex (buffer));

    //    System.out.println (memberName);
    //    System.out.println (Utility.toHex (buffer, 0, 12));
    //    System.out.printf ("Buffer length: %d  Data length: %d%n", buffer.length, dataLength);
    //    assert dataLength == totLines * 80;

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
    return String.format ("%8s  %8s  %,9d  %,9d", memberName, userName, bufferLength,
        dataLength);
  }
}
