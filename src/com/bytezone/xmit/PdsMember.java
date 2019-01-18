package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PdsMember extends NamedData implements Iterable<DataBlock>
{
  CatalogEntry catalogEntry;
  private final List<DataBlock> dataBlocks;                    // PDS
  private final List<DataBlock> extraDataBlocks;               // PDSE

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  PdsMember (Disposition disposition)
  {
    super (disposition);

    dataBlocks = new ArrayList<> ();
    extraDataBlocks = new ArrayList<> ();
  }

  // ---------------------------------------------------------------------------------//
  // size
  // ---------------------------------------------------------------------------------//

  int size ()
  {
    return dataBlocks.size ();
  }

  // ---------------------------------------------------------------------------------//
  // setCatalogEntry
  // ---------------------------------------------------------------------------------//

  void setCatalogEntry (CatalogEntry catalogEntry)
  {
    this.catalogEntry = catalogEntry;
    this.name = catalogEntry.getMemberName ();
  }

  // ---------------------------------------------------------------------------------//
  // getCatalogEntry
  // ---------------------------------------------------------------------------------//

  public CatalogEntry getCatalogEntry ()
  {
    return catalogEntry;
  }

  // ---------------------------------------------------------------------------------//
  // addDataBlock
  // ---------------------------------------------------------------------------------//

  void addDataBlock (DataBlock dataBlock)
  {
    byte type = dataBlock.getType ();
    if (type == 0x00 || type == (byte) 0x80)      // basic PDS data
    {
      dataBlocks.add (dataBlock);
      dataLength += dataBlock.getSize ();
    }
    else                                          // additional PDSE blocks
      extraDataBlocks.add (dataBlock);
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

  @Override
  public byte[] getDataBuffer ()
  {
    byte[] buffer = new byte[dataLength];
    int ptr = 0;

    for (DataBlock dataBlock : dataBlocks)
      ptr = dataBlock.packBuffer (buffer, ptr);

    assert ptr == dataLength;
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  // getDataBuffer
  // ---------------------------------------------------------------------------------//

  @Override
  public byte[] getDataBuffer (int limit)
  {
    int length = 0;
    List<DataBlock> tmpBlocks = new ArrayList<> ();
    for (DataBlock dataBlock : dataBlocks)
    {
      tmpBlocks.add (dataBlock);
      length += dataBlock.getSize ();
      if (length >= limit)
        break;
    }

    byte[] buffer = new byte[length];
    int ptr = 0;

    for (DataBlock dataBlock : tmpBlocks)
      ptr = dataBlock.packBuffer (buffer, ptr);

    assert ptr == length;
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  // isXmit
  // ---------------------------------------------------------------------------------//

  @Override
  public boolean isXmit ()
  {
    return dataBlocks.get (0).isXmit ();
  }

  // ---------------------------------------------------------------------------------//
  // isRdw
  // ---------------------------------------------------------------------------------//

  @Override
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

  @Override
  byte[] getEightBytes ()
  {
    return dataBlocks.get (0).getEightBytes ();
  }

  // ---------------------------------------------------------------------------------//
  // pds
  // ---------------------------------------------------------------------------------//

  @Override
  void createLines ()
  {
    byte[] buffer = getDataBuffer ();
    int ptr = 0;
    int length = buffer.length;
    while (length > 0)
    {
      int len = Math.min (disposition.lrecl == 0 ? 80 : disposition.lrecl, length);
      lines.add (Utility.getString (buffer, ptr, len).stripTrailing ());
      ptr += len;
      length -= len;
    }
  }

  // ---------------------------------------------------------------------------------//
  // hexDump
  // ---------------------------------------------------------------------------------//

  @Override
  void hexDump ()
  {
    for (DataBlock dataBlock : dataBlocks)
    {
      byte[] buffer = dataBlock.getBuffer ();
      String[] chunks = Utility.getHexDump (buffer).split ("\n");
      for (String chunk : chunks)
        lines.add (chunk);
      if (lines.size () > 5000)
        break;
      lines.add ("");
    }
  }

  // ---------------------------------------------------------------------------------//
  // rdw
  // ---------------------------------------------------------------------------------//

  @Override
  void rdw ()         // see SOURCE.XMI
  {
    for (DataBlock dataBlock : dataBlocks)
    {
      byte[] buffer = dataBlock.getBuffer ();

      int ptr = 4;
      while (ptr < buffer.length && lines.size () < 2000)
      {
        int len = Utility.getTwoBytes (buffer, ptr);

        if (Utility.isBinary (buffer, ptr + 4, len - 4))
        {
          String text = Utility.getHexDump (buffer, ptr + 4, len - 4);
          String[] chunks = text.split ("\n");
          for (String chunk : chunks)
            lines.add (chunk);
          lines.add ("");
        }
        else
          lines.add (Utility.getString (buffer, ptr + 4, len - 4));

        ptr += len;
      }
    }
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

    text.append (
        "\n    #   Offset     Header                    Data      Data      Ptr\n");
    text.append (
        "  ----  ------  -----------------------------------  --------    ---\n");
    for (DataBlock dataBlock : dataBlocks)          // PDS
    {
      total += dataBlock.getSize ();
      text.append (String.format ("   %3d  %s%n", count++, dataBlock));
    }
    text.append (String.format ("%42.42s %s%n", "", "--------  --------"));

    int b1 = (total & 0xFF0000) >>> 16;
    int b2 = (total & 0x00FF00) >>> 8;
    int b3 = (total & 0x0000FF);
    text.append (
        String.format ("%42.42s %02X %02X %02X %,9d%n%n", "", b1, b2, b3, total));

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
