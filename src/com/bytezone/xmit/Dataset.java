package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.Dsorg.Org;

// ---------------------------------------------------------------------------------//
public abstract class Dataset
//---------------------------------------------------------------------------------//
{
  final Reader reader;
  final Disposition disposition;

  final List<Segment> segments = new ArrayList<> ();
  int rawBufferLength;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  Dataset (Reader reader, Disposition disposition)
  {
    this.reader = reader;
    this.disposition = disposition;
  }

  // ---------------------------------------------------------------------------------//
  // getReader
  // ---------------------------------------------------------------------------------//

  public Reader getReader ()
  {
    return reader;
  }

  // ---------------------------------------------------------------------------------//
  // getDisposition
  // ---------------------------------------------------------------------------------//

  public Disposition getDisposition ()
  {
    return disposition;
  }

  // ---------------------------------------------------------------------------------//
  // isPs
  // ---------------------------------------------------------------------------------//

  public boolean isPhysicalSequential ()
  {
    return disposition.dsorg == Org.PS;
  }

  // ---------------------------------------------------------------------------------//
  // isPds
  // ---------------------------------------------------------------------------------//

  public boolean isPartitionedDataset ()
  {
    return disposition.dsorg == Org.PDS;
  }

  // ---------------------------------------------------------------------------------//
  // getRawBufferLength
  // ---------------------------------------------------------------------------------//

  int getRawBufferLength ()
  {
    return rawBufferLength;
  }

  // ---------------------------------------------------------------------------------//
  // allocateSegments
  // ---------------------------------------------------------------------------------//

  abstract void allocateSegments ();

  // ---------------------------------------------------------------------------------//
  // addSegment
  // ---------------------------------------------------------------------------------//

  void addSegment (Segment segment)
  {
    segments.add (segment);
    rawBufferLength += segment.getRawBufferLength ();
  }

  // ---------------------------------------------------------------------------------//
  // getControlRecord
  // ---------------------------------------------------------------------------------//

  //  private ControlRecord getControlRecord ()
  //  {
  //    return inmr02;
  //  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  public String listSegments ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("File contains %,d bytes in %,d Segments%n%n",
        rawBufferLength, segments.size ()));

    int count = 0;
    int total = 0;
    for (Segment segment : segments)
    {
      total += segment.getRawBufferLength ();
      text.append (String.format ("%,5d  %,7d  %,7d  %3d%n", count++,
          segment.getRawBufferLength (), total, segment.size ()));

      if (count > 500)
        break;
    }

    Utility.removeTrailingNewlines (text);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%-20s %s", reader.getFileName (), disposition);
  }
}
