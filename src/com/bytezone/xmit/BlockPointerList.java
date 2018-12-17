package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockPointerList implements Iterable<BlockPointer>
{
  final List<BlockPointer> blockPointers = new ArrayList<> ();
  private final byte[] buffer;          // all block pointers refer to this
  private int bufferLength;
  private int dataLength;
  private boolean isBinary;
  private CatalogEntry catalogEntry;
  private byte sortKey;

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
    {
      // this is wrong, it assumes only one data record
      dataLength = (int) Utility.getValue (buffer, blockPointer.offset + 9, 3);
      setBinaryFlag (blockPointer);

      sortKey = buffer[blockPointer.offset + 8];
    }
  }

  // ---------------------------------------------------------------------------------//
  // getFirstHeader
  // ---------------------------------------------------------------------------------//

  String getFirstHeader ()
  {
    BlockPointer bp = blockPointers.get (0);
    return Utility.getHex (buffer, bp.offset, 12);
  }

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
  // mysteryMatches
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

  //  int countHeaders ()
  //  {
  //    return (bufferLength - dataLength) / 12;
  //  }

  // ---------------------------------------------------------------------------------//
  // isLastBlock
  // ---------------------------------------------------------------------------------//

  boolean isLastBlock ()
  {
    byte[] buffer = getBuffer ();       // expensive
    int ptr = 0;
    int dataLength = -1;
    while (ptr < buffer.length)
    {
      dataLength = Reader.getWord (buffer, ptr + 10);
      ptr += 12 + dataLength;
    }
    return dataLength == 0;
  }

  // ---------------------------------------------------------------------------------//
  // listHeaders
  // ---------------------------------------------------------------------------------//

  String listHeaders ()
  {
    StringBuilder text = new StringBuilder ();
    //    text.append ("----------------------< Block Pointer List >------------------\n");
    text.append (String.format ("Member        : %s%n", catalogEntry.getMemberName ()));
    text.append (String.format ("Block pointers: %d%n", size ()));
    text.append (String.format ("Buffer length : %d%n", bufferLength));
    text.append (String.format ("Data length   : %d%n%n", dataLength));

    int ptr = 0;
    int avail = 0;
    int recLen = 0;

    List<BlockPointer> newList = new ArrayList<> ();
    //    for (BlockPointer blockPointer : blockPointers)
    //    {
    //      System.out.println (blockPointer);
    //      System.out
    //          .println (Utility.toHex (buffer, blockPointer.offset, blockPointer.length));
    //    }

    for (BlockPointer blockPointer : blockPointers)
    {
      text.append (String.format ("BP: %s%n", blockPointer));

      ptr = blockPointer.offset;
      avail = blockPointer.length;

      while (avail > 0)
      {
        //        System.out.printf ("ptr: %06X  dataLength: %d  avail: %d%n", ptr, recLen, avail);
        if (recLen == 0)                // at a data header
        {
          recLen = Reader.getWord (buffer, ptr + 10);
          if (recLen == 0)
            break;
          ptr += 12;
          avail -= 12;
        }
        //        System.out.printf ("ptr: %06X  dataLength: %d  avail: %d%n", ptr, recLen, avail);

        int len = Math.min (recLen, avail);
        BlockPointer bp = new BlockPointer (buffer, ptr, len);
        newList.add (bp);
        ptr += len;
        avail -= len;
        recLen -= len;

        //        System.out.println (newList.get (newList.size () - 1));
      }
    }
    text.append ("\n");

    for (BlockPointer bp : newList)
      text.append (bp + "\n");

    text.append ("++++++++++ list old +++++++++++\n\n");
    byte[] buffer = getBuffer ();         // expensive
    //    System.out.println (Utility.toHex (buffer));
    ptr = 0;
    recLen = -1;

    while (ptr < buffer.length)
    {
      recLen = Reader.getWord (buffer, ptr + 10);
      text.append (String.format ("%06X  %s  %,7d%n", ptr,
          Utility.getHex (buffer, ptr, 12), recLen));
      ptr += 12 + recLen;
    }
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // listHeaders
  // ---------------------------------------------------------------------------------//

  void listHeaders2 ()
  {
    System.out.println ("----------------------< Block Pointer List >------------------");
    System.out.printf ("Member        : %s%n", catalogEntry.getMemberName ());
    System.out.printf ("Block pointers: %d%n", size ());
    System.out.printf ("Buffer length : %d%n", bufferLength);
    System.out.printf ("Data length   : %d%n", dataLength);

    int currentBlockPointer = 0;

    int ptr = blockPointers.get (0).offset;
    int bytesLeft = 0;
    int len = Utility.getWord (buffer, ptr);
    ptr += 12;

    //      dataLength = Reader.getWord (buffer, ptr + 10);
    //      System.out.printf ("%06X  %s  %,7d%n", ptr, Utility.getHex (buffer, ptr, 12),
    //          dataLength);
    //      ptr += 12 + dataLength;
    //    }
    //    return dataLength == 0;
  }

  // ---------------------------------------------------------------------------------//
  // getWord
  // ---------------------------------------------------------------------------------//

  int getWord (byte[] buffer, int ptr)
  {
    int b = (buffer[ptr] & 0xFF) << 8;
    int a = (buffer[ptr + 1] & 0xFF);
    return a + b;
  }

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

    text.append (String.format ("Buffer length: %04X  %<,8d%n", bufferLength));
    text.append (
        String.format ("Header length: %04X  %<,8d%n", bufferLength - dataLength));
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
