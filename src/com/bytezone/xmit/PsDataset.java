package com.bytezone.xmit;

// -----------------------------------------------------------------------------------//
public class PsDataset extends Dataset
// -----------------------------------------------------------------------------------//
{
  private FlatFile flatFile;

  // ---------------------------------------------------------------------------------//
  PsDataset (XmitReader reader, Disposition disposition, String name)
  // ---------------------------------------------------------------------------------//
  {
    super (reader, disposition, name);
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
    flatFile = new FlatFile (this, getDisposition ());
    flatFile.setName (getName ());

    for (Segment segment : segments)
      flatFile.addSegment (segment);
  }
}
