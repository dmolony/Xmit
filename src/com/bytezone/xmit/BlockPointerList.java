package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class BlockPointerList
{
  private final List<BlockPointer> blockPointers = new ArrayList<> ();
  private final byte[] buffer;
  private int bufferLength;
  private int dataLength;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public BlockPointerList (byte[] buffer)
  {
    this.buffer = buffer;
  }

  // ---------------------------------------------------------------------------------//
  // add
  // ---------------------------------------------------------------------------------//

  public void add (BlockPointer blockPointer)
  {
    blockPointers.add (blockPointer);
    bufferLength += blockPointer.length;
    if (blockPointers.size () == 1)
      dataLength = Reader.getWord (buffer, blockPointer.start + 10);
  }

  // ---------------------------------------------------------------------------------//
  // getBufferLength
  // ---------------------------------------------------------------------------------//

  public int getBufferLength ()
  {
    return bufferLength;
  }

  // ---------------------------------------------------------------------------------//
  // getDataLength
  // ---------------------------------------------------------------------------------//

  public int getDataLength ()
  {
    return dataLength;
  }

  // ---------------------------------------------------------------------------------//
  // countHeaders
  // ---------------------------------------------------------------------------------//

  int countHeaders ()
  {
    return (bufferLength - dataLength) / 12;
  }

  // ---------------------------------------------------------------------------------//
  // isLastBlock
  // ---------------------------------------------------------------------------------//

  boolean isLastBlock ()
  {
    // Each data block starts with a 12-byte header. If it ends with a 12-byte trailer,
    // or is followed by an empty 12-byte block (i.e. a header but no data) then that
    // signals the end of the data for that PDS member.
    return countHeaders () == 2 || dataLength == 0;
  }

  // ---------------------------------------------------------------------------------//
  // size
  // ---------------------------------------------------------------------------------//

  public int size ()
  {
    return blockPointers.size ();
  }

  // ---------------------------------------------------------------------------------//
  // getWord
  // ---------------------------------------------------------------------------------//

  int getWord (int offset)
  {
    BlockPointer blockPointer = blockPointers.get (0);
    assert offset < blockPointer.length + 1;
    return Reader.getWord (buffer, blockPointer.start + offset);
  }

  // ---------------------------------------------------------------------------------//
  // getBuffer
  // ---------------------------------------------------------------------------------//

  byte[] getBuffer ()
  {
    byte[] fullBlock = new byte[bufferLength];
    int ptr = 0;
    for (BlockPointer blockPointer : blockPointers)
    {
      System.arraycopy (buffer, blockPointer.start, fullBlock, ptr, blockPointer.length);
      ptr += blockPointer.length;
    }
    assert ptr == bufferLength;
    return fullBlock;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Header length: %04X%n", bufferLength - dataLength));
    text.append (String.format ("Data length  : %04X%n", dataLength));

    boolean hasTrailer = bufferLength - dataLength == 24;
    int count = 0;
    for (BlockPointer blockPointer : blockPointers)
    {
      text.append (blockPointer);
      text.append ("\n");
      if (++count == 1)
      {
        text.append (Utility.toHex (buffer, blockPointer.start, 12));
        if (dataLength > 0)
        {
          text.append ("\n\n");
          text.append (
              Utility.toHex (buffer, blockPointer.start + 12, blockPointer.length - 12));
        }
      }
      else if (count == blockPointers.size () && hasTrailer)
      {
        text.append (
            Utility.toHex (buffer, blockPointer.start, blockPointer.length - 12));
        text.append ("\n\n");
        text.append (
            Utility.toHex (buffer, blockPointer.start + blockPointer.length - 12, 12));
      }
      else
        text.append (Utility.toHex (buffer, blockPointer.start, blockPointer.length));
      text.append ("\n");
    }

    text.append (String.format ("Buffer length: %,7d  %<04X   Data length: %,7d  %<04X",
        bufferLength, dataLength));

    return text.toString ();
  }
}
