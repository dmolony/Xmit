package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Member implements Iterable<DataBlock>
{
  List<DataBlock> dataBlocks = new ArrayList<> ();
  List<DataBlock> extraDataBlocks = new ArrayList<> ();
  int length = 0;

  // ---------------------------------------------------------------------------------//
  // add
  // ---------------------------------------------------------------------------------//

  void add (DataBlock dataBlock)
  {
    byte type = dataBlock.getType ();
    if (type == (byte) 0x80 || type == 0x00)
    {
      dataBlocks.add (dataBlock);
      length += dataBlock.getSize ();
    }
    else
      extraDataBlocks.add (dataBlock);
  }

  // ---------------------------------------------------------------------------------//
  // getHeader
  // ---------------------------------------------------------------------------------//

  Header getHeader ()
  {
    return dataBlocks.get (0).getHeader ();
  }

  // ---------------------------------------------------------------------------------//
  // getDataBuffer
  // ---------------------------------------------------------------------------------//

  byte[] getDataBuffer ()
  {
    byte[] buffer = new byte[length];
    int ptr = 0;
    for (DataBlock dataBlock : dataBlocks)
      ptr = dataBlock.packBuffer (buffer, ptr);
    assert ptr == length;
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  // isXmit
  // ---------------------------------------------------------------------------------//

  boolean isXmit ()
  {
    return dataBlocks.get (0).isXmit ();
  }

  // ---------------------------------------------------------------------------------//
  // isRdw
  // ---------------------------------------------------------------------------------//

  boolean isRdw ()
  {
    DataBlock dataBlock = dataBlocks.get (0);
    //    int len = Utility.getTwoBytes (buffer, ptr)
    System.out.println ("Member not finished");
    return false;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    for (DataBlock dataBlock : dataBlocks)
    {
      text.append (dataBlock);
      text.append ("\n");
    }

    Utility.removeTrailingNewlines (text);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // iterator
  // ---------------------------------------------------------------------------------//

  @Override
  public Iterator<DataBlock> iterator ()
  {
    return dataBlocks.iterator ();
  }
}
