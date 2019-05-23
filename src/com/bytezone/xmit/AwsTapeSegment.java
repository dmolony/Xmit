package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class AwsTapeSegment extends Segment
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  @Override
  List<DataBlock> createDataBlocks ()                     // used only for data blocks
  // ---------------------------------------------------------------------------------//
  {
    List<DataBlock> dataBlocks = new ArrayList<> ();
    BlockPointer blockPointer = rawBlockPointers.get (0);
    int ptr = blockPointer.offset + 8;

    while (ptr < blockPointer.offset + blockPointer.length)
    {
      Header header = new Header ();
      DataBlock dataBlock = new DataBlock (ptr, header);
      System.arraycopy (blockPointer.buffer, ptr, header.buffer, 0, 12);
      int len = Utility.getTwoBytes (blockPointer.buffer, ptr + 10);
      dataBlock.add (new BlockPointer (blockPointer.buffer, ptr + 12, len));
      dataBlocks.add (dataBlock);
      ptr += len + 12;
    }

    return dataBlocks;
  }

  //---------------------------------------------------------------------------------//
  @Override
  public byte[] getRawBuffer ()                                 // contains no headers
  //---------------------------------------------------------------------------------//
  {
    return rawBlockPointers.get (0).getData (8);
  }

  //---------------------------------------------------------------------------------//
  @Override
  int packBuffer (byte[] dataBuffer, int ptr)
  //---------------------------------------------------------------------------------//
  {
    System.out.println ("packBuffer not writted");
    return 0;
  }

  //---------------------------------------------------------------------------------//
  @Override
  byte[] getEightBytes ()
  //---------------------------------------------------------------------------------//
  {
    byte[] eightBytes = new byte[8];

    BlockPointer blockPointer = rawBlockPointers.get (0);
    System.arraycopy (blockPointer.buffer, blockPointer.offset + 8, eightBytes, 0, 8);

    return eightBytes;
  }

  //---------------------------------------------------------------------------------//
  @Override
  boolean isXmit ()
  //---------------------------------------------------------------------------------//
  {
    System.out.println ("isXmit not writted");
    return false;
  }
}
