package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class DataBlock
{
  private final int offset;
  private final byte[] header;
  private final List<BlockPointer> blockPointers = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public DataBlock (int offset, byte[] header)
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
  // getSize
  // ---------------------------------------------------------------------------------//

  public int getSize ()
  {
    return (int) Utility.getValue (header, 9, 3);
  }

  // ---------------------------------------------------------------------------------//
  // ttlMatches
  // ---------------------------------------------------------------------------------//

  boolean ttlMatches (byte[] ttl)
  {
    return Utility.matches (ttl, header, 4);
  }

  // ---------------------------------------------------------------------------------//
  // getTtl
  // ---------------------------------------------------------------------------------//

  long getTtl ()
  {
    return Utility.getValue (header, 4, 5);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%06X: %s  %,7d", offset, Utility.getHexValues (header),
        getSize ());
  }
}
