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

    int count = 0;
    for (int i = 2; i < rawBlockPointers.size (); i++)
    {
      BlockPointer blockPointer = rawBlockPointers.get (i);

      int ptr = blockPointer.offset + 8;
      //      if (blockPointer.buffer[ptr + 9] == 0x08)      // skip catalog entries
      //        continue;

      while (ptr < blockPointer.offset + blockPointer.length)
      {
        System.out.printf ("%3d  %s%n", ++count,
            Utility.getHexDump (blockPointer.buffer, ptr, 12));
        int len = Utility.getTwoBytes (blockPointer.buffer, ptr + 10);
        ptr += len + 12;
      }
    }

    return dataBlocks;
  }

  //---------------------------------------------------------------------------------//
  @Override
  byte[] getEightBytes ()
  //---------------------------------------------------------------------------------//
  {
    return null;
  }

  //---------------------------------------------------------------------------------//
  @Override
  public byte[] getRawBuffer ()
  //---------------------------------------------------------------------------------//
  {
    return null;
  }

  //---------------------------------------------------------------------------------//
  @Override
  int packBuffer (byte[] dataBuffer, int ptr)
  //---------------------------------------------------------------------------------//
  {
    return 0;
  }

  //---------------------------------------------------------------------------------//
  @Override
  boolean isXmit ()
  //---------------------------------------------------------------------------------//
  {
    return false;
  }
}
