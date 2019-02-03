package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg.Org;

// ---------------------------------------------------------------------------------//
public abstract class Dataset
//---------------------------------------------------------------------------------//
{
  final Reader reader;
  final ControlRecord inmr02;
  final Disposition disposition;

  final List<Segment> segments = new ArrayList<> ();
  int rawBufferLength;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  Dataset (Reader reader, ControlRecord inmr02)
  {
    this.reader = reader;
    this.inmr02 = inmr02;
    disposition = new Disposition (inmr02);
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

  public boolean isPs ()
  {
    return disposition.dsorg == Org.PS;
  }

  // ---------------------------------------------------------------------------------//
  // isPds
  // ---------------------------------------------------------------------------------//

  public boolean isPds ()
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

  public ControlRecord getControlRecord ()
  {
    return inmr02;
  }

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
