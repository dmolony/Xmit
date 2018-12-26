package com.bytezone.xmit;

import java.util.*;

import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;
import com.bytezone.xmit.textunit.TextUnit;
import com.bytezone.xmit.textunit.TextUnitNumber;
import com.bytezone.xmit.textunit.TextUnitString;

public class Reader
{
  private static final int DIR_BLOCK_LENGTH = 0x114;

  private final List<ControlRecord> controlRecords = new ArrayList<> ();
  private final List<CatalogEntry> catalogEntries = new ArrayList<> ();

  private final byte[] INMR06 = { 0x08, (byte) 0xE0, (byte) 0xC9, (byte) 0xD5,
                                  (byte) 0xD4, (byte) 0xD9, (byte) 0xF0, (byte) 0xF6 };

  private final List<BlockPointerList> controlPointerLists = new ArrayList<> ();
  private final List<BlockPointerList> blockPointerLists = new ArrayList<> ();
  private int catalogEndBlock = 0;
  private final List<String> lines = new ArrayList<> ();        // flat file
  private final int lrecl;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Reader (byte[] buffer)
  {
    buildPointerLists (buffer);

    // build the INMRxx control records
    for (BlockPointerList bpl : controlPointerLists)
      controlRecords.add (new ControlRecord (bpl.getBuffer ()));

    lrecl = getRecordLength ();

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
  // buildPointerLists
  // ---------------------------------------------------------------------------------//

  private void buildPointerLists (byte[] buffer)
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
  }

  // ---------------------------------------------------------------------------------//
  // processPS
  // ---------------------------------------------------------------------------------//

  void processPS (List<BlockPointerList> blockPointerLists)
  {
    int max = blockPointerLists.size ();
    if (max > 300)
    {
      lines.add (String.format ("File contains %,d BlockPointerLists", max));
      max = 5;
    }

    for (int i = 0; i < max; i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      lines.add (Utility.getHexDump (bpl.getBuffer ()));
    }
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

    Map<Integer, CatalogEntry> offsets = new TreeMap<> ();
    for (CatalogEntry catalogEntry : sortedCatalogEntries)
      if (!offsets.containsKey (catalogEntry.getOffset ()))
        offsets.put (catalogEntry.getOffset (), catalogEntry);

    List<CatalogEntry> uniqueCatalogEntries = new ArrayList<> ();
    for (CatalogEntry catalogEntry : offsets.values ())
      uniqueCatalogEntries.add (catalogEntry);

    // assign BlockPointerLists to CatalogEntries
    if (blockPointerLists.get (catalogEndBlock + 1).isPDSE ())
      assignPdsExtendedBlocks (uniqueCatalogEntries);
    else
      assignPdsBlocks (uniqueCatalogEntries);
  }

  // ---------------------------------------------------------------------------------//
  // assignPdsBlocks
  // ---------------------------------------------------------------------------------//

  private void assignPdsBlocks (List<CatalogEntry> uniqueCatalogEntries)
  {
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

  // ---------------------------------------------------------------------------------//
  // assignPdsExtendedBlocks
  // ---------------------------------------------------------------------------------//

  private void assignPdsExtendedBlocks (List<CatalogEntry> uniqueCatalogEntries)
  {
    int lastOffset = -1;
    int currentMember = -1;

    for (int i = catalogEndBlock + 2; i < blockPointerLists.size (); i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);

      int offset = bpl.getOffset ();
      if (lastOffset != offset)
      {
        ++currentMember;
        lastOffset = offset;
      }
      CatalogEntry ce = uniqueCatalogEntries.get (currentMember);
      if (ce.getOffset () == offset)
        ce.addBlockPointerList (bpl);
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

        CatalogEntry catalogEntry = new CatalogEntry (buffer, ptr2, lrecl);
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
  // getXmitFiles
  // ---------------------------------------------------------------------------------//

  public List<CatalogEntry> getXmitFiles ()
  {
    List<CatalogEntry> xmitFiles = new ArrayList<> ();
    for (CatalogEntry catalogEntry : catalogEntries)
      if (catalogEntry.isXmit ())
        xmitFiles.add (catalogEntry);
    return xmitFiles;
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
  // getControlRecordNumber
  // ---------------------------------------------------------------------------------//

  int getControlRecordNumber (int key)
  {
    for (ControlRecord controlRecord : controlRecords)
    {
      TextUnit textUnit = controlRecord.getTextUnit (key);
      if (textUnit != null && textUnit instanceof TextUnitNumber)
        return (int) ((TextUnitNumber) textUnit).getNumber ();
    }
    return -1;
  }

  // ---------------------------------------------------------------------------------//
  // getRecordLength
  // ---------------------------------------------------------------------------------//

  public int getRecordLength ()
  {
    Optional<ControlRecord> opt =
        getControlRecord ("INMR02", TextUnit.INMUTILN, "IEBCOPY");
    if (opt.isPresent ())
    {
      TextUnit textUnit = opt.get ().getTextUnit (TextUnit.INMLRECL);
      if (textUnit != null && textUnit instanceof TextUnitNumber)
        return (int) ((TextUnitNumber) textUnit).getNumber ();
    }
    return 0;
  }

  // ---------------------------------------------------------------------------------//
  // getFileName
  // ---------------------------------------------------------------------------------//

  public String getFileName ()
  {
    return getControlRecordString (TextUnit.INMDSNAM);
  }
}
