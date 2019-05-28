package com.bytezone.xmit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;
import com.bytezone.xmit.textunit.Dsorg.Org;
import com.bytezone.xmit.textunit.TextUnit;
import com.bytezone.xmit.textunit.TextUnitNumber;
import com.bytezone.xmit.textunit.TextUnitString;

// -----------------------------------------------------------------------------------//
public class XmitReader extends Reader
// -----------------------------------------------------------------------------------//
{
  static final byte[] INMR01 = { (byte) 0xE0, (byte) 0xC9, (byte) 0xD5, (byte) 0xD4,
                                 (byte) 0xD9, (byte) 0xF0, (byte) 0xF1 };

  private final List<ControlRecord> controlRecords = new ArrayList<> ();

  private int files;

  // ---------------------------------------------------------------------------------//
  public XmitReader (File file)
  // ---------------------------------------------------------------------------------//
  {
    super (file.getName (), 0);

    read (readFile (file));
  }

  // ---------------------------------------------------------------------------------//
  public XmitReader (DataFile dataFile)
  // ---------------------------------------------------------------------------------//
  {
    super (dataFile.getName (), dataFile.getLevel ());

    read (dataFile.getDataBuffer ());
  }

  // ---------------------------------------------------------------------------------//
  private void read (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    XmitSegment currentSegment = null;
    Dataset currentDataset = null;
    int ptr = 0;

    while (ptr < buffer.length)
    {
      int length = buffer[ptr] & 0xFF;
      if (ptr + length > buffer.length)
      {
        incomplete = true;          // see FILE185.XMI / FILE234I
        break;
      }

      byte flags = buffer[ptr + 1];
      boolean firstSegment = (flags & 0x80) != 0;
      boolean lastSegment = (flags & 0x40) != 0;
      boolean controlRecord = (flags & 0x20) != 0;
      boolean recordNumber = (flags & 0x10) != 0;       // not seen one of these yet

      if (recordNumber)
        System.out.println ("******** Found a record number");

      if (firstSegment)
        currentSegment = new XmitSegment ();

      currentSegment.addBlockPointer (new BlockPointer (buffer, ptr + 2, length - 2));

      if (lastSegment)
      {
        if (controlRecord)
        {
          ControlRecord cr = new ControlRecord (currentSegment.getRawBuffer ());
          controlRecords.add (cr);
          if (cr.nameMatches ("INMR06"))
            break;
          if (cr.nameMatches ("INMR01"))
          {
            TextUnit textUnit = cr.getTextUnit (TextUnit.INMNUMF);
            if (textUnit != null)
              files = (int) ((TextUnitNumber) textUnit).getNumber ();
          }
          if (cr.nameMatches ("INMR03"))
          {
            Optional<Org> optOrg = getDsorg (datasets.size () + 1);
            if (optOrg.isPresent ())
            {
              Disposition disposition =
                  new Disposition (getInmr02 (datasets.size () + 1).get ());
              switch (optOrg.get ())
              {
                case PS:
                  currentDataset = new PsDataset (this, disposition);
                  break;

                case PDS:
                  currentDataset = new PdsDataset (this, disposition);
                  break;

                case VSAM:
                  currentDataset = null;         // will crash
                  System.out.println ("VSAM datasets are not supported");
                  break;
              }
            }
            else
              currentDataset = null;

            if (currentDataset != null)
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
      dataset.allocateSegments ();

    // set active dataset
    activeDataset = datasets.get (datasets.size () - 1);     // always last
    //    activeDataset = datasets.get (0);     // always first
    assert files == datasets.size ();
  }

  // ---------------------------------------------------------------------------------//
  public List<ControlRecord> getControlRecords ()
  // ---------------------------------------------------------------------------------//
  {
    return controlRecords;
  }

  // ---------------------------------------------------------------------------------//
  private Optional<Org> getDsorg (int fileNbr)
  // ---------------------------------------------------------------------------------//
  {
    Optional<ControlRecord> opt = getInmr02 (fileNbr, "IEBCOPY");
    if (opt.isEmpty ())
      opt = getInmr02 (fileNbr, "INMCOPY");
    if (opt.isPresent ())
    {
      ControlRecord controlRecord = opt.get ();
      TextUnit textUnit = controlRecord.getTextUnit (TextUnit.INMDSORG);
      if (textUnit != null)
        return Optional.of (((Dsorg) textUnit).type);
    }
    return Optional.empty ();
  }

  // ---------------------------------------------------------------------------------//
  private Optional<ControlRecord> getInmr02 (int fileNbr)
  // ---------------------------------------------------------------------------------//
  {
    for (ControlRecord controlRecord : controlRecords)
      if (controlRecord.fileNbrMatches (fileNbr))
        return Optional.of (controlRecord);

    return Optional.empty ();
  }

  // ---------------------------------------------------------------------------------//
  private Optional<ControlRecord> getInmr02 (int fileNbr, String utilityName)
  // ---------------------------------------------------------------------------------//
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
  @Override
  public String getDatasetName ()
  // ---------------------------------------------------------------------------------//
  {
    return getControlRecordString (TextUnit.INMDSNAM);
  }

  // ---------------------------------------------------------------------------------//
  private String getControlRecordString (int key)
  // ---------------------------------------------------------------------------------//
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
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("Xmit Reader: %s", fileName);
  }
}
