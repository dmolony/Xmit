package com.bytezone.xmit;

import java.util.*;

import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;
import com.bytezone.xmit.textunit.Dsorg.Org;
import com.bytezone.xmit.textunit.TextUnit;
import com.bytezone.xmit.textunit.TextUnitNumber;
import com.bytezone.xmit.textunit.TextUnitString;

public class Reader
{
  private static final int DIR_BLOCK_LENGTH = 0x114;

  private final List<ControlRecord> controlRecords = new ArrayList<> ();
  private final List<CatalogEntry> catalogEntries = new ArrayList<> ();

  private final byte[] INMR03 = { (byte) 0xE0, (byte) 0xC9, (byte) 0xD5, (byte) 0xD4,
                                  (byte) 0xD9, (byte) 0xF0, (byte) 0xF3 };
  private final byte[] INMR06 = { 0x08, (byte) 0xE0, (byte) 0xC9, (byte) 0xD5,
                                  (byte) 0xD4, (byte) 0xD9, (byte) 0xF0, (byte) 0xF6 };

  private final List<BlockPointerList> controlPointerLists = new ArrayList<> ();
  private List<BlockPointerList> blockPointerLists;
  private final List<List<BlockPointerList>> masterBPL = new ArrayList<> ();

  private int catalogEndBlock = 0;
  private final List<String> lines = new ArrayList<> ();        // sequential file
  private int lrecl;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Reader (byte[] buffer)
  {
    buildPointerLists (buffer);

    // build the INMRxx control records
    for (BlockPointerList bpl : controlPointerLists)
      controlRecords.add (new ControlRecord (bpl.getRawBuffer ()));

    if (false)
      for (ControlRecord controlRecord : controlRecords)
        System.out.println (controlRecord);

    // default to the last DSORG and BPL
    Org org = getOrg (masterBPL.size ());                           // 1-based
    blockPointerLists = masterBPL.get (masterBPL.size () - 1);      // 0-based

    if (false && masterBPL.size () > 1)
      for (BlockPointerList bpl : masterBPL.get (0))
        System.out.println (Utility.getString (bpl.getRawBuffer ()));

    // allocate the data records
    switch (org)
    {
      case PDS:
        lrecl = getRecordLength ("IEBCOPY");
        processPDS (blockPointerLists);
        break;

      case PS:
        lrecl = getRecordLength ("INMCOPY");
        processPS (blockPointerLists);
        break;

      default:
        System.out.println ("Unknown ORG: " + org);
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
      {
        String name = controlRecord ? Utility.getString (buffer, ptr + 2, 6) : "";
        System.out.printf ("%02X  %2s  %2s  %2s  %2s  %s%n", length,
            firstSegment ? "FS" : "", lastSegment ? "LS" : "", controlRecord ? "CR" : "",
            recordNumber ? "RN" : "", name);
        if (!controlRecord && firstSegment && lastSegment)
          System.out.println (Utility.getHexDump (buffer, ptr, length));
      }

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
          controlPointerLists.add (currentBlockPointerList);

          if (Utility.matches (INMR06, buffer, ptr))
            eof = true;

          // handle multiple INMR03 records - see FILE434.XMI
          else if (Utility.matches (INMR03, buffer, ptr + 1))
          {
            blockPointerLists = new ArrayList<> ();
            masterBPL.add (blockPointerLists);
          }
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
    //    if (max > 300)
    //    {
    //      lines.add (String.format ("File contains %,d BlockPointerLists", max));
    //      max = 5;
    //    }

    for (int i = 0; i < max; i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      byte[] buffer = bpl.getRawBuffer ();             // raw buffer
      if (lrecl == 0)
        lines.add (Utility.getHexDump (buffer));
      else
      {
        int ptr = 0;
        while (ptr < buffer.length)
        {
          int len = Math.min (lrecl, buffer.length - ptr);
          lines.add (Utility.getString (buffer, ptr, len).stripTrailing ());
          ptr += len;
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // getLines
  // ---------------------------------------------------------------------------------//

  // this should be converted to an abstract File which Member would also use
  // only OutputPane uses this
  public String getLines ()
  {
    lines.clear ();
    blockPointerLists = masterBPL.get (masterBPL.size () - 1);
    processPS (blockPointerLists);

    StringBuilder text = new StringBuilder ();
    for (String line : lines)
      text.append (line + "\n");
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // processPDS
  // ---------------------------------------------------------------------------------//

  void processPDS (List<BlockPointerList> blockPointerLists)
  {
    boolean inCatalog = true;

    // skip first two BlockPointerList entries
    // read catalo data as raw data
    // convert remaining entries to BlockPointers with the headers removed
    for (int i = 2; i < blockPointerLists.size (); i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (inCatalog)
      {
        inCatalog = addCatalogEntries (bpl.getRawBuffer ());
        if (!inCatalog)
          catalogEndBlock = i;
      }
      else
        bpl.createDataBlocks ();       // create new BlockPointers
    }

    // assign new BlockPointer lists to CatalogEntries
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
      CatalogEntry catalogEntry = uniqueCatalogEntries.get (currentMember);
      if (!catalogEntry.addBlockPointerList (bpl))
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
      CatalogEntry catalogEntry = uniqueCatalogEntries.get (currentMember);
      if (catalogEntry.getOffset () == offset)
        catalogEntry.addBlockPointerList (bpl);
    }
  }

  // ---------------------------------------------------------------------------------//
  // addCatalogEntries
  // ---------------------------------------------------------------------------------//

  boolean addCatalogEntries (byte[] buffer)
  {
    int ptr = 0;
    while (ptr + 22 < buffer.length)
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
  // getOrg
  // ---------------------------------------------------------------------------------//

  Dsorg.Org getOrg ()
  {
    Optional<ControlRecord> opt =
        getControlRecord ("INMR02", TextUnit.INMUTILN, "IEBCOPY");
    if (opt.isEmpty ())
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
  // getOrg
  // ---------------------------------------------------------------------------------//

  Dsorg.Org getOrg (int fileNbr)
  {
    Optional<ControlRecord> opt = getInmr02 (fileNbr, "IEBCOPY");
    if (opt.isEmpty ())
      opt = getInmr02 (fileNbr, "INMCOPY");
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
  // getInmr02
  // ---------------------------------------------------------------------------------//

  Optional<ControlRecord> getInmr02 (int fileNbr, String utilityName)
  {
    for (ControlRecord controlRecord : controlRecords)
      if (controlRecord.fileNbrMatches (fileNbr))
      {
        TextUnit textUnit = controlRecord.getTextUnit (TextUnit.INMUTILN);
        if (textUnit != null
            && ((TextUnitString) textUnit).getString ().equals (utilityName))
          return Optional.of (controlRecord);
      }

    return Optional.empty ();
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

  public int getRecordLength (String utility)
  {
    Optional<ControlRecord> opt = getControlRecord ("INMR02", TextUnit.INMUTILN, utility);
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
