package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class DataBlock
{
  int offset;
  byte[] header;
  List<BlockPointer> blockPointers = new ArrayList<> ();

  public DataBlock (int offset, byte[] header)
  {
    this.offset = offset;
    this.header = header;
  }

  public int getSize ()
  {
    return (int) Utility.getValue (header, 9, 3);
  }

  long getTtl ()
  {
    return Utility.getValue (header, 4, 5);
  }

  @Override
  public String toString ()
  {
    return String.format ("%06X: %s  %,7d", offset, Utility.getHexValues (header),
        getSize ());
  }
}
