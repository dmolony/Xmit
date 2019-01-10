package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class DataBlock
{
  private final int offset;
  private final Header header;
  private final List<BlockPointer> blockPointers = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public DataBlock (int offset, Header header)
  {
    this.offset = offset;
    this.header = header;
  }

  // ---------------------------------------------------------------------------------//
  // add
  // ---------------------------------------------------------------------------------//

  void add (BlockPointer blockPointer)
  {
    blockPointers.add (blockPointer);
  }

  // ---------------------------------------------------------------------------------//
  // getHeader
  // ---------------------------------------------------------------------------------//

  Header getHeader ()
  {
    return header;
  }

  // ---------------------------------------------------------------------------------//
  // getSize
  // ---------------------------------------------------------------------------------//

  public int getSize ()
  {
    return header.getSize ();
  }

  // ---------------------------------------------------------------------------------//
  // ttlMatches
  // ---------------------------------------------------------------------------------//

  boolean ttlMatches (byte[] ttl)
  {
    return header.ttlMatches (ttl);
  }

  // ---------------------------------------------------------------------------------//
  // getTtl
  // ---------------------------------------------------------------------------------//

  long getTtl ()
  {
    return header.getTtl ();
  }

  // ---------------------------------------------------------------------------------//
  // getDataBuffer
  // ---------------------------------------------------------------------------------//

  int getDataBuffer (byte[] buffer, int offset)
  {
    assert buffer.length >= offset + getSize ();

    for (BlockPointer blockPointer : blockPointers)
    {
      System.arraycopy (blockPointer.buffer, blockPointer.offset, buffer, offset,
          blockPointer.length);
      offset += blockPointer.length;
    }

    return offset;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%06X: %s  %,7d", offset, header, getSize ());
  }
}
