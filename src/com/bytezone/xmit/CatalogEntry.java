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
  List<String> lines;
  byte[] data = new byte[24];

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CatalogEntry (byte[] buffer, int offset)
  {
    memberName = Reader.getString (buffer, offset, 8);
    userName = Reader.getString (buffer, offset + 32, 8);

    size = Reader.getWord (buffer, offset + 26);
    lines = new ArrayList<> (size);

    System.arraycopy (buffer, offset + 8, data, 0, 24);
  }

  // ---------------------------------------------------------------------------------//
  // 
  // ---------------------------------------------------------------------------------//

  void addBlock (byte[] buffer)
  {
    int ptr = 12;
    //    Reader.printHex (buffer, 0, 12);
    while (ptr < buffer.length)
    {
      int len = Integer.min (80, buffer.length - ptr);
      String line = Reader.getString (buffer, ptr, len);
      if (lines.size () < size)
        lines.add (line);
      else
      {
        Reader.printHex (buffer, ptr, len);
        System.out.println (((len - 1) / 80 + 1) + " extra lines");
      }
      ptr += len;
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
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%8s  %8s  %,5d  %,5d", memberName, userName, size,
        lines.size ());
  }
}
