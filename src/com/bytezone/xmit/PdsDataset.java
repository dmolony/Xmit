package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bytezone.xmit.textunit.ControlRecord;

public class PdsDataset extends Dataset
{
  private static final int DIR_BLOCK_LENGTH = 0x114;

  private int catalogEndBlock = 0;
  private final List<CatalogEntry> catalogEntries = new ArrayList<> ();
  private CopyR1 copyR1;
  private CopyR2 copyR2;

  private final Map<Long, List<CatalogEntry>> catalogMap = new HashMap<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  PdsDataset (Reader reader, ControlRecord inmr02)
  {
    super (reader, inmr02);
  }

  // ---------------------------------------------------------------------------------//
  // getCatalogEntries
  // ---------------------------------------------------------------------------------//

  public List<CatalogEntry> getMembers ()
  {
    return catalogEntries;
  }

  // ---------------------------------------------------------------------------------//
  // getCopyR1
  // ---------------------------------------------------------------------------------//

  public CopyR1 getCopyR1 ()
  {
    return copyR1;
  }

  // ---------------------------------------------------------------------------------//
  // getCopyR2
  // ---------------------------------------------------------------------------------//

  public CopyR2 getCopyR2 ()
  {
    return copyR2;
  }

  // ---------------------------------------------------------------------------------//
  // getXmitMembers
  // ---------------------------------------------------------------------------------//

  public List<CatalogEntry> getXmitMembers ()
  {
    List<CatalogEntry> xmitFiles = new ArrayList<> ();
    for (CatalogEntry catalogEntry : catalogEntries)
      if (catalogEntry.isXmit ())
        xmitFiles.add (catalogEntry);
    return xmitFiles;
  }

  // ---------------------------------------------------------------------------------//
  // process
  // ---------------------------------------------------------------------------------//

  @Override
  void process ()
  {
    boolean inCatalog = true;

    // convert first two BlockPointerList entries
    copyR1 = new CopyR1 (blockPointerLists.get (0).getRawBuffer ());
    copyR2 = new CopyR2 (blockPointerLists.get (1).getRawBuffer ());
    List<CatalogEntry> catalogEntries = null;

    // read catalog data as raw data
    // convert remaining entries to BlockPointers
    for (int i = 2; i < blockPointerLists.size (); i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (inCatalog)
      {
        inCatalog = addCatalogEntries (bpl.getRawBuffer ());
        if (!inCatalog)
          catalogEndBlock = i;
      }
      else
      {
        bpl.createDataBlocks ();       // create new BlockPointers
        for (DataBlock dataBlock : bpl)
        {
          long ttl = dataBlock.getTtl ();
          if (catalogMap.containsKey (ttl))
            catalogEntries = catalogMap.get (ttl);
          if (catalogEntries != null)
            for (CatalogEntry catalogEntry : catalogEntries)
              catalogEntry.addBlockPointerList (bpl);
          break;        // just need the first DataBlock
        }
      }
    }

    if (false)
    {
      System.out.println (reader.getFileName ());
      System.out.println ("\nBlock pointers:");
      System.out.printf ("CopyRx ..........       2%n");
      System.out.printf ("Catalog ......... %,7d%n", catalogEndBlock - 1);
      System.out.printf ("Data ............ %,7d%n",
          blockPointerLists.size () - catalogEndBlock - 1);
      System.out.printf ("Total ........... %,7d%n", blockPointerLists.size ());
    }
  }

  // ---------------------------------------------------------------------------------//
  // addCatalogEntries
  // ---------------------------------------------------------------------------------//

  private boolean addCatalogEntries (byte[] buffer)
  {
    int ptr = 0;
    while (ptr + 22 < buffer.length)
    {
      int ptr2 = ptr + 22;

      while (true)
      {
        if (buffer[ptr2] == (byte) 0xFF)
          return false;                                     // member list finished

        CatalogEntry catalogEntry = new CatalogEntry (reader, buffer, ptr2, lrecl, recfm);
        catalogEntries.add (catalogEntry);
        addToMap (catalogEntry);

        // check for last member
        if (Utility.matches (buffer, ptr2, buffer, ptr + 12, 8))
          break;

        ptr2 += catalogEntry.length ();
      }

      ptr += DIR_BLOCK_LENGTH;
    }

    return true;                                            // member list not finished
  }

  // ---------------------------------------------------------------------------------//
  // addToMap
  // ---------------------------------------------------------------------------------//

  private void addToMap (CatalogEntry catalogEntry)
  {
    long ttl = catalogEntry.setCopyRecords (copyR1, copyR2);
    List<CatalogEntry> catalogEntriesTtl = catalogMap.get (ttl);
    if (catalogEntriesTtl == null)
    {
      catalogEntriesTtl = new ArrayList<> ();
      catalogMap.put (ttl, catalogEntriesTtl);
    }
    catalogEntriesTtl.add (catalogEntry);
  }

  // ---------------------------------------------------------------------------------//
  // getFileName
  // ---------------------------------------------------------------------------------//

  public String getFileName ()
  {
    return reader.getFileName ();
  }

  // ---------------------------------------------------------------------------------//
  // getBlockListing
  // ---------------------------------------------------------------------------------//

  public String getBlockListing ()
  {
    StringBuilder text = new StringBuilder ();

    int count = 0;
    for (CatalogEntry catalogEntry : catalogEntries)
    {
      int total = 0;
      text.append ("\n");
      for (BlockPointerList blockPointerList : catalogEntry.blockPointerLists)
      {
        for (DataBlock dataBlock : blockPointerList)
        {
          int size = dataBlock.getSize ();
          total += size;
          if (size > 0)
            text.append (String.format ("%,5d  %-8s  %s%n", count++,
                catalogEntry.getMemberName (), dataBlock));
          else
            text.append (String.format ("%,5d  %-8s  %s   %06X %<,7d%n", count++,
                catalogEntry.getMemberName (), dataBlock, total));
        }
      }
    }

    return text.toString ();
  }
}
