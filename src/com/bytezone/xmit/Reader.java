package com.bytezone.xmit;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.common.Utility;

public class Reader
{
  List<ControlRecord> controlRecords = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Reader (byte[] buffer)
  {
    List<byte[]> blocks = new ArrayList<> ();
    int blockLength = 0;
    int totalLength = 0;
    int dataSegments = 0;

    int ptr = 0;
    while (ptr < buffer.length)
    {
      int length = buffer[ptr] & 0xFF;
      byte flags = buffer[ptr + 1];

      boolean firstSegment = (flags & 0x80) != 0;
      boolean lastSegment = (flags & 0x40) != 0;
      boolean controlRecord = (flags & 0x20) != 0;
      boolean recordNumber = (flags & 0x10) != 0;

      if (false)
        System.out.printf ("%08X  %02X   %s %s %s %s%n", ptr, length,
            firstSegment ? "x" : " ", lastSegment ? "x" : " ", controlRecord ? "x" : " ",
            recordNumber ? "x" : " ");

      if (controlRecord)
      {
        ControlRecord cr = new ControlRecord (buffer, ptr + 2, length - 2);
        controlRecords.add (cr);
        if (cr.name.equals ("INMR06"))
          break;
      }
      else
      {
        if (firstSegment)
        {
          blocks.clear ();
          blockLength = 0;
        }

        byte[] block = new byte[length - 2];
        System.arraycopy (buffer, ptr + 2, block, 0, length - 2);
        blocks.add (block);
        blockLength += length - 2;

        if (lastSegment)
        {
          byte[] fullBlock = consolidate (blocks, blockLength);

          totalLength += blockLength;
          ++dataSegments;

          if (false)
          {
            System.out.println ();
            System.out.println (
                Utility.toHex (fullBlock, 0, fullBlock.length, Utility.EBCDIC, 0));
          }

          if (false)
          {
            if (dataSegments <= 2)              // presumably info about the file layout
              ;
            else if (dataSegments <= 6)         // 4 directory blocks in FILE069
              printDirectory (fullBlock);
            else                                // rest is data
              printData (fullBlock);
          }
        }
      }

      ptr += length;
    }

    System.out.printf ("%nData segments :     %04X  %<,10d%n", dataSegments);
    System.out.printf ("Data size     : %08X  %<,10d%n", totalLength);
    //    TextUnit.dump ();
  }

  // ---------------------------------------------------------------------------------//
  // consolidate
  // ---------------------------------------------------------------------------------//

  byte[] consolidate (List<byte[]> blocks, int blockLength)
  {
    byte[] fullBlock = new byte[blockLength];
    int ptr = 0;
    for (byte[] block : blocks)
    {
      System.arraycopy (block, 0, fullBlock, ptr, block.length);
      ptr += block.length;
    }
    assert ptr == blockLength;
    return fullBlock;
  }

  // ---------------------------------------------------------------------------------//
  // printDirectory
  // ---------------------------------------------------------------------------------//

  void printDirectory (byte[] buffer)
  {
    int ptr = 22;
    System.out.println ();

    while (ptr + 42 < buffer.length)
    {
      if (buffer[ptr] == (byte) 0xFF)
        break;
      if (buffer[ptr] == 0)
      {
        ptr += 24;
        continue;
      }
      System.out.println (Utility.toHex (buffer, ptr, 16, Utility.EBCDIC, 0));
      ptr += 42;
    }
  }

  // ---------------------------------------------------------------------------------//
  // printData
  // ---------------------------------------------------------------------------------//

  void printData (byte[] buffer)
  {
    int ptr = 12;
    System.out.println ();
    while (ptr < buffer.length)
    {
      int len = Integer.min (80, buffer.length - ptr);
      System.out.println (Reader.getString (buffer, ptr, len));
      ptr += len;
    }
  }

  // ---------------------------------------------------------------------------------//
  // getString
  // ---------------------------------------------------------------------------------//

  static String getString (byte[] buffer, int ptr, int length)
  {
    try
    {
      return new String (buffer, ptr, length, Utility.EBCDIC);
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
    return "";
  }

  // ---------------------------------------------------------------------------------//
  // getWord
  // ---------------------------------------------------------------------------------//

  static int getWord (byte[] buffer, int ptr)
  {
    int b = (buffer[ptr] & 0xFF) << 8;
    int a = (buffer[ptr + 1] & 0xFF);
    return a + b;
  }

  // ---------------------------------------------------------------------------------//
  // getDouble
  // ---------------------------------------------------------------------------------//

  static int getDouble (byte[] buffer, int ptr)
  {
    int a = getWord (buffer, ptr) << 16;
    int b = getWord (buffer, ptr + 2);
    return a + b;
  }
}
