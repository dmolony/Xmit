package com.bytezone.xmit;

import java.util.*;

import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;
import com.bytezone.xmit.textunit.TextUnit;
import com.bytezone.xmit.textunit.TextUnitString;

public class Reader
{
  private static final int DIR_BLOCK_LENGTH = 0x114;

  private final List<ControlRecord> controlRecords = new ArrayList<> ();
  private final List<CatalogEntry> catalogEntries = new ArrayList<> ();
  private final List<String> lines = new ArrayList<> ();

  private final byte[] INMR06 = { 0x08, (byte) 0xE0, (byte) 0xC9, (byte) 0xD5,
                                  (byte) 0xD4, (byte) 0xD9, (byte) 0xF0, (byte) 0xF6 };

  private boolean isPDSE;

  private final List<BlockPointerList> controlPointerLists = new ArrayList<> ();
  private final List<BlockPointerList> blockPointerLists = new ArrayList<> ();
  private int catalogEndBlock = 0;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Reader (byte[] buffer)
  {
    BlockPointerList currentBlockPointerList = null;

    boolean dumpRaw = false;

    int ptr = 0;
    int count = 0;
    boolean eof = false;

    while (!eof && ptr < buffer.length)
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
        System.out.println ("******** Found a record number");

      if (dumpRaw)
      {
        System.out.println (Utility.getHexDump (buffer, ptr, length));
        System.out.println ();
        if (Utility.matches (INMR06, buffer, ptr))
          eof = true;
        ptr += length;
        continue;
      }

      if (firstSegment)
      {
        currentBlockPointerList = new BlockPointerList (buffer, count++);
        if (controlRecord)
        {
          if (Utility.matches (INMR06, buffer, ptr))
            eof = true;
          controlPointerLists.add (currentBlockPointerList);
        }
        else
          blockPointerLists.add (currentBlockPointerList);
      }

      currentBlockPointerList.addSegment (firstSegment, lastSegment,
          new BlockPointer (buffer, ptr + 2, length - 2));

      ptr += length;
    }

    // build the control records
    for (BlockPointerList bpl : controlPointerLists)
      controlRecords.add (new ControlRecord (bpl.getBuffer ()));

    // allocate the data records
    switch (getOrg ())
    {
      case PDS:
        processPDS (blockPointerLists);
        break;

      case PS:
        processPS (blockPointerLists);
        break;

      default:
        System.out.println ("Unknown ORG: " + getOrg ());
    }
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
    if (blockPointerLists.size () > 600)
    {
      lines.add (String.format ("File contains %,d BlockPointerLists",
          blockPointerLists.size ()));
      for (int i = 0; i < 5; i++)
      {
        BlockPointerList bpl = blockPointerLists.get (i);
        lines.add (Utility.getHexDump (bpl.getBuffer ()));
      }
    }
    else
      for (int i = 0; i < blockPointerLists.size (); i++)
        lines.add (getString (blockPointerLists.get (i).getBuffer ()));
  }

  // ---------------------------------------------------------------------------------//
  // processPDS
  // ---------------------------------------------------------------------------------//

  void processPDS (List<BlockPointerList> blockPointerLists)
  {
    boolean inCatalog = true;

    for (int i = 2; i < blockPointerLists.size (); i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (inCatalog)
      {
        inCatalog = addCatalogEntries (bpl.getBuffer ());
        if (!inCatalog)
          catalogEndBlock = i;
      }
      else
        bpl.build ();       // create new BlockPointers
    }

    List<CatalogEntry> sortedCatalogEntries = new ArrayList<> (catalogEntries);
    Collections.sort (sortedCatalogEntries);

    //    System.out.printf ("%nSorted catalog entries:%n");
    Map<Integer, CatalogEntry> offsets = new TreeMap<> ();
    for (CatalogEntry catalogEntry : sortedCatalogEntries)
    {
      //      System.out.println (catalogEntry);
      if (!offsets.containsKey (catalogEntry.getOffset ()))
        offsets.put (catalogEntry.getOffset (), catalogEntry);
    }

    //    System.out.printf ("%nUnique entries:%n");
    List<CatalogEntry> uniqueCatalogEntries = new ArrayList<> ();
    for (CatalogEntry catalogEntry : offsets.values ())
    {
      //      System.out.println (catalogEntry);
      uniqueCatalogEntries.add (catalogEntry);
    }

    // check for PDSE
    BlockPointerList bpl2 = blockPointerLists.get (catalogEndBlock + 1);

    if (bpl2.headers.get (0)[0] == (byte) 0x88)
    {
      isPDSE = true;
      int lastValue = 0;
      int currentMember = -1;
      for (int i = catalogEndBlock + 2; i < blockPointerLists.size (); i++)
      {
        BlockPointerList bpl = blockPointerLists.get (i);

        int value = (int) Utility.getValue (bpl.headers.get (0), 6, 3);
        if (lastValue != value)
        {
          ++currentMember;
          lastValue = value;
        }
        CatalogEntry ce = uniqueCatalogEntries.get (currentMember);
        if (ce.getOffset () == value)
          ce.addBlockPointerList (bpl);
      }
    }
    else
    {
      // assign BlockPointerLists to CatalogEntries
      int currentMember = 0;
      for (int i = catalogEndBlock + 1; i < blockPointerLists.size (); i++)
      {
        BlockPointerList bpl = blockPointerLists.get (i);

        CatalogEntry ce = uniqueCatalogEntries.get (currentMember);
        if (!ce.addBlockPointerList (bpl))
          break;
        if (bpl.isLastBlock ())
          ++currentMember;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // addCatalogEntries
  // ---------------------------------------------------------------------------------//

  boolean addCatalogEntries (byte[] buffer)
  {
    int ptr = 0;
    while (ptr < buffer.length)
    {
      int ptr2 = ptr + 22;

      while (true)
      {
        if (buffer[ptr2] == (byte) 0xFF)
          return false;                                     // member list finished

        CatalogEntry catalogEntry = new CatalogEntry (buffer, ptr2);
        catalogEntries.add (catalogEntry);

        // check for last member
        if (Utility.matches (buffer, ptr2, buffer, ptr + 12, 8))
          break;

        ptr2 += catalogEntry.length ();
      }

      ptr += DIR_BLOCK_LENGTH;
    }

    return true;                                            // member list not finished
  }

  // ---------------------------------------------------------------------------------//
  // getControlRecords
  // ---------------------------------------------------------------------------------//

  public List<ControlRecord> getControlRecords ()
  {
    return controlRecords;
  }

  // ---------------------------------------------------------------------------------//
  // getCatalogEntries
  // ---------------------------------------------------------------------------------//

  public List<CatalogEntry> getCatalogEntries ()
  {
    return catalogEntries;
  }

  // ---------------------------------------------------------------------------------//
  // getBlockPointerLists
  // ---------------------------------------------------------------------------------//

  public List<BlockPointerList> getDataBlockPointerLists ()
  {
    List<BlockPointerList> newList = new ArrayList<> ();
    for (int i = catalogEndBlock + 1; i < blockPointerLists.size (); i++)
      newList.add (blockPointerLists.get (i));
    return newList;
  }

  // ---------------------------------------------------------------------------------//
  // getLines
  // ---------------------------------------------------------------------------------//

  // this should be converted to an abstract File which Member would also use
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
  // getOrg
  // ---------------------------------------------------------------------------------//

  Dsorg.Org getOrg ()
  {
    Optional<ControlRecord> opt =
        getControlRecord ("INMR02", TextUnit.INMUTILN, "IEBCOPY");
    if (!opt.isPresent ())
      opt = getControlRecord ("INMR02", TextUnit.INMUTILN, "INMCOPY");
    if (opt.isPresent ())
    {
      ControlRecord controlRecord = opt.get ();
      TextUnit textUnit = controlRecord.getTextUnit (TextUnit.INMDSORG);
      if (textUnit != null)
        return ((Dsorg) textUnit).type;
    }

    return null;
  }

  // ---------------------------------------------------------------------------------//
  // getControlRecord
  // ---------------------------------------------------------------------------------//

  private Optional<ControlRecord> getControlRecord (String stepName, int key,
      String value)
  {
    for (ControlRecord controlRecord : controlRecords)
      if (controlRecord.nameMatches (stepName))
      {
        TextUnit textUnit = controlRecord.getTextUnit (key);
        if (textUnit != null && ((TextUnitString) textUnit).getString ().equals (value))
          return Optional.of (controlRecord);
      }
    return Optional.empty ();
  }

  // ---------------------------------------------------------------------------------//
  // getControlRecord
  // ---------------------------------------------------------------------------------//

  Optional<ControlRecord> getControlRecord (int key, String value)
  {
    for (ControlRecord controlRecord : controlRecords)
    {
      TextUnit textUnit = controlRecord.getTextUnit (key);
      if (textUnit != null && ((TextUnitString) textUnit).getString ().equals (value))
        return Optional.of (controlRecord);
    }
    return Optional.empty ();
  }

  // ---------------------------------------------------------------------------------//
  // getControlRecordString
  // ---------------------------------------------------------------------------------//

  String getControlRecordString (int key)
  {
    for (ControlRecord controlRecord : controlRecords)
    {
      TextUnit textUnit = controlRecord.getTextUnit (key);
      if (textUnit != null && textUnit instanceof TextUnitString)
        return ((TextUnitString) textUnit).getString ();
    }
    return "";
  }

  // ---------------------------------------------------------------------------------//
  // getFileName
  // ---------------------------------------------------------------------------------//

  public String getFileName ()
  {
    return getControlRecordString (TextUnit.INMDSNAM);
  }

  // ---------------------------------------------------------------------------------//
  // printHex
  // ---------------------------------------------------------------------------------//

  static void printHex (byte[] buffer)
  {
    System.out.println (Utility.getHexDump (buffer, 0, buffer.length));
  }

  // ---------------------------------------------------------------------------------//
  // printHex
  // ---------------------------------------------------------------------------------//

  static void printHex (byte[] buffer, int offset, int length)
  {
    System.out.println (Utility.getHexDump (buffer, offset, length));
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

    return text.toString ();
  }
}
