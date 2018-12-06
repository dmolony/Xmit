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
    int totalBlockSize = 0;
    int currentEntry = 0;
    boolean inCatalog = true;
    //    int count = 0;
    int totalLines = 0;

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
          totalBlockSize += blocks.size ();
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
            System.out.println ();
          }
          else if (dataBlocks.size () == 2)     // presumably info about the file layout
          {
            printHex (fullBlock);
            System.out.println ();
          }
          else if (inCatalog)
          {
            inCatalog = addCatalogEntries (fullBlock);
            System.out.println ();
          }
          else    // in data
          {
            int lines = ((fullBlock.length - 12) / 80);
            totalLines += lines;
            int rem = fullBlock.length - lines * 80;

            int dataLength = Reader.getWord (fullBlock, 10);

            CatalogEntry catalogEntry = catalogEntries.get (currentEntry);
            catalogEntry.addBlock (fullBlock);

            if (rem == 24 || dataLength == 0)
            {
              String header = getHexString (fullBlock, 0, 12);
              String trailer =
                  rem == 24 ? getHexString (fullBlock, fullBlock.length - 12, 12) : "";
              System.out.printf ("%3d  %<04X  %s  %04X  %<,6d  %3d  %4d  %d  %s  %s%n",
                  currentEntry, catalogEntry.memberName, fullBlock.length, lines,
                  totalLines, rem, header, trailer);

              ++currentEntry;
              totalLines = 0;
            }
          }
        }
      }

      ptr += length;
    }

    int totalLength = 0;
    for (byte[] block : dataBlocks)
      totalLength += block.length;

    System.out.printf ("Data segments   :     %04X  %<,10d%n", dataBlocks.size ());
    System.out.printf ("Data size       : %08X  %<,10d%n", totalLength);
    System.out.printf ("Total blocks    :     %04X  %<,10d%n", totalBlockSize);
    System.out.printf ("Catalog entries :     %04X  %<,10d%n", catalogEntries.size ());

    //    if (false)
    //    {
    //      System.out.println ();
    //    count = 0;
    //    for (CatalogEntry catalogEntry : catalogEntries)
    //      System.out.printf ("%4d  %s%n", count++, catalogEntry);
    //
    //      for (int i = 0; i < 5; i++)
    //        if (i < catalogEntries.size ())
    //          catalogEntries.get (i).list ();
    //    }
    catalogEntries.get (400).list ();
  }

  // ---------------------------------------------------------------------------------//
  // consolidate
  // ---------------------------------------------------------------------------------//

  byte[] consolidate (List<byte[]> blocks)
  {
    int blockLength = 0;
    for (byte[] block : blocks)         // this should be a pointer to the original buffer
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
  // processBlocks
  // ---------------------------------------------------------------------------------//

  void processBlocks (byte[] fullBlock)
  {
  }

  // ---------------------------------------------------------------------------------//
  // addCatalogEntries
  // ---------------------------------------------------------------------------------//

  boolean addCatalogEntries (byte[] buffer)
  {
    int ptr = 0;
    boolean stillProcessing = true;
    System.out.printf ("Processing buffer: %,d  %<04X%n", buffer.length);

    while (ptr + 22 < buffer.length)
    {
      System.out.printf ("%06X: %s", ptr, getHexString (buffer, ptr, 22));      // header
      String lastMember = getString (buffer, ptr + 12, 8);
      System.out.println ("  Last: " + lastMember);
      ptr += 22;

      int len = getWord (buffer, ptr - 2);          // used data?

      int ptr2 = ptr;

      while (true)
      {
        stillProcessing = buffer[ptr2] != (byte) 0xFF;
        if (!stillProcessing || buffer[ptr2] == 0)
          break;

        CatalogEntry catalogEntry = new CatalogEntry (buffer, ptr2);
        catalogEntries.add (catalogEntry);

        System.out.printf ("%06X: %s%n", ptr2, catalogEntry.getPrintLine ());

        ptr2 += catalogEntry.length ();
      }

      ptr += 254;
      System.out.println ();
    }

    return stillProcessing;
  }

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
    assert ptr + length <= buffer.length;
    try
    {
      StringBuilder text = new StringBuilder ();

      for (int i = 0; i < length; i++)
        if ((buffer[ptr + i] & 0xFF) < 0x40)
          text.append (".");
        else
          text.append (new String (buffer, ptr + i, 1, Utility.EBCDIC));

      return text.toString ();
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

  static String getHexString (byte[] buffer)
  {
    return getHexString (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  // getHexString
  // ---------------------------------------------------------------------------------//

  static String getHexString (byte[] buffer, int offset, int length)
  {
    StringBuilder text = new StringBuilder ();

    while (length-- > 0 && offset < buffer.length)
      text.append (String.format ("%02X ", buffer[offset++]));
    //    if (text.length () > 0)
    //      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}
