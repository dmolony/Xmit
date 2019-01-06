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
  //  private List<CatalogEntry> sortedCatalogEntries;
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
          //          System.out.println (dataBlock);
          long ttl = dataBlock.getTtl ();
          if (catalogMap.containsKey (ttl))
            catalogEntries = catalogMap.get (ttl);
          if (catalogEntries != null)
            for (CatalogEntry catalogEntry : catalogEntries)
              catalogEntry.addBlockPointerList2 (bpl);
          //          else
          //            System.out.println ("null CE");
          //            System.out.println (catalogEntry);
          //          System.out.println (dataBlock);
          break;
        }
      }
    }

    //    for (List<CatalogEntry> catalogEntries : catalogMap.values ())
    //      for (CatalogEntry catalogEntry : catalogEntries)
    //        System.out.println (catalogEntry);

    //    for (int i = catalogEndBlock + 1; i < blockPointerLists.size (); i++)
    //    {
    //      BlockPointerList bpl = blockPointerLists.get (i);
    //      for (DataBlock dataBlock : bpl)
    //      {
    //        long ttl = dataBlock.getTtl ();
    //        if (catalogMap.containsKey (ttl))
    //          for (CatalogEntry catalogEntry : catalogMap.get (ttl))
    //            System.out.println (catalogEntry);
    //        System.out.println (dataBlock);
    //      }
    //    }

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

    // assign new BlockPointer lists to CatalogEntries
    //    sortedCatalogEntries = new ArrayList<> (catalogEntries);
    //    Collections.sort (sortedCatalogEntries);
    //
    //    Map<Integer, CatalogEntry> offsets = new TreeMap<> ();
    //    for (CatalogEntry catalogEntry : sortedCatalogEntries)
    //      if (!offsets.containsKey (catalogEntry.getOffset ()))
    //        offsets.put (catalogEntry.getOffset (), catalogEntry);
    //
    //    List<CatalogEntry> uniqueCatalogEntries = new ArrayList<> ();
    //    for (CatalogEntry catalogEntry : offsets.values ())
    //      uniqueCatalogEntries.add (catalogEntry);

    // assign BlockPointerLists to CatalogEntries
    //    if (copyR1.isPdse ())
    //      assignPdsExtendedBlocks (uniqueCatalogEntries);
    //    else
    //      assignPdsBlocks (uniqueCatalogEntries);
  }

  // ---------------------------------------------------------------------------------//
  // assignPdsBlocks
  // ---------------------------------------------------------------------------------//

  //  private void assignPdsBlocks (List<CatalogEntry> uniqueCatalogEntries)
  //  {
  //    int currentMember = 0;
  //    for (int i = catalogEndBlock + 1; i < blockPointerLists.size (); i++)
  //    {
  //      BlockPointerList bpl = blockPointerLists.get (i);
  //      CatalogEntry catalogEntry = uniqueCatalogEntries.get (currentMember);
  //      if (!catalogEntry.addBlockPointerList (bpl))
  //        break;
  //
  //      if (bpl.isLastBlock ())
  //        ++currentMember;
  //    }
  //  }

  // ---------------------------------------------------------------------------------//
  // assignPdsExtendedBlocks
  // ---------------------------------------------------------------------------------//

  //  private void assignPdsExtendedBlocks (List<CatalogEntry> uniqueCatalogEntries)
  //  {
  //    int lastOffset = -1;
  //    int currentMember = -1;
  //
  //    for (int i = catalogEndBlock + 2; i < blockPointerLists.size (); i++)
  //    {
  //      BlockPointerList bpl = blockPointerLists.get (i);
  //
  //      int offset = bpl.getOffset ();
  //      if (lastOffset != offset)
  //      {
  //        ++currentMember;
  //        lastOffset = offset;
  //      }
  //      CatalogEntry catalogEntry = uniqueCatalogEntries.get (currentMember);
  //      if (catalogEntry.getOffset () == offset)
  //      {
  //        catalogEntry.setPdse (true);
  //        catalogEntry.addBlockPointerList (bpl);
  //      }
  //    }
  //  }

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

  public String getBlockListing2 ()
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

  //  public String getBlockListing ()
  //  {
  //    StringBuilder text = new StringBuilder ();
  //
  //    int count = 0;
  //    for (CatalogEntry catalogEntry : sortedCatalogEntries)
  //    {
  //      int total = 0;
  //
  //      // FILE659 for two extents
  //      if (false)
  //      {
  //        int xx = catalogEntry.getOffset ();
  //        int x2 = xx & 0x0000FF;
  //        int x1 = (xx & 0xFFFF00) >>> 8;
  //
  //        int yy = (int) Utility.getFourBytes (copyR2.buffer, 22);
  //        int y2 = (yy & 0xFFFF);
  //        int y1 = (yy & 0xFFFF0000) >>> 16;
  //
  //        int z3 = x2;
  //        int z2 = (x1 + y2) % 15;
  //        int z1 = y1 + (x1 + y2) / 15;
  //
  //        int z11 = (z1 & 0xFF00) >>> 8;
  //        int z12 = z1 & 0x00FF;
  //      }
  //
  //      text.append ("\n");
  //      //      text.append (String.format ("%n       %s  %-19.19s %02X %02X    %02X %02X%n",
  //      //          catalogEntry.getMemberName (), "", z11, z12, z2, z3));
  //      for (BlockPointerList blockPointerList : catalogEntry.blockPointerLists)
  //        for (DataBlock dataBlock : blockPointerList)
  //        {
  //          int size = dataBlock.getSize ();
  //          total += size;
  //          if (size > 0)
  //            text.append (String.format ("%,5d  %-8s  %s%n", count++,
  //                catalogEntry.getMemberName (), dataBlock));
  //          else
  //            text.append (String.format ("%,5d  %-8s  %s   %06X %<,7d%n", count++,
  //                catalogEntry.getMemberName (), dataBlock, total));
  //        }
  //    }
  //    return text.toString ();
  //  }
}
