package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.Dsorg.Org;

// ---------------------------------------------------------------------------------//
public abstract class Dataset
//---------------------------------------------------------------------------------//
{
  final XmitReader reader;
  final Disposition disposition;

  final List<XmitSegment> segments = new ArrayList<> ();
  int rawBufferLength;

  // ---------------------------------------------------------------------------------//
  Dataset (XmitReader reader, Disposition disposition)
  // ---------------------------------------------------------------------------------//
  {
    this.reader = reader;
    this.disposition = disposition;
  }

  // ---------------------------------------------------------------------------------//
  public XmitReader getReader ()
  // ---------------------------------------------------------------------------------//
  {
    return reader;
  }

  // ---------------------------------------------------------------------------------//
  public Disposition getDisposition ()
  // ---------------------------------------------------------------------------------//
  {
    return disposition;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isPhysicalSequential ()
  // ---------------------------------------------------------------------------------//
  {
    return disposition.dsorg == Org.PS;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isPartitionedDataset ()
  // ---------------------------------------------------------------------------------//
  {
    return disposition.dsorg == Org.PDS;
  }

  // ---------------------------------------------------------------------------------//
  int getRawBufferLength ()
  // ---------------------------------------------------------------------------------//
  {
    return rawBufferLength;
  }

  // ---------------------------------------------------------------------------------//
  abstract void allocateSegments ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  void addSegment (XmitSegment segment)
  // ---------------------------------------------------------------------------------//
  {
    segments.add (segment);
    rawBufferLength += segment.getRawBufferLength ();
  }

  // ---------------------------------------------------------------------------------//
  public String listSegments ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("File contains %,d bytes in %,d Segments%n%n",
        rawBufferLength, segments.size ()));

    int count = 0;
    int total = 0;
    for (XmitSegment segment : segments)
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
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%-20s %s", reader.getFileName (), disposition);
  }
}
