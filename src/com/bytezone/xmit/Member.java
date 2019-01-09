package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Member implements Iterable<DataBlock>
{
  List<DataBlock> dataBlocks = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // add
  // ---------------------------------------------------------------------------------//

  void add (DataBlock dataBlock)
  {
    dataBlocks.add (dataBlock);
  }

  // ---------------------------------------------------------------------------------//
  // getHeader
  // ---------------------------------------------------------------------------------//

  byte[] getHeader ()
  {
    return dataBlocks.get (0).getHeader ();
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
