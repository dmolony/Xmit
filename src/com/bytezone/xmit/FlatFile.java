package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class FlatFile extends DataFile implements Iterable<Segment>
// -----------------------------------------------------------------------------------//
{
  private final List<Segment> segments;                        // PS

  // ---------------------------------------------------------------------------------//
  FlatFile (Dataset dataset, Disposition disposition)
  // ---------------------------------------------------------------------------------//
  {
    super (dataset, disposition);

    segments = new ArrayList<> ();
  }

  // ---------------------------------------------------------------------------------//
  void addSegment (Segment segment)
  // ---------------------------------------------------------------------------------//
  {
    segments.add (segment);
    incrementDataLength (segment.getRawBufferLength ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] getDataBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = new byte[getDataLength ()];
    int ptr = 0;

    for (Segment segment : segments)
      ptr = segment.packBuffer (buffer, ptr);

    assert ptr == getDataLength ();
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] getDataBuffer (int limit)
  // ---------------------------------------------------------------------------------//
  {
    if (getDataLength () <= limit)
      return getDataBuffer ();

    int length = 0;
    List<Segment> tmpSegments = new ArrayList<> ();
    for (Segment segment : segments)
    {
      tmpSegments.add (segment);
      length += segment.getRawBufferLength ();
      if (length >= limit)
        break;
    }

    byte[] buffer = new byte[length];
    int ptr = 0;

    for (Segment segment : tmpSegments)
      ptr = segment.packBuffer (buffer, ptr);

    assert ptr == length;
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isXmit ()
  // ---------------------------------------------------------------------------------//
  {
    return segments.get (0).isXmit ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  boolean isRdw ()
  // ---------------------------------------------------------------------------------//
  {
    System.out.println ("isRdw not written: " + getName ());    // haven't seen one yet
    return false;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void rdwLines ()
  // ---------------------------------------------------------------------------------//
  {
    int count = 0;
    int max = 500;
    for (Segment segment : segments)
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
  @Override
  byte[] getEightBytes ()
  // ---------------------------------------------------------------------------------//
  {
    return segments.get (0).getEightBytes ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int count = 0;
    int total = 0;

    text.append ("\n    #   Offset     Data     Data     Ptr\n");
    text.append ("  ----  ------    ------   ------    ---\n");
    for (Segment segment : segments)                // PS
    {
      total += segment.getRawBufferLength ();
      text.append (String.format ("   %3d  %s%n", count++, segment));
    }

    Utility.removeTrailingNewlines (text);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<Segment> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    return segments.iterator ();
  }
}
