package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.bytezone.xmit.textunit.ControlRecord;

// ---------------------------------------------------------------------------------//
public class PdsDataset extends Dataset implements Iterable<CatalogEntry>
//---------------------------------------------------------------------------------//
{
  private static final int DIR_BLOCK_LENGTH = 0x114;

  private final List<CatalogEntry> catalogEntries = new ArrayList<> ();
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
  public boolean isBasic ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogEntries.size () > 0 && catalogEntries.get (0).isBasic ();
  }

  // ---------------------------------------------------------------------------------//
  public int size ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogEntries.size ();
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

    // catalogMap : list of all CatalogEntries that share a TTL in TTL sequence
    // members    : list of PdsMember (List<DataBlock>) in ascending TTL sequence

    List<PdsMember> members =
        copyR1.isPdse () ? createPdseMembers (dataBlocks) : createPdsMembers (dataBlocks);

    if (catalogMap.values ().size () != members.size ())
    {
      System.out.println ("Catalog mismatch");
      System.out.printf ("%d %d%n", catalogMap.values ().size (), members.size ());
    }

    // Match each PdsMember (List<DataBlock>) to every CatalogEntry that refers to it.
    // Match each CatalogEntry to one PdsMember

    int count = 0;
    for (List<CatalogEntry> catalogEntryList : catalogMap.values ())
    {
      PdsMember member = members.get (count++);

      CatalogEntry sourceEntry = catalogEntryList.get (0);
      for (CatalogEntry catalogEntry : catalogEntryList)
      {
        catalogEntry.setMember (member);
        if (catalogEntry.getAliasName ().isEmpty ())
          sourceEntry = catalogEntry;
      }
      // see FILE182.UTILXMIT
      member.setCatalogEntry (sourceEntry);

      if (member.isXmit ())
        xmitMembers.add (member);       // should these be CatalogEntry?
    }

    // FILE182.XMITxx contains REV38 which is flagged as an alias of REVIEW, but its
    // TTL does not match REVIEW's. So it has its own Member.
    if (false)
    {
      displayMap (catalogMap);
      listMembers (members);
      for (CatalogEntry catalogEntry : catalogEntries)
        System.out.println (catalogEntry);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void displayMap (Map<Long, List<CatalogEntry>> catalogMap)
  // ---------------------------------------------------------------------------------//
  {
    int count = 0;
    for (long ttl : catalogMap.keySet ())
    {
      System.out.printf ("%4d  %06X%n", count++, ttl);
      for (CatalogEntry catalogEntry : catalogMap.get (ttl))
      {
        System.out.printf ("             %s%n", catalogEntry);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private void listMembers (List<PdsMember> members)
  // ---------------------------------------------------------------------------------//
  {
    int count = 0;
    for (PdsMember pdsMember : members)
      System.out.printf ("%4d  %-8s  %,9d%n", count++, pdsMember.getName (),
          pdsMember.dataLength);
  }

  // ---------------------------------------------------------------------------------//
  private List<PdsMember> createPdsMembers (List<DataBlock> dataBlocks)
  // ---------------------------------------------------------------------------------//
  {
    List<PdsMember> members = new ArrayList<> ();
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
    return members;
  }

  // ---------------------------------------------------------------------------------//
  private List<PdsMember> createPdseMembers (List<DataBlock> dataBlocks)
  // ---------------------------------------------------------------------------------//
  {
    List<PdsMember> members = new ArrayList<> ();
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
    return members;
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
    long ttl = catalogEntry.getTtr ();
    List<CatalogEntry> catalogEntriesTtl = catalogMap.get (ttl);
    if (catalogEntriesTtl == null)
    {
      catalogEntriesTtl = new ArrayList<> ();
      catalogMap.put (ttl, catalogEntriesTtl);
    }
    catalogEntriesTtl.add (catalogEntry);
  }

  // ---------------------------------------------------------------------------------//
  //  public CatalogEntry getCatalogEntry (String name)
  //  // ---------------------------------------------------------------------------------//
  //  {
  //    for (CatalogEntry catalogEntry : catalogEntries)
  //      if (name.equals (catalogEntry.getMemberName ()))
  //        return catalogEntry;
  //    System.out.printf ("No catalog entry found: [%s]%n", name);
  //    return null;
  //  }

  // ---------------------------------------------------------------------------------//
  public int memberIndex (String memberName)
  // ---------------------------------------------------------------------------------//
  {
    int index = 0;
    for (CatalogEntry catalogEntry : catalogEntries)
    {
      if (memberName.equals (catalogEntry.getMemberName ()))
        return index;
      ++index;
    }
    return 0;
  }

  // ---------------------------------------------------------------------------------//
  public String getFileName ()
  // ---------------------------------------------------------------------------------//
  {
    return reader.getFileName ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<CatalogEntry> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogEntries.iterator ();
  }
}
