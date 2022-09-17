package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public abstract class Segment
// -----------------------------------------------------------------------------------//
{
  int rawBufferLength;
  final List<BlockPointer> rawBlockPointers = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public int size ()
  // ---------------------------------------------------------------------------------//
  {
    return rawBlockPointers.size ();
  }

  // ---------------------------------------------------------------------------------//
  abstract List<DataBlock> createDataBlocks ();           // used only for data blocks
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  abstract public byte[] getRawBuffer ();                      // contains no headers
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  abstract int packBuffer (byte[] dataBuffer, int ptr);
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  abstract byte[] getEightBytes ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  abstract boolean isXmit ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  public void addBlockPointer (BlockPointer blockPointer)
  // ---------------------------------------------------------------------------------//
  {
    if (blockPointer.offset + blockPointer.length > blockPointer.buffer.length)
    {
      // FILE185.XMI / FILE234I
      System.out.println ("invalid block pointer");
      System.out.printf ("%06X  %02X  %06X%n", blockPointer.offset, blockPointer.length,
          blockPointer.buffer.length);
      return;
    }

    rawBlockPointers.add (blockPointer);
    rawBufferLength += blockPointer.length;               // used for non-data blocks
  }

  // ---------------------------------------------------------------------------------//
  public int getRawBufferLength ()
  // ---------------------------------------------------------------------------------//
  {
    return rawBufferLength;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    BlockPointer blockPointer = rawBlockPointers.get (0);
    return String.format ("%06X:   %06X  %<,7d  %,5d", blockPointer.offset, rawBufferLength,
        rawBlockPointers.size ());
  }
}
