package com.bytezone.xmit;

// -----------------------------------------------------------------------------------//
public class PsDataset extends Dataset
// -----------------------------------------------------------------------------------//
{
  private FlatFile flatFile;

  // ---------------------------------------------------------------------------------//
  PsDataset (XmitReader reader, Disposition disposition)
  // ---------------------------------------------------------------------------------//
  {
    super (reader, disposition, reader.getDatasetName ());
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
    flatFile.setName (getReader ().getDatasetName ());

    for (Segment segment : segments)
      flatFile.addSegment (segment);
  }
}
