package com.bytezone.xmit;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.common.Utility;
import com.bytezone.xmit.textunit.Dsorg;
import com.bytezone.xmit.textunit.TextUnit;
import com.bytezone.xmit.textunit.TextUnitString;

public class Reader
{
  private static String[] format = { "?", "V", "F", "U" };

  List<ControlRecord> controlRecords = new ArrayList<> ();
  List<CatalogEntry> catalogEntries = new ArrayList<> ();
  List<byte[]> dataBlocks = new ArrayList<> ();

  boolean debug = false;
  Dsorg.Org org;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Reader (byte[] buffer)
  {
    List<List<BlockPointer>> blockPointersList = new ArrayList<> ();
    List<BlockPointer> blockPointers = new ArrayList<> ();

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

      if (controlRecord)
      {
        ControlRecord cr = new ControlRecord (buffer, ptr + 2, length - 2);
        controlRecords.add (cr);
        if (cr.name.equals ("INMR06"))
          break;
        if (cr.name.equals ("INMR02") && org == null)
          org = getOrg ();
      }
      else
      {
        if (firstSegment)
        {
          blockPointers.clear ();
        }

        blockPointers.add (new BlockPointer (ptr + 2, length - 2));

        if (lastSegment)
        {
          byte[] fullBlock = consolidate (buffer, blockPointers);
          dataBlocks.add (fullBlock);

          if (dataBlocks.size () == 1)
          {
            int dsorg = getWord (fullBlock, 4);
            int blksize = getWord (fullBlock, 6);
            int lrecl = getWord (fullBlock, 8);

            if (false)
            {
              int byte10 = buffer[10] & 0xFF;
              String recfm = format[byte10 >> 6];
              String blocked = (byte10 & 0x10) != 0 ? "B" : "";
              String spanned = (byte10 & 0x08) != 0 ? "S" : "";

              int keyLen = buffer[11] & 0xFF;
              int optcd = buffer[12] & 0xFF;

              int containingBlksize = getWord (buffer, 14);

              int maxBlocks = (containingBlksize + 8) / (blksize + 12);

              int lastField = getWord (buffer, 54);
              //            assert lastField == 0;

              System.out.printf ("Keylen = %d%n", keyLen);
              System.out.printf ("Max blocks = %d%n", maxBlocks);
              System.out.printf ("Containing blksize = %d%n", containingBlksize);
              System.out.printf ("DSORG : %04X%n", dsorg);
              System.out.printf ("BLKSZ : %04X  %<,6d%n", blksize);
              System.out.printf ("RECLEN: %04X  %<,6d%n", lrecl);
              System.out.printf ("RECFM : %s %s %s%n", recfm, blocked, spanned);
              System.out.println ();
            }
          }
          else if (dataBlocks.size () == 2)     // presumably info about the file layout
          {
          }
          else if (inCatalog)
          {
            inCatalog = addCatalogEntries (fullBlock);
          }
          else    // in data
          {
            CatalogEntry catalogEntry = catalogEntries.get (currentEntry);
            catalogEntry.addBlock (fullBlock);

            int rem = fullBlock.length % 80;
            int dataLength = Reader.getWord (fullBlock, 10);
            if (rem == 24 || dataLength == 0)
              ++currentEntry;
          }
        }
      }

      ptr += length;
    }
  }

  // ---------------------------------------------------------------------------------//
  // getOrg
  // ---------------------------------------------------------------------------------//

  Dsorg.Org getOrg ()
  {
    for (ControlRecord controlRecord : controlRecords)
    {
      if (controlRecord.name.equals ("INMR02"))
      {
        TextUnit textUnit = controlRecord.getTextUnit (TextUnit.INMUTILN);
        if (textUnit == null)
          System.out.println ("text unit not found");
        else if (((TextUnitString) textUnit).getString ().equals ("IEBCOPY"))
        {
          //          System.out.println (controlRecord.getTextUnit (TextUnit.INMDSORG));
          Dsorg dsorg = (Dsorg) controlRecord.getTextUnit (TextUnit.INMDSORG);
          return dsorg.type;
        }
      }
    }
    return null;
  }

  // ---------------------------------------------------------------------------------//
  // consolidate
  // ---------------------------------------------------------------------------------//

  byte[] consolidate (byte[] buffer, List<BlockPointer> blockPointers)
  {
    int blockLength = 0;
    for (BlockPointer blockPointer : blockPointers)
      blockLength += blockPointer.length;

    byte[] fullBlock = new byte[blockLength];
    int ptr = 0;
    for (BlockPointer blockPointer : blockPointers)
    {
      System.arraycopy (buffer, blockPointer.start, fullBlock, ptr, blockPointer.length);
      ptr += blockPointer.length;
    }
    assert ptr == blockLength;
    return fullBlock;
  }

  // ---------------------------------------------------------------------------------//
  // addCatalogEntries
  // ---------------------------------------------------------------------------------//

  boolean addCatalogEntries (byte[] buffer)
  {
    int ptr = 0;
    boolean eof = false;

    while (ptr + 22 < buffer.length)
    {
      String lastMember = getString (buffer, ptr + 12, 8);
      ptr += 22;

      int len = getWord (buffer, ptr - 2);          // used data?

      int ptr2 = ptr;

      while (true)
      {
        eof = buffer[ptr2] == (byte) 0xFF;
        if (eof || buffer[ptr2] == 0)
          break;

        CatalogEntry catalogEntry = new CatalogEntry (buffer, ptr2);
        catalogEntries.add (catalogEntry);

        ptr2 += catalogEntry.length ();
      }

      ptr += 254;
    }

    return !eof;
  }

  // ---------------------------------------------------------------------------------//
  // getCatalogEntries
  // ---------------------------------------------------------------------------------//

  public List<CatalogEntry> getCatalogEntries ()
  {
    return catalogEntries;
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
  // getDoubleWord
  // ---------------------------------------------------------------------------------//

  static int getDoubleWord (byte[] buffer, int ptr)
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

  class BlockPointer
  {
    int start;
    int length;

    BlockPointer (int start, int length)
    {
      this.start = start;
      this.length = length;
    }
  }
}
