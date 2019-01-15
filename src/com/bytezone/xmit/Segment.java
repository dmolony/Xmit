package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Segment implements Iterable<BlockPointer>
{
  private final byte[] buffer;          // all block pointers refer to this
  private int rawBufferLength;
  private final List<BlockPointer> rawBlockPointers = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Segment (byte[] buffer)
  {
    this.buffer = buffer;
  }

  // ---------------------------------------------------------------------------------//
  // size
  // ---------------------------------------------------------------------------------//

  public int size ()
  {
    return rawBlockPointers.size ();
  }

  // ---------------------------------------------------------------------------------//
  // addBlockPointer
  // ---------------------------------------------------------------------------------//

  public void addBlockPointer (BlockPointer blockPointer)
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
    rawBufferLength += blockPointer.length;       // used for non-data blocks
  }

  // ---------------------------------------------------------------------------------//
  // createDataBlocks
  // ---------------------------------------------------------------------------------//

  List<DataBlock> createDataBlocks ()                     // used only for data blocks
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
            //            isLastBlock = true;
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

    return dataBlocks;
  }

  // ---------------------------------------------------------------------------------//
  // getRawBufferLength
  // ---------------------------------------------------------------------------------//

  public int getRawBufferLength ()
  {
    return rawBufferLength;
  }

  // ---------------------------------------------------------------------------------//
  // getEightBytes
  // ---------------------------------------------------------------------------------//

  byte[] getEightBytes ()
  {
    byte[] eightBytes = new byte[8];

    BlockPointer blockPointer = rawBlockPointers.get (0);
    System.arraycopy (blockPointer.buffer, blockPointer.offset, eightBytes, 0,
        eightBytes.length);

    return eightBytes;
  }

  // ---------------------------------------------------------------------------------//
  // getRawBuffer - contains no headers
  // ---------------------------------------------------------------------------------//

  public byte[] getRawBuffer ()
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
  // getRawBuffer - contains no headers
  // ---------------------------------------------------------------------------------//

  int packBuffer (byte[] dataBuffer, int ptr)
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
  // isXmit
  // ---------------------------------------------------------------------------------//

  boolean isXmit ()
  {
    BlockPointer blockPointer = rawBlockPointers.get (0);
    return Utility.matches (Reader.INMR01, blockPointer.buffer, blockPointer.offset + 1);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  //  @Override
  //  public String toString ()
  //  {
  //    StringBuilder text = new StringBuilder ();
  //
  //    text.append (String.format ("Data length  : %04X  %<,8d%n", rawBufferLength));
  //
  //    for (BlockPointer blockPointer : rawBlockPointers)
  //    {
  //      text.append ("      ");
  //      text.append (blockPointer);
  //      text.append ("\n");
  //    }
  //
  //    return text.toString ();
  //  }

  // ---------------------------------------------------------------------------------//
  // Iterator
  // ---------------------------------------------------------------------------------//

  @Override
  public Iterator<BlockPointer> iterator ()
  {
    return rawBlockPointers.iterator ();
  }
}
