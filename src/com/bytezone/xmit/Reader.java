package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bytezone.xmit.textunit.Dsorg;
import com.bytezone.xmit.textunit.TextUnit;
import com.bytezone.xmit.textunit.TextUnitString;

public class Reader
{
  //  private static String[] format = { "?", "V", "F", "U" };

  List<ControlRecord> controlRecords = new ArrayList<> ();
  List<CatalogEntry> catalogEntries = new ArrayList<> ();
  List<String> lines = new ArrayList<> ();

  private final byte[] buffer;
  private final byte[] INMR06 = { 0x08, (byte) 0xE0, (byte) 0xC9, (byte) 0xD5,
                                  (byte) 0xD4, (byte) 0xD9, (byte) 0xF0, (byte) 0xF6 };

  Dsorg.Org org;

  //  __XMIheadr  = "E0C9D5D4D9F0F1"x    xxINMR01
  //  __POEheadr  = "01CA6D0F"x
  //  __PDSheadr  = "00CA6D0F"x
  //  __R1offset  = 2
  //  __R2offset  = 12

  //  __ESDheadr  = "02C5E2C4"x
  //  __TXTheadr  = "02E3E7E3"x
  //  __RLDheadr_ = "02D9D3C4"x
  //  __ENDheadr  = "02C5D5C4"x

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Reader (byte[] buffer)
  {
    List<BlockPointerList> blockPointerLists = new ArrayList<> ();
    BlockPointerList currentBlockPointerList = null;
    this.buffer = buffer;
    //    System.out.println (Utility.toHex (buffer));

    //    String header = Utility.toHex (buffer, 0, 8);
    //    System.out.println (header);
    boolean dumpRaw = false;

    int ptr = 0;
    while (ptr < buffer.length)
    {
      int length = buffer[ptr] & 0xFF;
      byte flags = buffer[ptr + 1];

      boolean firstSegment = (flags & 0x80) != 0;
      boolean lastSegment = (flags & 0x40) != 0;
      boolean controlRecord = (flags & 0x20) != 0;
      boolean recordNumber = (flags & 0x10) != 0;       // not seen one of these yet

      if (false)
        System.out.printf ("%2s  %2s  %2s  %2s%n", firstSegment ? "FS" : "",
            lastSegment ? "LS" : "", controlRecord ? "CR" : "", recordNumber ? "RN" : "");

      if (recordNumber)
        System.out.println ("Found a record number");

      if (dumpRaw)
      {
        System.out.println (Utility.toHex (buffer, ptr, length));
        System.out.println ();
        if (matches (INMR06, buffer, ptr))
          return;
        ptr += length;
        continue;
      }

      if (controlRecord)
      {
        ControlRecord cr = new ControlRecord (buffer, ptr + 2, length - 2);
        controlRecords.add (cr);
        if (cr.name.equals ("INMR06"))
          break;
        if (cr.name.equals ("INMR02") && org == null)
          org = getOrg ();
        System.out.println (cr);
      }
      else
      {
        if (firstSegment)
        {
          currentBlockPointerList = new BlockPointerList (buffer);
          blockPointerLists.add (currentBlockPointerList);
        }

        currentBlockPointerList.add (new BlockPointer (buffer, ptr + 2, length - 2));
      }

      ptr += length;
    }

    switch (org)
    {
      case PDS:
        processPDS (blockPointerLists);
        break;
      case PS:
        processPS (blockPointerLists);
        break;
      default:
        System.out.println ("Unknown ORG: " + org);
    }

    //    for (CatalogEntry catalogEntry : catalogEntries)
    //      System.out.println (catalogEntry.getPrintLine ());
  }

  // ---------------------------------------------------------------------------------//
  // getOrg
  // ---------------------------------------------------------------------------------//

  Dsorg.Org getOrg ()
  {
    for (ControlRecord controlRecord : controlRecords)
      if (controlRecord.name.equals ("INMR02"))
      {
        TextUnit textUnit = controlRecord.getTextUnit (TextUnit.INMUTILN);
        if (textUnit == null)
          System.out.println ("text unit not found");
        else if (((TextUnitString) textUnit).getString ().equals ("IEBCOPY")
            || ((TextUnitString) textUnit).getString ().equals ("INMCOPY"))
        {
          Dsorg dsorg = (Dsorg) controlRecord.getTextUnit (TextUnit.INMDSORG);
          return dsorg.type;
        }
      }

    return null;
  }

  // ---------------------------------------------------------------------------------//
  // getTextUnit
  // ---------------------------------------------------------------------------------//

  Optional<TextUnit> getTextUnit (String unitKey)
  {
    return Optional.empty ();
  }

  // ---------------------------------------------------------------------------------//
  // processPS
  // ---------------------------------------------------------------------------------//

  void processPS (List<BlockPointerList> blockPointerLists)
  {
    for (int i = 0; i < blockPointerLists.size (); i++)
      lines.add (getString (blockPointerLists.get (i).getBuffer ()));
  }

  // ---------------------------------------------------------------------------------//
  // processPDS
  // ---------------------------------------------------------------------------------//

  void processPDS (List<BlockPointerList> blockPointerLists)
  {
    int currentEntry = 0;
    boolean inCatalog = true;
    int catalogLength = 0;

    for (int i = 0; i < blockPointerLists.size (); i++)
    {
      if (i == 0)
      {
        //            if (false)
        //            {
        //            int dsorg = getWord (fullBlock, 4);
        //            int blksize = getWord (fullBlock, 6);
        //            int lrecl = getWord (fullBlock, 8);
        //              int byte10 = buffer[10] & 0xFF;
        //              String recfm = format[byte10 >> 6];
        //              String blocked = (byte10 & 0x10) != 0 ? "B" : "";
        //              String spanned = (byte10 & 0x08) != 0 ? "S" : "";
        //
        //              int keyLen = buffer[11] & 0xFF;
        //              int optcd = buffer[12] & 0xFF;
        //
        //              int containingBlksize = getWord (buffer, 14);
        //
        //              int maxBlocks = (containingBlksize + 8) / (blksize + 12);
        //
        //              int lastField = getWord (buffer, 54);
        //              //            assert lastField == 0;
        //
        //              System.out.printf ("Keylen = %d%n", keyLen);
        //              System.out.printf ("Max blocks = %d%n", maxBlocks);
        //              System.out.printf ("Containing blksize = %d%n", containingBlksize);
        //              System.out.printf ("DSORG : %04X%n", dsorg);
        //              System.out.printf ("BLKSZ : %04X  %<,6d%n", blksize);
        //              System.out.printf ("RECLEN: %04X  %<,6d%n", lrecl);
        //              System.out.printf ("RECFM : %s %s %s%n", recfm, blocked, spanned);
        //              System.out.println ();
        //            }
        byte[] fullBlock = blockPointerLists.get (i).getBuffer ();
        System.out.println (Utility.toHex (fullBlock));
        System.out.println ();
      }
      else if (i == 1)     // presumably info about the file layout
      {
        byte[] fullBlock = blockPointerLists.get (i).getBuffer ();
        System.out.println (Utility.toHex (fullBlock));
        System.out.println ();

        System.out.println (Utility.toHex (fullBlock, 0, 16));
        System.out.println ();
        int tot = fullBlock[0] & 0xFF;
        for (int j = 0; j < tot; j++)
          System.out.println (Utility.toHex (fullBlock, j * 16 + 16, 16));
        System.out.println ();
      }
      else if (inCatalog)
      {
        byte[] fullBlock = blockPointerLists.get (i).getBuffer ();
        System.out.printf ("Catalog buffer #%2d   %04X%n", i, fullBlock.length);
        //        System.out.println (Utility.toHex (fullBlock));
        //        System.out.println ();
        inCatalog = addCatalogEntries (fullBlock);
        catalogLength += fullBlock.length;
      }
      else    // in data
      {
        // distribute the data blocks to the PDS members
        BlockPointerList bpl = blockPointerLists.get (i);
        CatalogEntry catalogEntry = catalogEntries.get (currentEntry);
        catalogEntry.addBlockPointerList (bpl);

        if (false)
          System.out.printf ("%s   %,5d  %,7d  %,7d   %d%n",
              catalogEntry.getMemberName (), bpl.size (), bpl.getBufferLength (),
              bpl.getDataLength (), bpl.countHeaders ());

        if (bpl.isLastBlock ())
          ++currentEntry;
      }
    }
    System.out.printf ("%nCatalog entries: %d  %<04X  %,d  %<06X%n",
        catalogEntries.size (), catalogLength);
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
        if (eof || buffer[ptr2] == 0x00)
          break;

        CatalogEntry catalogEntry = new CatalogEntry (buffer, ptr2);
        catalogEntries.add (catalogEntry);

        ptr2 += catalogEntry.length ();       // 42 or 12 or 52
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
  // getLines
  // ---------------------------------------------------------------------------------//

  public String getLines ()
  {
    StringBuilder text = new StringBuilder ();
    for (String line : lines)
      text.append (line + "\n");
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // matches
  // ---------------------------------------------------------------------------------//

  static boolean matches (byte[] key, byte[] buffer, int ptr)
  {
    if (ptr + key.length > buffer.length)
      return false;

    for (int i = 0; i < key.length; i++)
      if (key[i] != buffer[ptr + i])
        return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  // printHex
  // ---------------------------------------------------------------------------------//

  static void printHex (byte[] buffer)
  {
    System.out.println (Utility.toHex (buffer, 0, buffer.length));
  }

  static void printHex (byte[] buffer, int offset, int length)
  {
    System.out.println (Utility.toHex (buffer, offset, length));
  }

  // ---------------------------------------------------------------------------------//
  // getString
  // ---------------------------------------------------------------------------------//

  static String getString (byte[] buffer)
  {
    return getString (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  // getString
  // ---------------------------------------------------------------------------------//

  public static String getString (byte[] buffer, int ptr, int length)
  {
    assert ptr + length <= buffer.length;

    StringBuilder text = new StringBuilder ();

    for (int i = 0; i < length; i++)
    {
      int c = buffer[ptr + i] & 0xFF;
      text.append (c < 0x40 ? "." : (char) Utility.ebc2asc[c]);
    }

    return text.toString ();
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
}
