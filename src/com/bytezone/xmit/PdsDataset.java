package com.bytezone.xmit;

import java.util.*;

import com.bytezone.xmit.textunit.ControlRecord;

// ---------------------------------------------------------------------------------//
public class PdsDataset extends Dataset implements Iterable<PdsMember>
//---------------------------------------------------------------------------------//
{
  private static final int DIR_BLOCK_LENGTH = 0x114;

  private final List<CatalogEntry> catalogEntries = new ArrayList<> ();
  private final List<PdsMember> members = new ArrayList<> ();
  private final List<PdsMember> xmitMembers = new ArrayList<> ();

  private CopyR1 copyR1;
  private CopyR2 copyR2;

  // ---------------------------------------------------------------------------------//
  PdsDataset (Reader reader, ControlRecord inmr02)
  // ---------------------------------------------------------------------------------//
  {
    super (reader, inmr02);
  }

  // ---------------------------------------------------------------------------------//
  public List<CatalogEntry> getCatalogEntries ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogEntries;
  }

  // ---------------------------------------------------------------------------------//
  public int size ()
  // ---------------------------------------------------------------------------------//
  {
    return members.size ();
  }

  // ---------------------------------------------------------------------------------//
  public CopyR1 getCopyR1 ()
  // ---------------------------------------------------------------------------------//
  {
    return copyR1;
  }

  // ---------------------------------------------------------------------------------//
  public CopyR2 getCopyR2 ()
  // ---------------------------------------------------------------------------------//
  {
    return copyR2;
  }

  // ---------------------------------------------------------------------------------//
  public List<PdsMember> getXmitMembers ()
  // ---------------------------------------------------------------------------------//
  {
    return xmitMembers;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void allocateSegments ()
  // ---------------------------------------------------------------------------------//
  {
    // convert first two BlockPointerList entries
    copyR1 = new CopyR1 (segments.get (0).getRawBuffer ());
    copyR2 = new CopyR2 (segments.get (1).getRawBuffer ());

    disposition.setPdse (copyR1.isPdse ());

    boolean inCatalog = true;
    List<DataBlock> dataBlocks = new ArrayList<> ();
    Map<Long, List<CatalogEntry>> catalogMap = new TreeMap<> ();

    for (int i = 2; i < segments.size (); i++)
    {
      Segment segment = segments.get (i);
      if (inCatalog)
        inCatalog = addCatalogEntries (segment.getRawBuffer (), catalogMap);
      else
        dataBlocks.addAll (segment.createDataBlocks ());    // create new BlockPointers
    }

    if (copyR1.isPdse ())
      allocatePDSE (dataBlocks);
    else
      allocatePDS (dataBlocks);

    if (catalogMap.values ().size () != members.size ())
      System.out.printf ("%d %d%n", catalogMap.values ().size (), members.size ());

    int count = 0;
    for (List<CatalogEntry> catalogEntryList : catalogMap.values ())
    {
      PdsMember member = members.get (count++);
      member.setCatalogEntries (catalogEntryList);
      for (CatalogEntry catalogEntry : catalogEntryList)
        catalogEntry.setMember (member);

      if (member.isXmit ())
        xmitMembers.add (member);
    }
    Collections.sort (members);
  }

  // ---------------------------------------------------------------------------------//
  private void allocatePDS (List<DataBlock> dataBlocks)
  // ---------------------------------------------------------------------------------//
  {
    PdsMember currentMember = null;

    for (DataBlock dataBlock : dataBlocks)
    {
      if (currentMember == null)
      {
        currentMember = new PdsMember (this, disposition);
        members.add (currentMember);
      }

      currentMember.addDataBlock (dataBlock);

      if (dataBlock.getSize () == 0)
        currentMember = null;
    }
  }

  // ---------------------------------------------------------------------------------//
  private void allocatePDSE (List<DataBlock> dataBlocks)
  // ---------------------------------------------------------------------------------//
  {
    PdsMember currentMember = null;
    long lastTtl = 0;

    for (DataBlock dataBlock : dataBlocks)
    {
      long ttl = dataBlock.getTtl ();
      if (ttl == 0)
        continue;     // skip first PDSE block

      if (ttl != lastTtl)
      {
        currentMember = new PdsMember (this, disposition);
        members.add (currentMember);
        lastTtl = ttl;
      }

      currentMember.addDataBlock (dataBlock);
    }
  }

  // ---------------------------------------------------------------------------------//
  private boolean addCatalogEntries (byte[] buffer,
      Map<Long, List<CatalogEntry>> catalogMap)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = 0;
    while (ptr + 22 < buffer.length)
    {
      int ptr2 = ptr + 22;

      while (true)
      {
        if (buffer[ptr2] == (byte) 0xFF)
          return false;                                     // member list finished

        CatalogEntry catalogEntry = new CatalogEntry (buffer, ptr2);
        catalogEntries.add (catalogEntry);
        addToMap (catalogEntry, catalogMap);

        // check for last member
        if (Utility.matches (buffer, ptr2, buffer, ptr + 12, 8))
          break;

        ptr2 += catalogEntry.getDirectoryData ().length;
      }

      ptr += DIR_BLOCK_LENGTH;
    }

    return true;                                            // member list not finished
  }

  // ---------------------------------------------------------------------------------//
  // addToMap
  // ---------------------------------------------------------------------------------//

  private void addToMap (CatalogEntry catalogEntry,
      Map<Long, List<CatalogEntry>> catalogMap)
  {
    catalogEntry.setCopyRecords (copyR1, copyR2);
    long ttl = catalogEntry.getOffset ();
    List<CatalogEntry> catalogEntriesTtl = catalogMap.get (ttl);
    if (catalogEntriesTtl == null)
    {
      catalogEntriesTtl = new ArrayList<> ();
      catalogMap.put (ttl, catalogEntriesTtl);
    }
    catalogEntriesTtl.add (catalogEntry);
  }

  // ---------------------------------------------------------------------------------//
  public String getFileName ()
  // ---------------------------------------------------------------------------------//
  {
    return reader.getFileName ();
  }

  // ---------------------------------------------------------------------------------//
  public String getBlockListing ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int count = 0;
    for (CatalogEntry catalogEntry : catalogEntries)
    {
      int total = 0;
      text.append ("\n");

      PdsMember member = catalogEntry.getMember ();
      for (DataBlock dataBlock : member)
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

      for (DataBlock dataBlock : member.getExtraDataBlocks ())
        text.append (String.format ("%,5d  %-8s  %s%n", count++,
            catalogEntry.getMemberName (), dataBlock));
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<PdsMember> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    return members.iterator ();
  }
}
