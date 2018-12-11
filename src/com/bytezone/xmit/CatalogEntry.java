package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class CatalogEntry
{
  private static String line = "====== ---------+---------+---------+---------+"
      + "---------+---------+---------+---------+";

  private final String memberName;
  private final String userName;
  private int size1;
  private int size2;
  private int size3;
  private List<String> lines;
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
        lines = new ArrayList<> (size1);
        directoryData = new byte[42];
        System.arraycopy (buffer, offset, directoryData, 0, directoryData.length);
        break;

      case 0x14:
        userName = Reader.getString (buffer, offset + 32, 8);
        directoryData = new byte[52];
        System.arraycopy (buffer, offset, directoryData, 0, directoryData.length);
        break;

      case 0x2C:
        directoryData = new byte[36];
        System.arraycopy (buffer, offset, directoryData, 0, directoryData.length);
        userName = "";
        break;

      case 0x2E:
        directoryData = new byte[40];
        System.arraycopy (buffer, offset, directoryData, 0, directoryData.length);
        userName = "";
        break;

      case 0xB3:
        directoryData = new byte[18];
        System.arraycopy (buffer, offset, directoryData, 0, directoryData.length);
        userName = "";
        break;

      case 0:
        userName = "";
        lines = new ArrayList<> ();
        directoryData = new byte[12];
        System.arraycopy (buffer, offset, directoryData, 0, directoryData.length);
        break;

      default:
        System.out.println ("********************** Unknown extra length: " + extra);
        userName = "";
        directoryData = new byte[12];
        System.arraycopy (buffer, offset, directoryData, 0, directoryData.length);
    }

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
      if (blockPointerLists.size () > 200)
        return partialDump ();

      for (BlockPointerList blockPointerList : blockPointerLists)
      {
        byte[] buffer = blockPointerList.getBuffer ();
        addLines (buffer);
      }
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
  private void addLines (byte[] buffer)
  {
    int ptr = 12;
    int totLines = buffer.length / 80;
    int dataLength = Reader.getWord (buffer, 10);
    System.out.println (Utility.toHex (buffer, 0, 12));
    System.out.printf ("Buffer length: %d  Data length: %d%n", buffer.length, dataLength);
    assert dataLength == totLines * 80;

    while (totLines-- > 0)
    {
      lines.add (Reader.getString (buffer, ptr, 80));
      ptr += 80;
    }
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
