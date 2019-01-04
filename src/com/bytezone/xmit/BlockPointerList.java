package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockPointerList implements Iterable<BlockPointer>
{
  private final byte[] buffer;          // all block pointers refer to this

  private int rawBufferLength;          // raw data length
  private int dataBufferLength;         // raw data minus headers

  private final List<BlockPointer> rawBlockPointers = new ArrayList<> ();
  private final List<BlockPointer> dataBlockPointers = new ArrayList<> ();

  //  private final List<byte[]> pdsHeaders = new ArrayList<> ();
  //  private final List<Integer> pdsHeaderOffsets = new ArrayList<> ();
  final List<DataBlock> dataBlocks = new ArrayList<> ();

  private CatalogEntry catalogEntry;

  private boolean isBinary;
  private byte sortKey;                 // value contained in first header
  private boolean isLastBlock;

  private final boolean shortDisplay = false;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public BlockPointerList (byte[] buffer)
  {
    this.buffer = buffer;
  }

  // ---------------------------------------------------------------------------------//
  // addSegment
  // ---------------------------------------------------------------------------------//

  public void addSegment (boolean firstSegment, boolean lastSegment,
      BlockPointer blockPointer)
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

  void createDataBlocks ()                        // used only for data blocks
  {
    setBinaryFlag (rawBlockPointers.get (0));
    sortKey = buffer[rawBlockPointers.get (0).offset + 8];

    int recLen = 0;
    int headerPtr = 0;
    byte[] header = null;
    DataBlock dataBlock = null;

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
            header = new byte[12];
            //            pdsHeaders.add (header);
            //            pdsHeaderOffsets.add (ptr);
            dataBlock = new DataBlock (ptr, header);
            dataBlocks.add (dataBlock);
          }

          if (avail < 12 - headerPtr)
          {
            System.arraycopy (buffer, ptr, header, headerPtr, avail);
            ptr += avail;
            headerPtr += avail;
            avail = 0;
            break;
          }

          int needed = 12 - headerPtr;
          System.arraycopy (buffer, ptr, header, headerPtr, needed);
          ptr += needed;
          avail -= needed;
          headerPtr = 0;

          recLen = Utility.getTwoBytes (header, 10);

          if (recLen == 0)
          {
            isLastBlock = true;
            break;
          }
        }

        int len = Math.min (recLen, avail);
        dataBlockPointers.add (new BlockPointer (buffer, ptr, len));
        ptr += len;
        avail -= len;
        recLen -= len;
      }
    }

    dataBufferLength = 0;
    for (BlockPointer blockPointer : dataBlockPointers)
      dataBufferLength += blockPointer.length;
  }

  // ---------------------------------------------------------------------------------//
  // getOffset
  // ---------------------------------------------------------------------------------//

  int getOffset ()
  {
    //    return (int) Utility.getValue (pdsHeaders.get (0), 6, 3);
    return (int) Utility.getValue (dataBlocks.get (0).header, 6, 3);
  }

  // ---------------------------------------------------------------------------------//
  // isPDSE
  // ---------------------------------------------------------------------------------//

  boolean isPDSE ()
  {
    //    return pdsHeaders.get (0)[0] == (byte) 0x88;
    return dataBlocks.get (0).header[0] == (byte) 0x88;
  }

  // ---------------------------------------------------------------------------------//
  // setBinaryFlag
  // ---------------------------------------------------------------------------------//

  private void setBinaryFlag (BlockPointer blockPointer)
  {
    for (int i = 0; i < 10; i++)
    {
      int ptr = blockPointer.offset + 12 + i;
      if (ptr >= buffer.length)
        break;
      int b = buffer[ptr] & 0xFF;
      if (b < 0x40 || b == 0xFF)
      {
        isBinary = true;
        break;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // sortKeyMatches
  // ---------------------------------------------------------------------------------//

  boolean sortKeyMatches (byte b)
  {
    return b == sortKey;
  }

  // ---------------------------------------------------------------------------------//
  // setCatalogEntry
  // ---------------------------------------------------------------------------------//

  void setCatalogEntry (CatalogEntry catalogEntry)
  {
    this.catalogEntry = catalogEntry;
  }

  // ---------------------------------------------------------------------------------//
  // getDataLength
  // ---------------------------------------------------------------------------------//

  public int getDataLength ()
  {
    return dataBufferLength;
  }

  // ---------------------------------------------------------------------------------//
  // getRawBufferLength
  // ---------------------------------------------------------------------------------//

  public int getRawBufferLength ()
  {
    return rawBufferLength;
  }

  // ---------------------------------------------------------------------------------//
  // isLastBlock
  // ---------------------------------------------------------------------------------//

  boolean isLastBlock ()
  {
    return isLastBlock;
  }

  // ---------------------------------------------------------------------------------//
  // listHeaders
  // ---------------------------------------------------------------------------------//

  String listHeaders ()
  {
    StringBuilder text = new StringBuilder ();

    if (catalogEntry != null)
    {
      int headerOffset = (int) Utility.getValue (dataBlocks.get (0).header, 6, 3);
      int diff = headerOffset - catalogEntry.getOffset ();
      text.append (String.format ("Member         : %s  %06X  Diff: %04X%n",
          catalogEntry.getMemberName (), catalogEntry.getOffset (), diff));
    }

    if (shortDisplay)
    {
      text.append (String.format ("Raw blocks    : %d%n", rawBlockPointers.size ()));
      text.append (String.format ("Data blocks   : %d%n", dataBlockPointers.size ()));
      text.append (String.format ("Buffer length : %06X  %<,d%n", rawBufferLength));
      text.append (String.format ("Data length   : %06X  %<,d%n", dataBufferLength));
    }
    else
    {

      text.append ("\nHeaders:\n");
      for (int i = 0; i < dataBlocks.size (); i++)
      {
        byte[] header = dataBlocks.get (i).header;
        int offset = dataBlocks.get (i).offset;
        text.append (String.format ("   %06X: ", offset));
        text.append (Utility.getHexValues (header));
        text.append ("\n");
      }

      text.append ("\nBlock pointers:\n");
      int total1 = 0;
      int total2 = 0;
      int max = Math.max (rawBlockPointers.size (), dataBlockPointers.size ());
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

        if (i < dataBlockPointers.size ())
        {
          bp2 = dataBlockPointers.get (i);
          total2 += bp2.length;
          text.append (String.format ("  %s", bp2));
        }
        text.append ("\n");
      }
      text.append (String.format ("            %04X%<,7d                    %04X%<,7d%n",
          total1, total2));
    }

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // isBinary
  // ---------------------------------------------------------------------------------//

  boolean isBinary ()
  {
    return isBinary;
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
    assert buffer.length >= ptr + dataBufferLength;

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

  byte[] getDataBuffer ()
  {
    byte[] fullBlock = new byte[dataBufferLength];
    int ptr = 0;
    for (BlockPointer blockPointer : dataBlockPointers)
    {
      System.arraycopy (buffer, blockPointer.offset, fullBlock, ptr, blockPointer.length);
      ptr += blockPointer.length;
    }
    assert ptr == dataBufferLength;
    return fullBlock;
  }

  // ---------------------------------------------------------------------------------//
  // getDataBuffer - contains headers which must be removed
  // ---------------------------------------------------------------------------------//

  int getDataBuffer (byte[] dataBuffer, int ptr)
  {
    assert buffer.length >= ptr + dataBufferLength;

    for (BlockPointer blockPointer : dataBlockPointers)
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

    text.append (String.format ("Data length  : %04X  %<,8d%n", dataBufferLength));

    int count = 0;
    for (BlockPointer blockPointer : rawBlockPointers)
    {
      text.append (
          String.format ("%nBlockPointer %d of %d%n", ++count, rawBlockPointers.size ()));
      if (false)
      {
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
