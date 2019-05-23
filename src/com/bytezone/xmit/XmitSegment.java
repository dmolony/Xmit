package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
class XmitSegment extends Segment
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  @Override
  List<DataBlock> createDataBlocks ()                     // used only for data blocks
  // ---------------------------------------------------------------------------------//
  {
    int recLen = 0;
    int headerPtr = 0;
    Header header = null;
    DataBlock dataBlock = null;

    List<DataBlock> dataBlocks = new ArrayList<> ();
    boolean debug = false;

    if (debug)
    {
      System.out.println ("\nCreating data blocks");
      for (BlockPointer blockPointer : rawBlockPointers)
        System.out.println (blockPointer);
    }

    for (BlockPointer blockPointer : rawBlockPointers)
    {
      int ptr = blockPointer.offset;
      int avail = blockPointer.length;

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
            System.arraycopy (blockPointer.buffer, ptr, header.buffer, headerPtr, avail);
            ptr += avail;
            headerPtr += avail;
            avail = 0;
            break;
          }

          int needed = 12 - headerPtr;
          System.arraycopy (blockPointer.buffer, ptr, header.buffer, headerPtr, needed);
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
        dataBlock.add (new BlockPointer (blockPointer.buffer, ptr, len));
        ptr += len;
        avail -= len;
        recLen -= len;
      }
    }

    if (dataBlocks.size () > 0 && dataBlocks.get (0).getHeader ().isEmpty ())
      System.out.println ("empty header found");

    if (debug)
    {
      System.out.println ("\nReturning data blocks");
      for (DataBlock block : dataBlocks)
        System.out.println (block);
    }

    return dataBlocks;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] getRawBuffer ()                                 // contains no headers
  // ---------------------------------------------------------------------------------//
  {
    byte[] fullBlock = new byte[rawBufferLength];
    int ptr = 0;
    for (BlockPointer blockPointer : rawBlockPointers)
    {
      System.arraycopy (blockPointer.buffer, blockPointer.offset, fullBlock, ptr,
          blockPointer.length);
      ptr += blockPointer.length;
    }
    assert ptr == fullBlock.length;
    return fullBlock;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  int packBuffer (byte[] dataBuffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    for (BlockPointer blockPointer : rawBlockPointers)
    {
      System.arraycopy (blockPointer.buffer, blockPointer.offset, dataBuffer, ptr,
          blockPointer.length);
      ptr += blockPointer.length;
    }

    return ptr;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  byte[] getEightBytes ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] eightBytes = new byte[8];

    BlockPointer blockPointer = rawBlockPointers.get (0);
    System.arraycopy (blockPointer.buffer, blockPointer.offset, eightBytes, 0, 8);

    return eightBytes;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  boolean isXmit ()
  // ---------------------------------------------------------------------------------//
  {
    BlockPointer blockPointer = rawBlockPointers.get (0);
    return Utility.matches (XmitReader.INMR01, blockPointer.buffer,
        blockPointer.offset + 1);
  }
}
