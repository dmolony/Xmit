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
  // getType
  // ---------------------------------------------------------------------------------//

  byte getType ()
  {
    return header.buffer[0];
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
  // getTwoBytes
  // ---------------------------------------------------------------------------------//

  int getTwoBytes ()
  {
    if (header.getSize () == 0)
      return 0;

    BlockPointer blockPointer = blockPointers.get (0);
    return Utility.getTwoBytes (blockPointer.buffer, blockPointer.offset);
  }

  // ---------------------------------------------------------------------------------//
  // getEightBytes
  // ---------------------------------------------------------------------------------//

  byte[] getEightBytes ()
  {
    byte[] buffer = new byte[8];
    if (header.getSize () == 0)
      return buffer;

    BlockPointer blockPointer = blockPointers.get (0);
    System.arraycopy (blockPointer.buffer, blockPointer.offset, buffer, 0, buffer.length);
    return buffer;
    //    return Utility.getTwoBytes (blockPointer.buffer, blockPointer.offset);
  }

  // ---------------------------------------------------------------------------------//
  // packBuffer
  // ---------------------------------------------------------------------------------//

  int packBuffer (byte[] buffer, int offset)
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
  // getBuffer
  // ---------------------------------------------------------------------------------//

  byte[] getBuffer ()
  {
    byte[] buffer = new byte[getSize ()];
    int ptr = 0;
    for (BlockPointer blockPointer : blockPointers)
    {
      System.arraycopy (blockPointer.buffer, blockPointer.offset, buffer, ptr,
          blockPointer.length);
      ptr += blockPointer.length;
    }
    assert ptr == buffer.length;
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  // isXmit
  // ---------------------------------------------------------------------------------//

  private static byte[] INMR01 = { (byte) 0xE0, (byte) 0xC9, (byte) 0xD5, (byte) 0xD4,
                                   (byte) 0xD9, (byte) 0xF0, (byte) 0xF1 };

  boolean isXmit ()
  {
    if (header.getSize () == 0)       // see FILE392.XMI/$NULL
      return false;

    BlockPointer blockPointer = blockPointers.get (0);
    return Utility.matches (INMR01, blockPointer.buffer, blockPointer.offset + 1);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%06X: %s  %,7d  %,5d", offset, header, getSize (),
        blockPointers.size ());
  }
}
