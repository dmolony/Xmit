package com.bytezone.xmit;

import com.bytezone.xmit.textunit.ControlRecord;

// ---------------------------------------------------------------------------------//
public class PsDataset extends Dataset
// ---------------------------------------------------------------------------------//
{
  private FlatFile flatFile;

  // ---------------------------------------------------------------------------------//
  PsDataset (Reader reader, ControlRecord inmr02)
  // ---------------------------------------------------------------------------------//
  {
    super (reader, inmr02);
  }

  // ---------------------------------------------------------------------------------//
  public FlatFile getFlatFile ()
  // ---------------------------------------------------------------------------------//
  {
    return flatFile;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void allocateSegments ()
  // ---------------------------------------------------------------------------------//
  {
    flatFile = new FlatFile (this, disposition);
    flatFile.setName (reader.getFileName ());

    for (Segment segment : segments)
      flatFile.addSegment (segment);
  }
}
