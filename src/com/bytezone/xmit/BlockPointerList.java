package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockPointerList implements Iterable<BlockPointer>
{
  private final int id;
  private CatalogEntry catalogEntry;
  final List<BlockPointer> blockPointers = new ArrayList<> ();
  private final byte[] buffer;          // all block pointers refer to this
  private int bufferLength;

  private int dataLength;               // only applies to data records
  private boolean isBinary;
  private byte sortKey;

  private List<BlockPointer> newList;
  boolean isLastBlock = false;
  List<byte[]> headers = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public BlockPointerList (byte[] buffer, int id)
  {
    this.buffer = buffer;
    this.id = id;
  }

  // ---------------------------------------------------------------------------------//
  // add
  // ---------------------------------------------------------------------------------//

  public void add (BlockPointer blockPointer)
  {
    blockPointers.add (blockPointer);
    bufferLength += blockPointer.length;                // used for catalog blocks

    if (blockPointers.size () == 1)
    {
      setBinaryFlag (blockPointer);
      sortKey = buffer[blockPointer.offset + 8];        // used for data blocks
    }
  }

  // ---------------------------------------------------------------------------------//
  // build
  // ---------------------------------------------------------------------------------//

  void build ()         // used for data blocks
  {
    int recLen = 0;
    isLastBlock = false;
    newList = new ArrayList<> ();
    boolean debug = false;

    int headerPtr = 0;
    byte[] header = null;

    for (BlockPointer blockPointer : blockPointers)
    {
      int ptr = blockPointer.offset;
      int avail = blockPointer.length;

      if (debug)
        System.out.println (blockPointer);

      while (avail > 0)
      {
        if (debug)
          System.out.printf ("        %06X  %3d  %3d%n", ptr, avail, recLen);

        if (recLen == 0)                // at a data header
        {
          if (headerPtr == 0)
          {
            header = new byte[12];
            headers.add (header);
          }

          if (avail < 12 - headerPtr)
          {
            //            System.out.println ("part filling " + Utility.toHex (buffer, ptr, 14));
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

          recLen = Reader.getWord (header, 10);

          if (recLen == 0)
          {
            isLastBlock = true;
            break;
          }
          if (debug)
            System.out.printf ("        %06X  %3d  %3d%n", ptr, avail, recLen);
        }

        int len = Math.min (recLen, avail);
        BlockPointer bp = new BlockPointer (buffer, ptr, len);
        newList.add (bp);
        ptr += len;
        avail -= len;
        recLen -= len;
        if (debug)
          System.out.printf ("        %06X  %3d  %3d%n", ptr, avail, recLen);
      }
    }

    for (BlockPointer blockPointer : newList)
      dataLength += blockPointer.length;

    if (debug)
    {
      System.out.println ("new");
      for (BlockPointer blockPointer : newList)
        System.out.println (blockPointer);
      for (byte[] header2 : headers)
        System.out.println (Utility.toHex (header2));
    }
  }

  // ---------------------------------------------------------------------------------//
  // getFirstHeader
  // ---------------------------------------------------------------------------------//

  //  private String getFirstHeader ()
  //  {
  //    BlockPointer bp = blockPointers.get (0);
  //    return Utility.getHex (buffer, bp.offset, 12);
  //  }

  // ---------------------------------------------------------------------------------//
  // setBinaryFlag
  // ---------------------------------------------------------------------------------//

  private void setBinaryFlag (BlockPointer blockPointer)
  {
    for (int i = 0; i < 10; i++)
    {
      int b = buffer[blockPointer.offset + 12 + i] & 0xFF;
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
  // getBufferLength
  // ---------------------------------------------------------------------------------//

  //  public int getBufferLength ()
  //  {
  //    return bufferLength;
  //  }

  // ---------------------------------------------------------------------------------//
  // getDataLength
  // ---------------------------------------------------------------------------------//

  public int getDataLength ()
  {
    return dataLength;
  }

  // ---------------------------------------------------------------------------------//
  // isLastBlock
  // ---------------------------------------------------------------------------------//

  boolean isLastBlock ()
  {
    return isLastBlock;
  }

  // ---------------------------------------------------------------------------------//
  // isLastBlock
  // ---------------------------------------------------------------------------------//

  boolean isLastBlockOld ()
  {
    byte[] buffer = getBuffer ();       // expensive
    int ptr = 0;
    int dataLength = -1;
    while (ptr < buffer.length)
    {
      dataLength = Reader.getWord (buffer, ptr + 10);
      ptr += 12 + dataLength;
    }
    assert (isLastBlock == (dataLength == 0));
    //      System.out.printf ("*****************  mismatch in lastBlock (%d)  %s%n", id,
    //          isLastBlock);
    return dataLength == 0;
    //    return isLastBlock;
  }

  // ---------------------------------------------------------------------------------//
  // listHeaders
  // ---------------------------------------------------------------------------------//

  String listHeaders ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Member        : %s%n", catalogEntry.getMemberName ()));
    text.append (String.format ("Block pointers: %d%n", size ()));
    text.append (String.format ("Buffer length : %d%n", bufferLength));
    text.append (String.format ("Data length   : %d%n", dataLength));

    text.append ("\nOld list:\n");
    for (BlockPointer blockPointer : blockPointers)
    {
      text.append (String.format ("%s%n", blockPointer));
      text.append (Utility.toHex (buffer, blockPointer.offset, blockPointer.length));
      text.append ("\n");
    }

    text.append ("\nNew list:\n");
    for (BlockPointer blockPointer : newList)
    {
      text.append (String.format ("%s%n", blockPointer));
      text.append (Utility.toHex (buffer, blockPointer.offset, blockPointer.length));
      text.append ("\n");
    }

    text.append ("\nHeaders:\n");
    for (byte[] header : headers)
    {
      text.append (Utility.getHex (header));
      text.append ("\n");
    }

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // getWord
  // ---------------------------------------------------------------------------------//

  //  int getWord (byte[] buffer, int ptr)
  //  {
  //    int b = (buffer[ptr] & 0xFF) << 8;
  //    int a = (buffer[ptr + 1] & 0xFF);
  //    return a + b;
  //  }

  // ---------------------------------------------------------------------------------//
  // size
  // ---------------------------------------------------------------------------------//

  public int size ()
  {
    return blockPointers.size ();
  }

  // ---------------------------------------------------------------------------------//
  // isBinary
  // ---------------------------------------------------------------------------------//

  boolean isBinary ()
  {
    return isBinary;
  }

  // ---------------------------------------------------------------------------------//
  // getBuffer
  // ---------------------------------------------------------------------------------//

  byte[] getBuffer ()
  {
    //    System.out.println ("Getting full buffer");
    byte[] fullBlock = new byte[bufferLength];
    int ptr = 0;
    for (BlockPointer blockPointer : blockPointers)
    {
      System.arraycopy (buffer, blockPointer.offset, fullBlock, ptr, blockPointer.length);
      ptr += blockPointer.length;
    }
    assert ptr == bufferLength;
    return fullBlock;
  }

  // ---------------------------------------------------------------------------------//
  // getNewBuffer
  // ---------------------------------------------------------------------------------//

  byte[] getDataBuffer ()
  {
    //    System.out.println ("Getting data buffer");
    byte[] fullBlock = new byte[dataLength];
    int ptr = 0;
    for (BlockPointer blockPointer : newList)
    {
      System.arraycopy (buffer, blockPointer.offset, fullBlock, ptr, blockPointer.length);
      ptr += blockPointer.length;
    }
    assert ptr == dataLength;
    return fullBlock;
  }

  // ---------------------------------------------------------------------------------//
  // isXmit
  // ---------------------------------------------------------------------------------//

  private static byte[] INMR01 = { (byte) 0xE0, (byte) 0xC9, (byte) 0xD5, (byte) 0xD4,
                                   (byte) 0xD9, (byte) 0xF0, (byte) 0xF1 };

  boolean isXmit ()
  {
    BlockPointer blockPointer = blockPointers.get (0);
    return Reader.matches (INMR01, buffer, blockPointer.offset + 13);
  }

  // ---------------------------------------------------------------------------------//
  // dump
  // ---------------------------------------------------------------------------------//

  void dump ()
  {
    for (BlockPointer blockPointer : blockPointers)
      System.out.println (blockPointer.toHex ());
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    //    text.append (String.format ("Buffer length: %04X  %<,8d%n", bufferLength));
    //    text.append (
    //        String.format ("Header length: %04X  %<,8d%n", bufferLength - dataLength));
    text.append (String.format ("Data length  : %04X  %<,8d%n", dataLength));

    int count = 0;
    for (BlockPointer blockPointer : blockPointers)
    {
      text.append (
          String.format ("%nBlockPointer %d of %d%n", ++count, blockPointers.size ()));
      if (false)
      {
        text.append (blockPointer);
        text.append ("\n");
      }
      else
      {
        text.append (Utility.toHex (buffer, blockPointer.offset, blockPointer.length));
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
    return blockPointers.iterator ();
  }
}
