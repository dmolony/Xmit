package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

// ---------------------------------------------------------------------------------//
public class FlatFile extends DataFile implements Iterable<XmitSegment>
//---------------------------------------------------------------------------------//
{
  private final List<XmitSegment> segments;                        // PS

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  FlatFile (Dataset dataset, Disposition disposition)
  {
    super (dataset, disposition);

    segments = new ArrayList<> ();
  }

  // ---------------------------------------------------------------------------------//
  // addSegment
  // ---------------------------------------------------------------------------------//

  void addSegment (XmitSegment segment)
  {
    segments.add (segment);
    incrementDataLength (segment.getRawBufferLength ());
  }

  // ---------------------------------------------------------------------------------//
  // getDataBuffer
  // ---------------------------------------------------------------------------------//

  @Override
  public byte[] getDataBuffer ()
  {
    byte[] buffer = new byte[getDataLength ()];
    int ptr = 0;

    for (XmitSegment segment : segments)
      ptr = segment.packBuffer (buffer, ptr);

    assert ptr == getDataLength ();
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  // getDataBuffer
  // ---------------------------------------------------------------------------------//

  @Override
  public byte[] getDataBuffer (int limit)
  {
    if (getDataLength () <= limit)
      return getDataBuffer ();

    int length = 0;
    List<XmitSegment> tmpSegments = new ArrayList<> ();
    for (XmitSegment segment : segments)
    {
      tmpSegments.add (segment);
      length += segment.getRawBufferLength ();
      if (length >= limit)
        break;
    }

    byte[] buffer = new byte[length];
    int ptr = 0;

    for (XmitSegment segment : tmpSegments)
      ptr = segment.packBuffer (buffer, ptr);

    assert ptr == length;
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  // isXmit
  // ---------------------------------------------------------------------------------//

  @Override
  public boolean isXmit ()
  {
    return segments.get (0).isXmit ();
  }

  // ---------------------------------------------------------------------------------//
  // isRdw
  // ---------------------------------------------------------------------------------//

  @Override
  boolean isRdw ()
  {
    System.out.println ("not written: " + getName ());       // haven't seen one yet
    return false;
  }

  // ---------------------------------------------------------------------------------//
  // rdw
  // ---------------------------------------------------------------------------------//

  @Override
  void rdwLines ()
  {
    int count = 0;
    int max = 500;
    for (XmitSegment segment : segments)
    {
      byte[] buffer = segment.getRawBuffer ();
      if (Utility.isBinary (buffer))
      {
        for (String line : Arrays.asList (Utility.getHexDump (buffer).split ("\n")))
          lines.add (line);
        //        if (lines.size () > 10_000)
        //          break;
      }
      else
        lines.add (Utility.getString (buffer));
      if (++count > max)
        break;
    }
  }

  // ---------------------------------------------------------------------------------//
  // getEightBytes
  // ---------------------------------------------------------------------------------//

  @Override
  byte[] getEightBytes ()
  {
    return segments.get (0).getEightBytes ();
  }

  // ---------------------------------------------------------------------------------//
  // createLines
  // ---------------------------------------------------------------------------------//

  //  @Override
  //  void createLines ()
  //  {
  //    int max = segments.size ();
  //    int rawBufferLength = dataLength;
  //
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
  //  }

  // ---------------------------------------------------------------------------------//
  // hexDump
  // ---------------------------------------------------------------------------------//

  //  @Override
  //  void hexDump ()
  //  {
  //    if (disposition.lrecl < 80)
  //    {
  //      byte[] buffer = getDataBuffer ();
  //      int ptr = 0;
  //      while (ptr + 80 < buffer.length)
  //      {
  //        String[] chunks = Utility.getHexDump (buffer, ptr, 80).split ("\n");
  //        for (String chunk : chunks)
  //          lines.add (chunk);
  //        ptr += 80;
  //      }
  //      return;
  //    }
  //
  //    for (Segment segment : segments)
  //    {
  //      byte[] buffer = segment.getRawBuffer ();
  //      String[] chunks = Utility.getHexDump (buffer).split ("\n");
  //      for (String chunk : chunks)
  //        lines.add (chunk);
  //      if (lines.size () > 5000)
  //        break;
  //      lines.add ("");
  //    }
  //  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    int count = 0;
    int total = 0;

    text.append ("\n    #   Offset     Data     Data     Ptr\n");
    text.append ("  ----  ------    ------   ------    ---\n");
    for (XmitSegment segment : segments)                // PS
    {
      total += segment.getRawBufferLength ();
      text.append (String.format ("   %3d  %s%n", count++, segment));
    }

    Utility.removeTrailingNewlines (text);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // iterator
  // ---------------------------------------------------------------------------------//

  @Override
  public Iterator<XmitSegment> iterator ()
  {
    return segments.iterator ();
  }
}
