package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

// -----------------------------------------------------------------------------------//
public class PdsMember extends DataFile implements Iterable<DataBlock>
// -----------------------------------------------------------------------------------//
{
  private final List<DataBlock> dataBlocks;                    // PDS & PDS/E
  private final List<DataBlock> extraDataBlocks;               // PDSE only
  private final Map<Integer, SizeCount> sizeCounts = new TreeMap<> ();
  private CatalogEntry catalogEntry;

  // ---------------------------------------------------------------------------------//
  PdsMember (Dataset dataset, Disposition disposition)
  // ---------------------------------------------------------------------------------//
  {
    super (dataset, disposition);

    dataBlocks = new ArrayList<> ();
    extraDataBlocks = new ArrayList<> ();
  }

  // ---------------------------------------------------------------------------------//
  void setCatalogEntry (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    this.catalogEntry = catalogEntry;
    setName (catalogEntry.getMemberName ());
  }

  // ---------------------------------------------------------------------------------//
  void addDataBlock (DataBlock dataBlock)
  // ---------------------------------------------------------------------------------//
  {
    byte type = dataBlock.getType ();
    if (type == 0x00 || type == (byte) 0x80)      // basic PDS data
    {
      dataBlocks.add (dataBlock);
      incrementDataLength (dataBlock.getSize ());
      incrementSizeCount (dataBlock.getSize ());
    }
    else                                          // additional PDS/E blocks
      extraDataBlocks.add (dataBlock);
  }

  // ---------------------------------------------------------------------------------//
  private void incrementSizeCount (int size)
  // ---------------------------------------------------------------------------------//
  {
    if (size == 0)          // ignore eof block
      return;

    SizeCount sizeCount = sizeCounts.get (size);
    if (sizeCount == null)
    {
      sizeCount = new SizeCount (size);
      sizeCounts.put (size, sizeCount);
    }

    sizeCount.increment ();
  }

  // ---------------------------------------------------------------------------------//
  int getCommonBlockLength ()
  // ---------------------------------------------------------------------------------//
  {
    if (sizeCounts.size () != 1)      // contains multiple sizes (or none)
      return 0;

    Entry<Integer, SizeCount> entry = sizeCounts.entrySet ().iterator ().next ();
    return entry == null ? 0 : entry.getValue ().blockSize;
  }

  // ---------------------------------------------------------------------------------//
  public void listSizeCounts (List<String> lines)
  // ---------------------------------------------------------------------------------//
  {
    lines.add ("    #       Size    Count      Total");
    String line = "  ----   -------    -----  ---------";
    lines.add (line);

    int seq = 0;
    int grandTotalSize = 0;
    int grandTotalBlocks = 0;

    for (SizeCount sizeCount : sizeCounts.values ())
    {
      int total = sizeCount.blockSize * sizeCount.count;
      lines.add (String.format (" %,5d   %,7d    %,5d  %,9d", ++seq, sizeCount.blockSize,
          sizeCount.count, total));
      grandTotalSize += total;
      grandTotalBlocks += sizeCount.count;
    }

    lines.add (line);
    lines.add (String.format ("%19s %,5d  %,9d", "", grandTotalBlocks, grandTotalSize));
  }

  // ---------------------------------------------------------------------------------//
  List<DataBlock> getExtraDataBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    return extraDataBlocks;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] getDataBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = new byte[getDataLength ()];
    int ptr = 0;

    for (DataBlock dataBlock : dataBlocks)
      ptr = dataBlock.packBuffer (buffer, ptr);

    assert ptr == getDataLength ();
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] getDataBuffer (int limit)
  // ---------------------------------------------------------------------------------//
  {
    if (getDataLength () <= limit)
      return getDataBuffer ();

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
  @Override
  public boolean isXmit ()
  // ---------------------------------------------------------------------------------//
  {
    return dataBlocks.get (0).isXmit ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  boolean isRdw ()
  // ---------------------------------------------------------------------------------//
  {
    for (DataBlock dataBlock : dataBlocks)
      if (dataBlock.getTwoBytes () != dataBlock.getSize ())
        return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void rdwLines ()         // see SOURCE.XMI
  // ---------------------------------------------------------------------------------//
  {
    for (DataBlock dataBlock : dataBlocks)
    {
      if (dataBlock.getSize () == 0)
        continue;

      byte[] buffer = dataBlock.getBuffer ();

      int ptr = 4;

      // check first record for binary zeroes
      int len = Utility.getTwoBytes (buffer, ptr);
      boolean isBinary = Utility.isBinary (buffer, ptr + 4, len - 4);

      while (ptr < buffer.length)
      {
        len = Utility.getTwoBytes (buffer, ptr);

        if (isBinary)
        {
          lines.add (Utility.getHexDump (buffer, ptr + 4, len - 4));
          lines.add ("");
        }
        else
          lines
              .add (Utility.translateUnicode (buffer, ptr + 4, len - 4).stripTrailing ());

        ptr += len;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void undefined ()         // recfm = U
  // ---------------------------------------------------------------------------------//
  {
    if (catalogEntry == null || catalogEntry.isLoadModule ()
        || getCommonBlockLength () <= 1)
    {
      if (getEightBytes ()[0] == 0x20)
        loadModule ();
      else
        hexDump ();
    }
    else
      for (DataBlock block : dataBlocks)
        lines.add (Utility.getString (block.getBuffer ()));
  }

  // ---------------------------------------------------------------------------------//
  private void loadModule ()
  // ---------------------------------------------------------------------------------//
  {
    lines.add ("Load Module");
    lines.add ("");
    byte[] buffer = getDataBuffer ();

    int ptr = 0;
    int count = 0;
    while (buffer[ptr] == 0x20)
    {
      int items = (buffer[ptr + 7] & 0xF0) >>> 4;
      int ptr2 = ptr + 8;
      for (int i = 0; i < items; i++)
      {
        if (buffer[ptr2] != 0 && buffer[ptr2] != 0x40)
        {
          String name = Utility.getString (buffer, ptr2, 8);
          String values = Utility.getHexValues (buffer, ptr2 + 8, 8);
          lines.add (String.format ("%,5d  %s  %s", count++, name, values));
        }
        ptr2 += 16;
      }
      ptr += 248;
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  byte[] getEightBytes ()
  // ---------------------------------------------------------------------------------//
  {
    if (isVB ())
    {
      byte[] buffer = dataBlocks.get (0).getSixteenBytes ();
      byte[] eightBytes = new byte[8];
      System.arraycopy (buffer, 8, eightBytes, 0, 8);
      return eightBytes;
    }
    return dataBlocks.get (0).getEightBytes ();
  }

  // ---------------------------------------------------------------------------------//
  public void getText (List<String> lines)
  // ---------------------------------------------------------------------------------//
  {
    int count = 0;
    int total = 0;
    int pointers = 0;

    lines.add ("");
    lines
        .add ("    #    Offset    Header                    Data         Data      Ptrs");
    lines
        .add ("  ----  --------  -------------------------- --------  ---------  ------");
    for (DataBlock dataBlock : dataBlocks)
    {
      total += dataBlock.getSize ();
      pointers += dataBlock.totalBlockPointers ();
      lines.add (String.format ("  %4d  %s", count++, dataBlock));
    }

    int b1 = (total & 0xFF0000) >>> 16;
    int b2 = (total & 0x00FF00) >>> 8;
    int b3 = (total & 0x0000FF);

    lines.add (String.format ("%44.44s %s", "", "--------  ---------  ------"));
    lines.add (String.format ("%44.44s %02X %02X %02X %,10d  %,6d", "", b1, b2, b3, total,
        pointers));

    if (extraDataBlocks.size () > 0)
    {
      lines.add ("");
      for (DataBlock dataBlock : extraDataBlocks)
        lines.add (String.format ("  %4d  %s", count++, dataBlock));
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogEntry.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<DataBlock> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    return dataBlocks.iterator ();
  }

  // ---------------------------------------------------------------------------------//
  class SizeCount
  // ---------------------------------------------------------------------------------//
  {
    int blockSize;
    int count;

    // ---------------------------------------------------------------------------------//
    SizeCount (int size)
    // ---------------------------------------------------------------------------------//
    {
      blockSize = size;
    }

    // ---------------------------------------------------------------------------------//
    void increment ()
    // ---------------------------------------------------------------------------------//
    {
      count++;
    }

    // ---------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // ---------------------------------------------------------------------------------//
    {
      return String.format ("%,7d  %,5d", blockSize, count);
    }
  }
}
