package com.bytezone.xmit;

import java.util.*;

import com.bytezone.xmit.CatalogEntry.ModuleType;

// useful: https://stackoverflow.com/questions/28929563/
// how-to-manipulate-the-result-of-a-future-in-javafx
// -----------------------------------------------------------------------------------//
public class PdsDataset extends Dataset implements Iterable<CatalogEntry>
// -----------------------------------------------------------------------------------//
{
  private static final int DIR_BLOCK_LENGTH = 0x114;

  private final List<CatalogEntry> catalogEntries = new ArrayList<> ();
  private final List<PdsMember> pdsXmitMembers = new ArrayList<> ();
  private final Map<String, Filter> filterList = new HashMap<> ();

  private CopyR1 copyR1;
  private CopyR2 copyR2;

  private final AwsTapeHeaders awsTapeHeaders;        // this is clumsy

  // ---------------------------------------------------------------------------------//
  PdsDataset (Reader reader, Disposition disposition, String datasetName)
  // ---------------------------------------------------------------------------------//
  {
    super (reader, disposition, datasetName);
    awsTapeHeaders = null;
  }

  // ---------------------------------------------------------------------------------//
  PdsDataset (Reader reader, AwsTapeHeaders awsTapeHeaders)
  // ---------------------------------------------------------------------------------//
  {
    super (reader, awsTapeHeaders.disposition, awsTapeHeaders.name);
    this.awsTapeHeaders = awsTapeHeaders;
  }

  // ---------------------------------------------------------------------------------//
  public ModuleType getModuleType ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogEntries.size () == 0 ? null : catalogEntries.get (0).getModuleType ();
  }

  // ---------------------------------------------------------------------------------//
  public boolean isBasicModule ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogEntries.size () > 0 && catalogEntries.get (0).isBasicModule ();
  }

  // ---------------------------------------------------------------------------------//
  public Optional<PdsMember> findMember (String name)
  // ---------------------------------------------------------------------------------//
  {
    for (CatalogEntry catalogEntry : catalogEntries)
      if (catalogEntry.getMemberName ().equals (name))
        return Optional.of (catalogEntry.getMember ());
    return Optional.empty ();
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
  public AwsTapeHeaders getAwsTapeHeaders ()
  // ---------------------------------------------------------------------------------//
  {
    return awsTapeHeaders;
  }

  // ---------------------------------------------------------------------------------//
  public List<PdsMember> getPdsXmitMembers ()
  // ---------------------------------------------------------------------------------//
  {
    return pdsXmitMembers;
  }

  // ---------------------------------------------------------------------------------//
  public List<CatalogEntry> getCatalogEntries ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogEntries;
  }

  // ---------------------------------------------------------------------------------//
  public boolean containsKey (String key)
  // ---------------------------------------------------------------------------------//
  {
    return filterList.containsKey (key);
  }

  // ---------------------------------------------------------------------------------//
  public Filter getFilter (String key)
  // ---------------------------------------------------------------------------------//
  {
    if (filterList.containsKey (key))
      return filterList.get (key);

    Filter filter = new Filter (this, key);
    filterList.put (key, filter);

    return filter;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void allocateSegments ()
  // ---------------------------------------------------------------------------------//
  {
    int segmentNbr = 0;

    // convert first two DataBlock entries
    copyR1 = new CopyR1 (segments.get (segmentNbr++).getRawBuffer ());
    copyR2 = new CopyR2 (segments.get (segmentNbr++).getRawBuffer ());
    setDisposition (copyR1.getDisposition ());
    getDisposition ().setPdse (copyR1.isPdse ());

    // read catalog entries
    Map<Long, List<CatalogEntry>> catalogMap = new TreeMap<> ();
    while (segmentNbr < segments.size ())
    {
      Segment segment = segments.get (segmentNbr++);
      if (!addCatalogEntries (segment.getRawBuffer (), catalogMap))
        break;
    }

    // read data blocks
    List<DataBlock> dataBlocks = new ArrayList<> ();
    while (segmentNbr < segments.size ())
    {
      Segment segment = segments.get (segmentNbr++);
      dataBlocks.addAll (segment.createDataBlocks ());
    }

    // catalogMap : list of all CatalogEntries that share a TTL, in TTL sequence
    // members    : list of PdsMember (List<DataBlock>) in TTL sequence

    List<PdsMember> members =
        copyR1.isPdse () ? createPdsEMembers (dataBlocks) : createPdsMembers (dataBlocks);

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

      CatalogEntry sourceEntry = catalogEntryList.get (0);      // non-alias
      member.setCatalogEntry (sourceEntry);

      for (CatalogEntry catalogEntry : catalogEntryList)
      {
        catalogEntry.setMember (member);

        if (catalogEntry.isAlias () && catalogEntry.getAliasName ().isEmpty ())
          catalogEntry.setAliasName (sourceEntry.getMemberName ());
      }

      if (member.isXmit ())
        pdsXmitMembers.add (member);       // should these be CatalogEntry?
    }

    // FILE182.UTILXMIT contains REV38 which is flagged as an alias of REVIEW, but
    // its TTL does not match REVIEW's. So it has its own Member.
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
        System.out.printf ("             %s%n", catalogEntry);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void listMembers (List<PdsMember> members)
  // ---------------------------------------------------------------------------------//
  {
    int count = 0;

    for (PdsMember pdsMember : members)
      System.out.printf ("%4d  %-8s  %,9d%n", count++, pdsMember.getName (),
          pdsMember.getDataLength ());
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
        currentMember = new PdsMember (this, getDisposition ());
        members.add (currentMember);
      }

      currentMember.addDataBlock (dataBlock);

      if (dataBlock.getSize () == 0)
        currentMember = null;
    }
    return members;
  }

  // ---------------------------------------------------------------------------------//
  private List<PdsMember> createPdsEMembers (List<DataBlock> dataBlocks)
  // ---------------------------------------------------------------------------------//
  {
    List<PdsMember> members = new ArrayList<> ();
    PdsMember currentMember = null;
    long lastTtl = 0;

    for (DataBlock dataBlock : dataBlocks)
    {
      long ttl = dataBlock.getTtr ();
      if (ttl == 0)
        continue;     // skip first PDSE block

      if (ttl != lastTtl)
      {
        currentMember = new PdsMember (this, getDisposition ());
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
    int ptr1 = 0;
    while (ptr1 + 22 < buffer.length)
    {
      int ptr2 = ptr1 + 22;

      while (true)
      {
        if (buffer[ptr2] == (byte) 0xFF)
          return false;                                     // member list finished

        CatalogEntry catalogEntry = CatalogEntry.instanceOf (buffer, ptr2);
        catalogEntries.add (catalogEntry);
        addToMap (catalogEntry, catalogMap);

        // check for last member
        if (Utility.matches (buffer, ptr2, buffer, ptr1 + 12, 8))
          break;

        ptr2 += catalogEntry.getEntryLength ();
      }

      ptr1 += DIR_BLOCK_LENGTH;
    }

    return true;                                            // member list not finished
  }

  // ---------------------------------------------------------------------------------//
  private void addToMap (CatalogEntry catalogEntry,
      Map<Long, List<CatalogEntry>> catalogMap)
  // ---------------------------------------------------------------------------------//
  {
    long ttr = catalogEntry.getTtr ();
    List<CatalogEntry> catalogEntriesTtr = catalogMap.get (ttr);

    if (catalogEntriesTtr == null)
    {
      catalogEntriesTtr = new ArrayList<> ();
      catalogMap.put (ttr, catalogEntriesTtr);
    }

    if (catalogEntry.isAlias ())
      catalogEntriesTtr.add (catalogEntry);       // retain original sequence
    else
      catalogEntriesTtr.add (0, catalogEntry);    // insert at the head of the list
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<CatalogEntry> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogEntries.iterator ();
  }
}
