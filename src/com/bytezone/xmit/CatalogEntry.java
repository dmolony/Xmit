package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class CatalogEntry
{
  String name;
  String owner;
  int size;
  List<String> lines;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CatalogEntry (byte[] buffer, int offset)
  {
    name = Reader.getString (buffer, offset, 8);
    owner = Reader.getString (buffer, offset + 32, 8);
    size = Reader.getWord (buffer, offset + 26);
    lines = new ArrayList<> (size);
  }

  // ---------------------------------------------------------------------------------//
  // 
  // ---------------------------------------------------------------------------------//

  void addBlock (byte[] buffer)
  {
    int ptr = 12;
    Reader.printHex (buffer, 0, 12);
    while (ptr < buffer.length)
    {
      int len = Integer.min (80, buffer.length - ptr);
      String line = Reader.getString (buffer, ptr, len);
      if (lines.size () < size)
        lines.add (line);
      else
        Reader.printHex (buffer, ptr, len);
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
    System.out.println ();
    System.out.println (name);
    for (String line : lines)
      System.out.println (line);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%8s  %8s  %,5d  %,5d", name, owner, size, lines.size ());
  }
}
