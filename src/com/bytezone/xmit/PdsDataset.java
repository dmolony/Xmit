package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.bytezone.xmit.textunit.ControlRecord;

public class PdsDataset extends Dataset
{
  private static final int DIR_BLOCK_LENGTH = 0x114;

  //  private int catalogEndBlock = 0;
  private final List<CatalogEntry> catalogEntries = new ArrayList<> ();
  private CopyR1 copyR1;
  private CopyR2 copyR2;

  private final Map<Long, List<CatalogEntry>> catalogMap = new TreeMap<> ();

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
    List<DataBlock> dataBlocks = new ArrayList<> ();

    // convert first two BlockPointerList entries
    copyR1 = new CopyR1 (segments.get (0).getRawBuffer ());
    copyR2 = new CopyR2 (segments.get (1).getRawBuffer ());

    for (int i = 2; i < segments.size (); i++)
    {
      Segment segment = segments.get (i);
      if (inCatalog)
        inCatalog = addCatalogEntries (segment.getRawBuffer ());
      else
        dataBlocks.addAll (segment.createDataBlocks ());    // create new BlockPointers
    }

    List<Member> members = new ArrayList<> ();
    Member currentMember = null;
    long lastTtl = 0;

    if (copyR1.isPdse ())
      for (DataBlock dataBlock : dataBlocks)
      {
        long ttl = dataBlock.getTtl ();
        if (ttl == 0)
          continue;     // skip first PDSE block

        if (ttl != lastTtl)
        {
          currentMember = new Member ();
          members.add (currentMember);
          lastTtl = ttl;
        }

        currentMember.add (dataBlock);
      }
    else
      for (DataBlock dataBlock : dataBlocks)
      {
        if (currentMember == null)
        {
          currentMember = new Member ();
          members.add (currentMember);
        }

        currentMember.add (dataBlock);

        if (dataBlock.getSize () == 0)
          currentMember = null;
      }

    int count = 0;
    for (List<CatalogEntry> catalogEntryList : catalogMap.values ())
    {
      Member member = members.get (count++);
      for (CatalogEntry catalogEntry : catalogEntryList)
        catalogEntry.setMember (member);
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

      Member member = catalogEntry.getMember ();
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
}
