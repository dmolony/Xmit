package com.bytezone.xmit;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.common.Utility;

public class Reader
{
  private static String[] format = { "?", "V", "F", "U" };

  List<ControlRecord> controlRecords = new ArrayList<> ();
  List<CatalogEntry> catalogEntries = new ArrayList<> ();
  List<byte[]> dataBlocks = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Reader (byte[] buffer)
  {
    List<byte[]> blocks = new ArrayList<> ();
    int totalBlocks = 0;
    int currentEntry = 0;
    boolean inCatalog = true;

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
          blocks.clear ();

        byte[] block = new byte[length - 2];
        System.arraycopy (buffer, ptr + 2, block, 0, length - 2);
        blocks.add (block);

        if (lastSegment)
        {
          totalBlocks += blocks.size ();
          byte[] fullBlock = consolidate (blocks);
          dataBlocks.add (fullBlock);

          if (false)
          {
            System.out.println ();
            System.out.println (
                Utility.toHex (fullBlock, 0, fullBlock.length, Utility.EBCDIC, 0));
          }

          if (dataBlocks.size () == 1)
          {
            printHex (fullBlock);
            System.out.println ();

            int dsorg = getWord (fullBlock, 4);
            int blksize = getWord (fullBlock, 6);
            int lrecl = getWord (fullBlock, 8);

            int byte10 = buffer[10] & 0xFF;
            String recfm = format[byte10 >> 6];
            String blocked = (byte10 & 0x10) != 0 ? "B" : "";
            String spanned = (byte10 & 0x08) != 0 ? "S" : "";

            int keyLen = buffer[11] & 0xFF;
            int optcd = buffer[12] & 0xFF;

            System.out.printf ("Keylen = %d%n", keyLen);
            int containingBlksize = getWord (buffer, 14);
            System.out.printf ("Containing blksize = %d%n", containingBlksize);

            int maxBlocks = (containingBlksize + 8) / (blksize + 12);
            System.out.printf ("Max blocks = %d%n", maxBlocks);

            int lastField = getWord (buffer, 54);
            //            assert lastField == 0;

            System.out.printf ("DSORG : %04X%n", dsorg);
            System.out.printf ("BLKSZ : %04X  %<,6d%n", blksize);
            System.out.printf ("RECLEN: %04X  %<,6d%n", lrecl);
            System.out.printf ("RECFM : %s %s %s%n", recfm, blocked, spanned);
          }
          else if (dataBlocks.size () == 2)     // presumably info about the file layout
          {
            printHex (fullBlock);
            System.out.println ();
          }
          else if (inCatalog)
            inCatalog = addCatalogEntries (fullBlock);
          else    // in data
          {
            if (fullBlock.length == 12)
              ;
            //              printHex (fullBlock);
            else
            {
              CatalogEntry catalogEntry = catalogEntries.get (currentEntry);
              catalogEntry.addBlock (fullBlock);
              if (catalogEntry.isComplete ())
                ++currentEntry;
            }
          }
        }
      }

      ptr += length;
    }

    //    int count = 0;
    //    for (byte[] block : dataBlocks)
    //    {
    //      if (++count > 10)
    //        break;
    //      System.out.printf ("Block: %04X%n", count);
    //      printHex (block);
    //      System.out.println ();
    //    }

    int totalLength = 0;
    for (byte[] block : dataBlocks)
      totalLength += block.length;

    System.out.printf ("Data segments :     %04X  %<,10d%n", dataBlocks.size ());
    System.out.printf ("Data size     : %08X  %<,10d%n", totalLength);
    System.out.printf ("Total blocks  :     %04X  %<,10d%n", totalBlocks);
    System.out.printf ("Total entries :     %04X  %<,10d%n", catalogEntries.size ());

    System.out.println ();
    int count = 0;
    for (CatalogEntry catalogEntry : catalogEntries)
      System.out.printf ("%4d  %s%n", count++, catalogEntry);

    for (int i = 0; i < 5; i++)
      if (i < catalogEntries.size ())
        catalogEntries.get (i).list ();
  }

  // ---------------------------------------------------------------------------------//
  // consolidate
  // ---------------------------------------------------------------------------------//

  byte[] consolidate (List<byte[]> blocks)
  {
    int blockLength = 0;
    for (byte[] block : blocks)
      blockLength += block.length;

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

  //  List<CatalogEntry> printDirectory (byte[] buffer)
  //  {
  //    int ptr = 22;
  //    List<CatalogEntry> catalogEntries = new ArrayList<> ();
  //
  //    while (ptr + 42 < buffer.length)
  //    {
  //      if (buffer[ptr] == (byte) 0xFF)
  //        break;
  //      if (buffer[ptr] == 0)
  //      {
  //        ptr += 24;
  //        continue;
  //      }
  //      System.out.println (Utility.toHex (buffer, ptr, 32, Utility.EBCDIC, 0));
  //      System.out.println ();
  //      CatalogEntry catalogEntry = new CatalogEntry (buffer, ptr);
  //      catalogEntries.add (catalogEntry);
  //      ptr += 42;
  //    }
  //    return catalogEntries;
  //  }

  // ---------------------------------------------------------------------------------//
  // addCatalogEntries
  // ---------------------------------------------------------------------------------//

  boolean addCatalogEntries (byte[] buffer)
  {
    int ptr = 0;
    while (ptr < buffer.length)
    {
      System.out.println (getHexString (buffer, ptr, 12));      // header
      ptr += 12;
      System.out.println (getHexString (buffer, ptr, 10));      // last member name
      ptr += 10;

      for (int i = 0; i < 6; i++)
      {
        if (buffer[ptr] == (byte) 0xFF)
          return false;
        String memberName = getString (buffer, ptr, 8);
        String userName = getString (buffer, ptr + 32, 8);
        System.out.printf ("%s %s %s%n", getHexString (buffer, ptr, 42), memberName,
            userName);
        catalogEntries.add (new CatalogEntry (buffer, ptr));

        ptr += 42;
      }

      System.out.println (getHexString (buffer, ptr, 2));
      ptr += 2;
    }
    System.out.println ();
    return true;
  }

  // ---------------------------------------------------------------------------------//
  // printData
  // ---------------------------------------------------------------------------------//

  //  void printData (int blockNo, int totalBlocks)
  //  {
  //    byte[] buffer = dataBlocks.get (blockNo);
  //    int ptr = 12;
  //    int line = 0;
  //    System.out.printf ("%nBlock no: %04X  %04X  %06X%n", blockNo, totalBlocks,
  //        buffer.length / 80);
  //    while (ptr < buffer.length)
  //    {
  //      ++line;
  //      int len = Integer.min (80, buffer.length - ptr);
  //      System.out.printf ("%04X: %s%n", line, Reader.getString (buffer, ptr, len));
  //      ptr += len;
  //    }
  //  }

  // ---------------------------------------------------------------------------------//
  // printHex
  // ---------------------------------------------------------------------------------//

  static void printHex (byte[] buffer)
  {
    System.out.println (Utility.toHex (buffer, 0, buffer.length, Utility.EBCDIC, 0));
  }

  static void printHex (byte[] buffer, int offset, int length)
  {
    System.out.println (Utility.toHex (buffer, offset, length, Utility.EBCDIC, 0));
  }

  // ---------------------------------------------------------------------------------//
  // getString
  // ---------------------------------------------------------------------------------//

  public static String getString (byte[] buffer, int ptr, int length)
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

  public static int getWord (byte[] buffer, int ptr)
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

  // ---------------------------------------------------------------------------------//
  // getHexString
  // ---------------------------------------------------------------------------------//

  static String getHexString (byte[] buffer, int offset, int length)
  {
    StringBuilder text = new StringBuilder ();

    while (length-- > 0)
      text.append (String.format ("%02X ", buffer[offset++]));
    //    if (text.length () > 0)
    //      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}
