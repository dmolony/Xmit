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
  //  private static final byte[] INMR03 =
  //      { (byte) 0xE0, (byte) 0xC9, (byte) 0xD5, (byte) 0xD4, (byte) 0xD9, (byte) 0xF0,
  //        (byte) 0xF3 };
  private static final byte[] INMR06 =
      { 0x08, (byte) 0xE0, (byte) 0xC9, (byte) 0xD5, (byte) 0xD4, (byte) 0xD9,
        (byte) 0xF0, (byte) 0xF6 };

  private final String fileName;
  private final List<ControlRecord> controlRecords = new ArrayList<> ();
  private final List<Dataset> datasets = new ArrayList<> ();

  private final Dataset activeDataset;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Reader (String fileName, byte[] buffer)
  {
    this.fileName = fileName;
    Segment currentSegment = null;
    Dataset currentDataset = null;

    int ptr = 0;

    while (ptr < buffer.length)
    {
      int length = buffer[ptr] & 0xFF;
      byte flags = buffer[ptr + 1];

      boolean firstSegment = (flags & 0x80) != 0;
      boolean lastSegment = (flags & 0x40) != 0;
      boolean controlRecord = (flags & 0x20) != 0;
      boolean recordNumber = (flags & 0x10) != 0;       // not seen one of these yet

      if (recordNumber)
        System.out.println ("******** Found a record number");

      //      if (false)
      //      {
      //        String name = controlRecord ? Utility.getString (buffer, ptr + 2, 6) : "";
      //        System.out.printf ("%02X  %2s  %2s  %2s  %2s  %s%n", length,
      //            firstSegment ? "FS" : "", lastSegment ? "LS" : "", controlRecord ? "CR" : "",
      //            recordNumber ? "RN" : "", name);
      //        if (!controlRecord && firstSegment && lastSegment)
      //          System.out.println (Utility.getHexDump (buffer, ptr, length));
      //      }

      //      if (false)
      //      {
      //        System.out.println (Utility.getHexDump (buffer, ptr, length));
      //        System.out.println ();
      //        if (Utility.matches (INMR06, buffer, ptr))
      //          break;
      //        ptr += length;
      //        continue;
      //      }

      if (firstSegment)
        currentSegment = new Segment (buffer);

      currentSegment.addBlockPointer (new BlockPointer (buffer, ptr + 2, length - 2));

      if (lastSegment)
      {
        if (controlRecord)
        {
          ControlRecord cr = new ControlRecord (currentSegment.getRawBuffer ());
          controlRecords.add (cr);
          if (cr.nameMatches ("INMR06"))
            break;
          //          if (cr.nameMatches ("INMR01"))
          //          {
          //            TextUnit textUnit = cr.getTextUnit (TextUnit.INMNUMF);
          //            if (textUnit != null)
          //              int files = (int) ((TextUnitNumber) textUnit).getNumber ();
          //          }
          if (cr.nameMatches ("INMR03"))
          {
            Org org = getOrg (datasets.size () + 1);
            ControlRecord inmr02 = getInmr02 (datasets.size () + 1).get ();
            switch (org)
            {
              case PS:
                currentDataset = new PsDataset (this, inmr02);
                break;

              case PDS:
                currentDataset = new PdsDataset (this, inmr02);
                break;

              case VSAM:
                currentDataset = null;         // will crash
                System.out.println ("VSAM dataset");
                break;
            }
            datasets.add (currentDataset);
          }
        }
        else
          currentDataset.addSegment (currentSegment);
      }

      ptr += length;
    }

    // allocate the data records
    for (Dataset dataset : datasets)
      dataset.process ();

    if (datasets.size () > 1)
      System.out.printf ("Processed %d datasets%n", datasets.size ());

    //    if (false)
    //      for (ControlRecord controlRecord : controlRecords)
    //        System.out.println (controlRecord);

    //    if (false && datasets.size () > 1)
    //      for (Segment segment : datasets.get (0).segments)
    //        System.out.println (Utility.getString (segment.getRawBuffer ()));

    // set active dataset
    activeDataset = datasets.get (datasets.size () - 1);     // always last
  }

  // ---------------------------------------------------------------------------------//
  // getName
  // ---------------------------------------------------------------------------------//

  public String getName ()
  {
    return fileName;
  }

  // ---------------------------------------------------------------------------------//
  // getDatasets
  // ---------------------------------------------------------------------------------//

  public List<Dataset> getDatasets ()
  {
    return datasets;
  }

  // ---------------------------------------------------------------------------------//
  // getActiveDataset
  // ---------------------------------------------------------------------------------//

  public Dataset getActiveDataset ()
  {
    return activeDataset;
  }

  // ---------------------------------------------------------------------------------//
  // getControlRecords
  // ---------------------------------------------------------------------------------//

  public List<ControlRecord> getControlRecords ()
  {
    return controlRecords;
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

  Optional<ControlRecord> getInmr02 (int fileNbr)
  {
    for (ControlRecord controlRecord : controlRecords)
      if (controlRecord.fileNbrMatches (fileNbr))
        return Optional.of (controlRecord);

    return Optional.empty ();
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
