package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

// ---------------------------------------------------------------------------------//
class XmitSegment
//---------------------------------------------------------------------------------//
{
  private int rawBufferLength;
  private final List<BlockPointer> rawBlockPointers = new ArrayList<> ();

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
        // BlockPointer dataBlockPointer = new BlockPointer (blockPointer.buffer, ptr, len);
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
  int packBuffer (byte[] dataBuffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    //    assert buffer.length >= ptr + rawBufferLength;

    for (BlockPointer blockPointer : rawBlockPointers)
    {
      System.arraycopy (blockPointer.buffer, blockPointer.offset, dataBuffer, ptr,
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
    return Utility.matches (XmitReader.INMR01, blockPointer.buffer,
        blockPointer.offset + 1);
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
}
