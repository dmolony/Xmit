package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.Dsorg.Org;

public class PsDataset extends Dataset
{
  private final List<String> lines = new ArrayList<> ();        // sequential file

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  PsDataset (Reader reader, Org org, int lrecl)
  {
    super (reader, org, lrecl);
  }

  // ---------------------------------------------------------------------------------//
  // processPS
  // ---------------------------------------------------------------------------------//

  @Override
  void process ()
  {
    int max = blockPointerLists.size ();
    if (max > 200)
    {
      lines.add (String.format ("File contains %,d BlockPointerLists", max));
      max = 10;
    }

    for (int i = 0; i < max; i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      byte[] buffer = bpl.getRawBuffer ();
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
  // getRawBuffer
  // ---------------------------------------------------------------------------------//

  public byte[] getRawBuffer ()
  {
    int max = blockPointerLists.size () > 200 ? 10 : blockPointerLists.size ();
    int bufferLength = 0;
    for (int i = 0; i < max; i++)
      bufferLength += blockPointerLists.get (i).getRawBufferLength ();

    byte[] buffer = new byte[bufferLength];

    int ptr = 0;
    for (int i = 0; i < max; i++)
      ptr = blockPointerLists.get (i).getRawBuffer (buffer, ptr);

    return buffer;
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
