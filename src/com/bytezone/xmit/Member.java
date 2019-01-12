package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.xmit.textunit.Dsorg.Org;

public class Member implements Iterable<DataBlock>
{
  private final Org org;
  private final List<DataBlock> dataBlocks = new ArrayList<> ();
  private final List<DataBlock> extraDataBlocks = new ArrayList<> ();     // PDSE
  private int length = 0;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  Member (Org org)        // will be used for PS files too
  {
    this.org = org;
  }

  // ---------------------------------------------------------------------------------//
  // addPdsDataBlock
  // ---------------------------------------------------------------------------------//

  void addPdsDataBlock (DataBlock dataBlock)
  {
    byte type = dataBlock.getType ();
    if (type == 0x00 || type == (byte) 0x80)      // basic PDS data
    {
      dataBlocks.add (dataBlock);
      length += dataBlock.getSize ();
    }
    else                                          // additional PDSE blocks
      extraDataBlocks.add (dataBlock);
  }

  // ---------------------------------------------------------------------------------//
  // getDataLength
  // ---------------------------------------------------------------------------------//

  public int getDataLength ()
  {
    return length;
  }

  // ---------------------------------------------------------------------------------//
  // getExtraDataBlocks
  // ---------------------------------------------------------------------------------//

  List<DataBlock> getExtraDataBlocks ()
  {
    return extraDataBlocks;
  }

  // ---------------------------------------------------------------------------------//
  // getDataBuffer
  // ---------------------------------------------------------------------------------//

  public byte[] getDataBuffer ()
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

  public boolean isXmit ()
  {
    return dataBlocks.get (0).isXmit ();
  }

  // ---------------------------------------------------------------------------------//
  // isRdw
  // ---------------------------------------------------------------------------------//

  boolean isRdw ()
  {
    for (DataBlock dataBlock : dataBlocks)
      if (dataBlock.getTwoBytes () != dataBlock.getSize ())
        return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  // getEightBytes
  // ---------------------------------------------------------------------------------//

  byte[] getEightBytes ()
  {
    return dataBlocks.get (0).getEightBytes ();
  }

  // ---------------------------------------------------------------------------------//
  // size
  // ---------------------------------------------------------------------------------//

  int size ()
  {
    return dataBlocks.size ();
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    int count = 0;
    int total = 0;
    for (DataBlock dataBlock : dataBlocks)
    {
      total += dataBlock.getSize ();
      text.append (String.format ("   %3d  %s%n", count++, dataBlock));
    }

    text.append (String.format ("%44.44s %s%n", "", "------ ---------"));
    text.append (String.format ("%44.44s %06X %<,9d%n%n", "", total));

    for (DataBlock dataBlock : extraDataBlocks)
      text.append (String.format ("   %3d  %s%n", count++, dataBlock));

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
