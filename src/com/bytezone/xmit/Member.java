package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Member implements Iterable<DataBlock>
{
  private CatalogEntry catalogEntry;
  List<DataBlock> dataBlocks = new ArrayList<> ();
  List<DataBlock> extraDataBlocks = new ArrayList<> ();
  int length = 0;

  // ---------------------------------------------------------------------------------//
  // setCatalogEntry
  // ---------------------------------------------------------------------------------//

  void setCatalogEntry (CatalogEntry catalogEntry)
  {
    this.catalogEntry = catalogEntry;
  }

  // ---------------------------------------------------------------------------------//
  // add
  // ---------------------------------------------------------------------------------//

  void add (DataBlock dataBlock)
  {
    byte type = dataBlock.getType ();
    if (type == 0x00 || type == (byte) 0x80)
    {
      dataBlocks.add (dataBlock);
      length += dataBlock.getSize ();
    }
    else
      extraDataBlocks.add (dataBlock);
  }

  public long getDataLength ()
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
