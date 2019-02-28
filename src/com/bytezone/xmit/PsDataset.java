package com.bytezone.xmit;

import com.bytezone.xmit.textunit.ControlRecord;

// ---------------------------------------------------------------------------------//
public class PsDataset extends Dataset
//---------------------------------------------------------------------------------//
{
  private FlatFile member;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  PsDataset (Reader reader, ControlRecord inmr02)
  {
    super (reader, inmr02);
  }

  // ---------------------------------------------------------------------------------//
  // getMember
  // ---------------------------------------------------------------------------------//

  public FlatFile getMember ()
  {
    return member;
  }

  // ---------------------------------------------------------------------------------//
  // process
  // ---------------------------------------------------------------------------------//

  @Override
  void allocateSegments ()
  {
    member = new FlatFile (this, disposition);
    member.setName (reader.getFileName ());

    for (Segment segment : segments)
      member.addSegment (segment);
  }
}
