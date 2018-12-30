package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;
import com.bytezone.xmit.textunit.Dsorg.Org;
import com.bytezone.xmit.textunit.TextUnit;
import com.bytezone.xmit.textunit.TextUnitNumber;
import com.bytezone.xmit.textunit.TextUnitString;

public class Reader
{
  private static final byte[] INMR03 =
      { (byte) 0xE0, (byte) 0xC9, (byte) 0xD5, (byte) 0xD4, (byte) 0xD9, (byte) 0xF0,
        (byte) 0xF3 };
  private static final byte[] INMR06 =
      { 0x08, (byte) 0xE0, (byte) 0xC9, (byte) 0xD5, (byte) 0xD4, (byte) 0xD9,
        (byte) 0xF0, (byte) 0xF6 };

  private final List<ControlRecord> controlRecords = new ArrayList<> ();
  private final List<BlockPointerList> controlPointerLists = new ArrayList<> ();

  private final Dataset currentDataset;
  private final List<Dataset> datasets = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Reader (byte[] buffer)
  {
    buildPointerLists (buffer);

    if (false)
      for (ControlRecord controlRecord : controlRecords)
        System.out.println (controlRecord);

    // default to the last dataset
    currentDataset = datasets.get (datasets.size () - 1);
    //    currentDataset = datasets.get (0);

    if (false && datasets.size () > 1)
      for (BlockPointerList bpl : datasets.get (0).blockPointerLists)
        System.out.println (Utility.getString (bpl.getRawBuffer ()));

    // allocate the data records
    switch (currentDataset.org)
    {
      case PDS:
        ((PdsDataset) currentDataset).processPDS ();
        break;

      case PS:
        ((PsDataset) currentDataset).processPS ();
        break;

      default:
        System.out.println ("Unknown ORG: " + currentDataset.org);
    }
  }

  // ---------------------------------------------------------------------------------//
  // buildPointerLists
  // ---------------------------------------------------------------------------------//

  private void buildPointerLists (byte[] buffer)
  {
    BlockPointerList currentBlockPointerList = null;
    Dataset dataset = null;

    boolean dumpRaw = false;

    int ptr = 0;
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
        currentBlockPointerList = new BlockPointerList (buffer);

        if (controlRecord)
        {
          controlPointerLists.add (currentBlockPointerList);

          if (Utility.matches (INMR06, buffer, ptr))
            eof = true;

          // handle multiple INMR03 records - see FILE434.XMI/XEF62
          else if (Utility.matches (INMR03, buffer, ptr + 1))
          {
            Org org = getOrg (datasets.size () + 1);
            switch (org)
            {
              case PS:
                dataset = new PsDataset (org, getRecordLength ("INMCOPY"));
                break;

              case PDS:
                dataset = new PdsDataset (org, getRecordLength ("IEBCOPY"));
                break;

              case VSAM:
                System.out.println ("VSAM dataset");
                break;
            }
            datasets.add (dataset);
          }
        }
        else
          dataset.add (currentBlockPointerList);
      }

      currentBlockPointerList.addSegment (firstSegment, lastSegment,
          new BlockPointer (buffer, ptr + 2, length - 2));

      if (lastSegment)
      {
        if (controlRecord)
        {
          BlockPointerList bpl =
              controlPointerLists.get (controlPointerLists.size () - 1);
          controlRecords.add (new ControlRecord (bpl.getRawBuffer ()));
        }
      }

      ptr += length;
    }
  }

  // ---------------------------------------------------------------------------------//
  // getLines
  // ---------------------------------------------------------------------------------//

  // this should be converted to an abstract File which Member would also use
  // only OutputPane uses this
  public String getLines ()
  {
    if (currentDataset.org == Org.PS)
      return ((PsDataset) currentDataset).getLines ();
    return "bollocks";
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
    if (currentDataset.org == Org.PDS)
      return ((PdsDataset) currentDataset).getCatalogEntries ();
    else
      return new ArrayList<> ();
  }

  // ---------------------------------------------------------------------------------//
  // getXmitFiles
  // ---------------------------------------------------------------------------------//

  public List<CatalogEntry> getMembers ()
  {
    if (currentDataset.org == Org.PDS)
      return ((PdsDataset) currentDataset).getMembers ();
    return new ArrayList<> ();
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

  int getRecordLength (String utility)
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
