package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class CatalogEntry
{
  static String line = "====== ---------+---------+---------+---------+"
      + "---------+---------+---------+---------+";
  String memberName;
  String userName;
  int size;
  int size2;
  int size3;
  List<String> lines;
  byte[] data;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CatalogEntry (byte[] buffer, int offset)
  {
    memberName = Reader.getString (buffer, offset, 8);

    if (buffer[offset + 11] == 0x0F)
    {
      userName = Reader.getString (buffer, offset + 32, 8);

      size = Reader.getWord (buffer, offset + 26);
      size2 = Reader.getWord (buffer, offset + 28);
      size3 = Reader.getWord (buffer, offset + 30);
      lines = new ArrayList<> (size);

      data = new byte[42];
      System.arraycopy (buffer, offset, data, 0, data.length);
    }
    else
    {
      userName = "";
      lines = new ArrayList<> ();
      data = new byte[12];
      System.arraycopy (buffer, offset, data, 0, data.length);
    }
  }

  // ---------------------------------------------------------------------------------//
  // length
  // ---------------------------------------------------------------------------------//

  int length ()
  {
    return data.length;
  }

  // ---------------------------------------------------------------------------------//
  // 
  // ---------------------------------------------------------------------------------//

  void addBlock (byte[] buffer)
  {
    int ptr = 12;
    int size = buffer.length / 80;
    int dataLength = Reader.getWord (buffer, 10);
    //    Reader.printHex (buffer, 0, 12);
    //    while (ptr < buffer.length)
    while (size-- > 0)
    {
      //      int len = Integer.min (80, buffer.length - ptr);
      String line = Reader.getString (buffer, ptr, 80);
      //      if (lines.size () < size)
      lines.add (line);
      //      else
      //      {
      //        Reader.printHex (buffer, ptr, len);
      //        System.out.printf ("%s %s %n", memberName, line);
      //      }
      ptr += 80;
    }
  }

  // ---------------------------------------------------------------------------------//
  // isComplete
  // ---------------------------------------------------------------------------------//

  boolean isComplete ()
  {
    return lines.size () >= size;
  }

  // ---------------------------------------------------------------------------------//
  // list
  // ---------------------------------------------------------------------------------//

  public void list ()
  {
    System.out.println (line);
    System.out.printf ("Member : %s%n", memberName);
    System.out.printf ("User   : %s%n", userName);
    System.out.printf ("Data   : %s%n", Reader.getHexString (data, 0, data.length));
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
    //    String hexText =
    //        data.length == 42 ? Reader.getHexString (data) : Reader.getHexString (data);

    return String.format ("%-126s %8s %8s %5d %5d %5d", Reader.getHexString (data),
        memberName, userName, size, size2, size3);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%8s  %8s  %,5d  %,5d", memberName, userName, size,
        lines.size ());
  }
}
