package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class BlockPointerList
{
  private final List<BlockPointer> blockPointers = new ArrayList<> ();
  private final byte[] buffer;
  private int length;

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
    length += blockPointer.length;
  }

  // ---------------------------------------------------------------------------------//
  // getLength
  // ---------------------------------------------------------------------------------//

  public int getDataLength ()
  {
    return length;
  }

  // ---------------------------------------------------------------------------------//
  // getWord
  // ---------------------------------------------------------------------------------//

  int getWord (int offset)
  {
    BlockPointer blockPointer = blockPointers.get (0);
    assert offset < blockPointer.length + 1;
    return Reader.getWord (buffer, blockPointer.start + offset);
  }

  // ---------------------------------------------------------------------------------//
  // getBuffer
  // ---------------------------------------------------------------------------------//

  public byte[] getBuffer ()
  {
    byte[] fullBlock = new byte[length];
    int ptr = 0;
    for (BlockPointer blockPointer : blockPointers)
    {
      System.arraycopy (buffer, blockPointer.start, fullBlock, ptr, blockPointer.length);
      ptr += blockPointer.length;
    }
    assert ptr == length;
    return fullBlock;
  }
}
