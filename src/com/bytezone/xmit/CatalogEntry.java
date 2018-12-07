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
  private byte[] data;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CatalogEntry (byte[] buffer, int offset)
  {
    memberName = Reader.getString (buffer, offset, 8);

    if (buffer[offset + 11] == 0x0F)
    {
      userName = Reader.getString (buffer, offset + 32, 8);

      size1 = Reader.getWord (buffer, offset + 26);
      size2 = Reader.getWord (buffer, offset + 28);
      size3 = Reader.getWord (buffer, offset + 30);
      lines = new ArrayList<> (size1);

      data = new byte[42];
    }
    else
    {
      userName = "";
      lines = new ArrayList<> ();
      data = new byte[12];
    }
    System.arraycopy (buffer, offset, data, 0, data.length);
  }

  // ---------------------------------------------------------------------------------//
  // length
  // ---------------------------------------------------------------------------------//

  int length ()
  {
    return data.length;
  }

  // ---------------------------------------------------------------------------------//
  // getMemberName
  // ---------------------------------------------------------------------------------//

  public String getMemberName ()
  {
    return memberName;
  }

  public String getText ()
  {
    StringBuilder text = new StringBuilder ();
    int lineNo = 0;
    for (String line : lines)
      text.append (String.format ("%05d0 %s%n", ++lineNo, line));
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  //
  // ---------------------------------------------------------------------------------//

  void addBlock (byte[] buffer)
  {
    int ptr = 12;
    int size = buffer.length / 80;
    int dataLength = Reader.getWord (buffer, 10);

    while (size-- > 0)
    {
      lines.add (Reader.getString (buffer, ptr, 80));
      ptr += 80;
    }
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
    return String.format ("%-126s %8s %8s %5d %5d %5d", Reader.getHexString (data),
        memberName, userName, size1, size2, size3);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%8s  %8s  %,5d  %,5d", memberName, userName, size1,
        lines.size ());
  }
}
