package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// ---------------------------------------------------------------------------------//
class XmitSegment implements Iterable<BlockPointer>
//---------------------------------------------------------------------------------//
{
  private final byte[] buffer;          // all block pointers refer to this
  private int rawBufferLength;
  private final List<BlockPointer> rawBlockPointers = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  XmitSegment (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    this.buffer = buffer;
  }

  // ---------------------------------------------------------------------------------//
  public int size ()
  // ---------------------------------------------------------------------------------//
  {
    return rawBlockPointers.size ();
  }

  // ---------------------------------------------------------------------------------//
  public void addBlockPointer (BlockPointer blockPointer)
  // ---------------------------------------------------------------------------------//
  {
    if (blockPointer.offset + blockPointer.length > buffer.length)
    {
      // FILE185.XMI / FILE234I
      System.out.println ("invalid block pointer");
      System.out.printf ("%06X  %02X  %06X%n", blockPointer.offset, blockPointer.length,
          buffer.length);
      return;
    }
    rawBlockPointers.add (blockPointer);
    rawBufferLength += blockPointer.length;               // used for non-data blocks
  }

  // ---------------------------------------------------------------------------------//
  List<DataBlock> createDataBlocks ()                     // used only for data blocks
  // ---------------------------------------------------------------------------------//
  {
    int recLen = 0;
    int headerPtr = 0;
    Header header = null;
    DataBlock dataBlock = null;
    List<DataBlock> dataBlocks = new ArrayList<> ();

    for (BlockPointer rawBlockPointer : rawBlockPointers)
    {
      int ptr = rawBlockPointer.offset;
      int avail = rawBlockPointer.length;

      while (avail > 0)
      {
        if (recLen == 0)                // at a data header
        {
          if (headerPtr == 0)
          {
            header = new Header ();
            dataBlock = new DataBlock (ptr, header);
            dataBlocks.add (dataBlock);
          }

          if (avail < 12 - headerPtr)
          {
            System.arraycopy (buffer, ptr, header.buffer, headerPtr, avail);
            ptr += avail;
            headerPtr += avail;
            avail = 0;
            break;
          }

          int needed = 12 - headerPtr;
          System.arraycopy (buffer, ptr, header.buffer, headerPtr, needed);
          ptr += needed;
          avail -= needed;
          headerPtr = 0;

          recLen = header.getSize ();

          if (recLen == 0)
          {
            if (header.isEmpty ())                // all zeroes
              dataBlocks.remove (dataBlock);
            break;
          }
        }

        int len = Math.min (recLen, avail);
        BlockPointer dataBlockPointer = new BlockPointer (buffer, ptr, len);
        dataBlock.add (dataBlockPointer);
        ptr += len;
        avail -= len;
        recLen -= len;
      }
    }

    if (dataBlocks.size () > 0 && dataBlocks.get (0).getHeader ().isEmpty ())
      System.out.println ("empty header found");

    return dataBlocks;
  }

  // ---------------------------------------------------------------------------------//
  public int getRawBufferLength ()
  // ---------------------------------------------------------------------------------//
  {
    return rawBufferLength;
  }

  // ---------------------------------------------------------------------------------//
  byte[] getEightBytes ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] eightBytes = new byte[8];

    BlockPointer blockPointer = rawBlockPointers.get (0);
    System.arraycopy (blockPointer.buffer, blockPointer.offset, eightBytes, 0,
        eightBytes.length);

    return eightBytes;
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getRawBuffer ()                           // contains no headers
  // ---------------------------------------------------------------------------------//
  {
    byte[] fullBlock = new byte[rawBufferLength];
    int ptr = 0;
    for (BlockPointer blockPointer : rawBlockPointers)
    {
      System.arraycopy (buffer, blockPointer.offset, fullBlock, ptr, blockPointer.length);
      ptr += blockPointer.length;
    }
    assert ptr == rawBufferLength;
    return fullBlock;
  }

  // ---------------------------------------------------------------------------------//
  int packBuffer (byte[] dataBuffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    assert buffer.length >= ptr + rawBufferLength;

    for (BlockPointer blockPointer : rawBlockPointers)
    {
      System.arraycopy (buffer, blockPointer.offset, dataBuffer, ptr,
          blockPointer.length);
      ptr += blockPointer.length;
    }

    return ptr;
  }

  // ---------------------------------------------------------------------------------//
  boolean isXmit ()
  // ---------------------------------------------------------------------------------//
  {
    BlockPointer blockPointer = rawBlockPointers.get (0);
    return Utility.matches (XmitReader.INMR01, blockPointer.buffer, blockPointer.offset + 1);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    BlockPointer blockPointer = rawBlockPointers.get (0);
    return String.format ("%06X:   %06X  %<,7d  %,5d", blockPointer.offset,
        rawBufferLength, rawBlockPointers.size ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<BlockPointer> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    return rawBlockPointers.iterator ();
  }
}
