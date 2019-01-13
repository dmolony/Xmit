package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Segment implements Iterable<BlockPointer>
{
  private final byte[] buffer;          // all block pointers refer to this
  private int rawBufferLength;          // raw data length
  private final List<BlockPointer> rawBlockPointers = new ArrayList<> ();

  //  private boolean isBinary;
  //  private boolean isLastBlock;

  private final boolean shortDisplay = false;

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
      System.out.println ("invalid block pointer");
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
    //    setBinaryFlag (rawBlockPointers.get (0));

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
        //        dataBlockPointers.add (dataBlockPointer);
        dataBlock.add (dataBlockPointer);
        ptr += len;
        avail -= len;
        recLen -= len;
      }
    }

    //    dataBufferLength = 0;
    //    for (BlockPointer blockPointer : dataBlockPointers)
    //      dataBufferLength += blockPointer.length;

    return dataBlocks;
  }

  // ---------------------------------------------------------------------------------//
  // setBinaryFlag
  // ---------------------------------------------------------------------------------//

  //  private void setBinaryFlag (BlockPointer blockPointer)
  //  {
  //    //    for (int i = 0; i < 10; i++)
  //    //    {
  //    //      int ptr = blockPointer.offset + 12 + i;
  //    //      if (ptr >= buffer.length)
  //    //        break;
  //    //      int b = buffer[ptr] & 0xFF;
  //    //      if (b < 0x40 || b == 0xFF)
  //    //      {
  //    //        isBinary = true;
  //    //        break;
  //    //      }
  //    //    }
  //    isBinary =
  //        Utility.isBinary (buffer, blockPointer.offset + 12, blockPointer.length - 12);
  //  }

  // ---------------------------------------------------------------------------------//
  // ttlMatches
  // ---------------------------------------------------------------------------------//

  //  boolean ttlMatches (byte[] ttl)
  //  {
  //    return dataBlocks.get (0).ttlMatches (ttl);
  //  }

  // ---------------------------------------------------------------------------------//
  // getRawBufferLength
  // ---------------------------------------------------------------------------------//

  public int getRawBufferLength ()
  {
    return rawBufferLength;
  }

  // ---------------------------------------------------------------------------------//
  // getDataLength
  // ---------------------------------------------------------------------------------//

  //  public int getDataLength ()
  //  {
  //    return dataBufferLength;
  //  }

  // ---------------------------------------------------------------------------------//
  // isLastBlock
  // ---------------------------------------------------------------------------------//

  //  boolean isLastBlock ()
  //  {
  //    return isLastBlock;
  //  }

  // ---------------------------------------------------------------------------------//
  // listHeaders
  // ---------------------------------------------------------------------------------//

  String listHeaders ()
  {
    StringBuilder text = new StringBuilder ();

    if (shortDisplay)
    {
      text.append (String.format ("Raw blocks    : %d%n", rawBlockPointers.size ()));
      //      text.append (String.format ("Data blocks   : %d%n", dataBlockPointers.size ()));
      text.append (String.format ("Buffer length : %06X  %<,d%n", rawBufferLength));
      //      text.append (String.format ("Data length   : %06X  %<,d", dataBufferLength));
      return text.toString ();
    }

    int total1 = 0;
    int total2 = 0;
    //    int max = Math.max (rawBlockPointers.size (), dataBlockPointers.size ());
    int max = rawBlockPointers.size ();
    BlockPointer bp1, bp2;
    for (int i = 0; i < max; i++)
    {
      if (i < rawBlockPointers.size ())
      {
        bp1 = rawBlockPointers.get (i);
        total1 += bp1.length;
        text.append (String.format ("   %s ", bp1));
      }
      else
        text.append ("                        ");

      text.append (String.format (" :%3d : ", i));

      //      if (i < dataBlockPointers.size ())
      //      {
      //        bp2 = dataBlockPointers.get (i);
      //        total2 += bp2.length;
      //        text.append (String.format ("  %s", bp2));
      //      }
      text.append ("\n");
    }
    text.append (String.format ("            %04X%<,7d                    %04X%<,7d%n",
        total1, total2));

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // isBinary
  // ---------------------------------------------------------------------------------//

  //  boolean isBinary ()
  //  {
  //    return isBinary;
  //  }

  // ---------------------------------------------------------------------------------//
  // getEightBytes
  // ---------------------------------------------------------------------------------//

  byte[] getEightBytes ()
  {
    byte[] eightBytes = new byte[8];
    BlockPointer blockPointer = rawBlockPointers.get (0);
    System.arraycopy (buffer, blockPointer.offset, eightBytes, 0, eightBytes.length);
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

  int getRawBuffer (byte[] dataBuffer, int ptr)
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
  // getDataBuffer - contains headers which must be removed
  // ---------------------------------------------------------------------------------//

  //  private byte[] getDataBuffer ()
  //  {
  //    byte[] fullBlock = new byte[dataBufferLength];
  //    int ptr = 0;
  //    for (BlockPointer blockPointer : dataBlockPointers)
  //    {
  //      System.arraycopy (buffer, blockPointer.offset, fullBlock, ptr, blockPointer.length);
  //      ptr += blockPointer.length;
  //    }
  //    assert ptr == dataBufferLength;
  //    return fullBlock;
  //  }

  // ---------------------------------------------------------------------------------//
  // getDataBuffer - contains headers which must be removed
  // ---------------------------------------------------------------------------------//

  //  private int getDataBuffer (byte[] dataBuffer, int ptr)
  //  {
  //    assert buffer.length >= ptr + dataBufferLength;
  //
  //    for (BlockPointer blockPointer : dataBlockPointers)
  //    {
  //      System.arraycopy (buffer, blockPointer.offset, dataBuffer, ptr,
  //          blockPointer.length);
  //      ptr += blockPointer.length;
  //    }
  //
  //    return ptr;
  //  }

  // ---------------------------------------------------------------------------------//
  // isXmit
  // ---------------------------------------------------------------------------------//

  private static byte[] INMR01 = { (byte) 0xE0, (byte) 0xC9, (byte) 0xD5, (byte) 0xD4,
                                   (byte) 0xD9, (byte) 0xF0, (byte) 0xF1 };

  boolean isXmit ()
  {
    BlockPointer blockPointer = rawBlockPointers.get (0);
    return Utility.matches (INMR01, buffer, blockPointer.offset + 13);
  }

  // ---------------------------------------------------------------------------------//
  // dump
  // ---------------------------------------------------------------------------------//

  void dump ()
  {
    for (BlockPointer blockPointer : rawBlockPointers)
      System.out.println (blockPointer.toHex ());
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Data length  : %04X  %<,8d%n", rawBufferLength));

    int count = 0;
    for (BlockPointer blockPointer : rawBlockPointers)
    {
      //      text.append (
      //          String.format ("%nBlockPointer %d of %d%n", ++count, rawBlockPointers.size ()));
      if (true)
      {
        text.append ("      ");
        text.append (blockPointer);
        text.append ("\n");
      }
      else
      {
        text.append (
            Utility.getHexDump (buffer, blockPointer.offset, blockPointer.length));
        text.append ("\n");
      }
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // Iterator
  // ---------------------------------------------------------------------------------//

  @Override
  public Iterator<BlockPointer> iterator ()
  {
    return rawBlockPointers.iterator ();
  }
}
