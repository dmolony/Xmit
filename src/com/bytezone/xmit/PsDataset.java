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
    member = new FlatFile (reader, disposition);
    member.setName (reader.getFileName ());

    for (Segment segment : segments)
      member.addSegment (segment);

    //    if (getFileType () != FileType.BIN)
    //    {
    //      extractMessage ();
    //      return;
    //    }
    //
    //    int max = segments.size ();
    //    if (max > 500 && rawBufferLength > 200_000)
    //    {
    //      lines.add (String.format ("File contains %,d bytes in %,d Segments",
    //          rawBufferLength, max));
    //      lines.add ("");
    //      max = disposition.lrecl < 1000 ? 500 : 30;
    //      lines.add ("Displaying first " + max + " segments");
    //      lines.add ("");
    //    }
    //
    //    for (int i = 0; i < max; i++)
    //    {
    //      Segment segment = segments.get (i);
    //      byte[] buffer = segment.getRawBuffer ();
    //      if (disposition.lrecl <= 1)
    //        lines.add (Utility.getHexDump (buffer));
    //      else
    //      {
    //        int ptr = 0;
    //        while (ptr < buffer.length)
    //        {
    //          int len = Math.min (disposition.lrecl, buffer.length - ptr);
    //          if (Utility.isBinary (buffer, ptr, len))
    //          {
    //            String[] chunks = Utility.getHexDump (buffer).split ("\n");
    //            for (String chunk : chunks)
    //              lines.add (chunk);
    //            lines.add ("");
    //            //            lines.add (String.format ("%3d  %3d  %s", i, ptr,
    //            //                Utility.getString (buffer, ptr, len).stripTrailing ()));
    //          }
    //          else
    //            lines.add (Utility.getString (buffer, ptr, len).stripTrailing ());
    //          ptr += len;
    //        }
    //      }
    //    }
  }

  // ---------------------------------------------------------------------------------//
  // extractMessage
  // ---------------------------------------------------------------------------------//

  //  void extractMessage ()
  //  {
  //    lines.add ("File type: " + getFileType ());
  //    lines.add ("");
  //    lines.add ("Use File->Extract to save a copy in the correct format,");
  //    lines.add ("      or use the HEX tab to view the raw file.");
  //  }

  // ---------------------------------------------------------------------------------//
  // getRawBuffer
  // ---------------------------------------------------------------------------------//

  //  public byte[] getRawBuffer ()
  //  {
  //    //    int max = segments.size ();
  //    byte[] buffer = new byte[rawBufferLength];
  //
  //    int ptr = 0;
  //    //    for (int i = 0; i < max; i++)
  //    //    ptr = segments.get (i).getRawBuffer (buffer, ptr);
  //    for (Segment segment : segments)
  //      ptr = segment.packBuffer (buffer, ptr);
  //
  //    return buffer;
  //  }

  // ---------------------------------------------------------------------------------//
  // getFileType
  // ---------------------------------------------------------------------------------//

  //  public FileType getFileType ()
  //  {
  //    return Utility.getFileType (segments.get (0).getEightBytes ());
  //  }

  // ---------------------------------------------------------------------------------//
  // getLines
  // ---------------------------------------------------------------------------------//

  // only OutputPane uses this
  //  public String getLines ()
  //  {
  //    StringBuilder text = new StringBuilder ();
  //    for (String line : lines)
  //      text.append (line + "\n");
  //    if (text.length () > 0)
  //      text.deleteCharAt (text.length () - 1);
  //    return text.toString ();
  //  }
}
