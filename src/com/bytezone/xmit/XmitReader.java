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
    super (file.getName (), ReaderType.XMIT);
    read (readFile (file));
  }

  // ---------------------------------------------------------------------------------//
  public XmitReader (DataFile dataFile)
  // ---------------------------------------------------------------------------------//
  {
    super (dataFile.getName (), ReaderType.XMIT);
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
        setIsIncomplete (true);          // see FILE185.XMI / FILE234I
        break;
      }

      byte flags = buffer[ptr + 1];
      boolean isFirstSegment = (flags & 0x80) != 0;
      boolean isLastSegment = (flags & 0x40) != 0;
      boolean isControlRecord = (flags & 0x20) != 0;
      boolean isRecordNumber = (flags & 0x10) != 0;       // not seen one of these yet

      if (isRecordNumber)
        System.out.println ("******** Found a record number");

      if (isFirstSegment)
        currentSegment = new XmitSegment ();

      currentSegment.addBlockPointer (new BlockPointer (buffer, ptr + 2, length - 2));
      ptr += length;

      if (isLastSegment)
      {
        if (isControlRecord)
        {
          ControlRecord controlRecord =
              new ControlRecord (currentSegment.getRawBuffer ());
          controlRecords.add (controlRecord);

          switch (controlRecord.getControlRecordType ())
          {
            case INMR01:
              TextUnit textUnit = controlRecord.getTextUnit (TextUnit.INMNUMF);
              if (textUnit != null)
                files = (int) ((TextUnitNumber) textUnit).getNumber ();
              break;
            case INMR02:
              break;
            case INMR03:
              int fileNo = datasets.size () + 1;
              Optional<Org> optOrg = getDsorg (fileNo);
              if (optOrg.isEmpty ())
                throw new IllegalArgumentException ("DSORG not found");

              Disposition disposition = new Disposition (getInmr02 (fileNo).get ());

              //   currentDataset = switch (optOrg.get ())
              //   {
              //    case PS -> new PsDataset (this, disposition, getDatasetName ());
              //    case PDS -> new PdsDataset (this, disposition, getDatasetName ());
              //  case VSAM -> throw new IllegalArgumentException ("VSAM not supported");
              //              };
              switch (optOrg.get ())
              {
                case PS:
                  currentDataset = new PsDataset (this, disposition, getDatasetName ());
                  break;
                case PDS:
                  currentDataset = new PdsDataset (this, disposition, getDatasetName ());
                  break;
                case VSAM:
                  throw new IllegalArgumentException ("VSAM not supported");
              }
              datasets.add (currentDataset);
              break;
            case INMR06:
              ptr = Integer.MAX_VALUE;      // force break
              break;
            case INMR04:
            case INMR05:
              break;
            default:
              break;
          }
        }            // is a control record
        else
          currentDataset.addSegment (currentSegment);
      }             // isLastSegment
    }               // while ptr < buffer.length

    // allocate the data records
    for (Dataset dataset : datasets)
      dataset.allocateSegments ();

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
  //  @Override
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
    return String.format ("Xmit Reader: %s", getFileName ());
  }
}
