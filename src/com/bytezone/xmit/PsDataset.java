package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.Utility.FileType;
import com.bytezone.xmit.textunit.ControlRecord;

public class PsDataset extends Dataset
{
  private final List<String> lines = new ArrayList<> ();        // sequential file

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  PsDataset (Reader reader, ControlRecord inmr02)
  {
    super (reader, inmr02);
  }

  // ---------------------------------------------------------------------------------//
  // process
  // ---------------------------------------------------------------------------------//

  @Override
  void process ()
  {
    if (getFileType () != FileType.BIN)
    {
      extractMessage ();
      return;
    }
    //    int size = 0;
    //    for (Segment segment : segments)
    //      size += segment.getRawBufferLength ();

    int max = segments.size ();
    if (max > 200 && rawBufferLength > 200_000)
    {
      lines.add (String.format ("File contains %,d Segments", max));
      lines.add (String.format ("File contains %,d bytes", rawBufferLength));
      lines.add ("");
      max = 20;
      lines.add ("Displaying first " + max + " segments");
      lines.add ("");
    }

    for (int i = 0; i < max; i++)
    {
      Segment segment = segments.get (i);
      byte[] buffer = segment.getRawBuffer ();
      if (lrecl == 0)
        lines.add (Utility.getHexDump (buffer));
      else
      {
        int ptr = 0;
        while (ptr < buffer.length)
        {
          int len = Math.min (lrecl, buffer.length - ptr);
          lines.add (Utility.getString (buffer, ptr, len).stripTrailing ());
          ptr += len;
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // extractMessage
  // ---------------------------------------------------------------------------------//

  void extractMessage ()
  {
    lines.add ("File type: " + getFileType ());
    lines.add ("");
    lines.add ("Use File->Extract to save a copy in the correct format,");
    lines.add ("      or use the HEX tab to view the raw file.");
  }

  // ---------------------------------------------------------------------------------//
  // getRawBuffer
  // ---------------------------------------------------------------------------------//

  public byte[] getRawBuffer ()
  {
    int max = segments.size ();
    //    int bufferLength = 0;
    //    for (int i = 0; i < max; i++)
    //      bufferLength += segments.get (i).getRawBufferLength ();

    byte[] buffer = new byte[rawBufferLength];

    int ptr = 0;
    for (int i = 0; i < max; i++)
      ptr = segments.get (i).getRawBuffer (buffer, ptr);

    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  // getFileType
  // ---------------------------------------------------------------------------------//

  public FileType getFileType ()
  {
    //    if (member.isXmit ())
    //      return FileType.XMIT;

    return Utility.getFileType (segments.get (0).getEightBytes ());
  }

  // ---------------------------------------------------------------------------------//
  // getLines
  // ---------------------------------------------------------------------------------//

  // only OutputPane uses this
  public String getLines ()
  {
    StringBuilder text = new StringBuilder ();
    for (String line : lines)
      text.append (line + "\n");
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }
}
