package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

// ---------------------------------------------------------------------------------//
public class Filter
// ---------------------------------------------------------------------------------//
{
  private final PdsDataset pdsDataset;
  private final String key;
  private final List<CatalogEntry> filtered = new ArrayList<> ();
  private final List<CatalogEntry> reversed = new ArrayList<> ();
  private final List<CatalogEntry> noFilter;

  public enum FilterMode
  {
    FILTERED, REVERSED, ALL
  }

  // ---------------------------------------------------------------------------------//
  public Filter (PdsDataset pdsDataset, String key)
  // ---------------------------------------------------------------------------------//
  {
    this.pdsDataset = pdsDataset;
    this.key = key;

    noFilter = pdsDataset.getCatalogEntries ();

    if (key.isEmpty ())
      filtered.addAll (noFilter);
    else
      for (CatalogEntry catalogEntry : noFilter)
        if (catalogEntry.contains (key))
          filtered.add (catalogEntry);
        else
          reversed.add (catalogEntry);
  }

  // ---------------------------------------------------------------------------------//
  public List<CatalogEntry> getFiltered (FilterMode filterMode)
  // ---------------------------------------------------------------------------------//
  {
    switch (filterMode)
    {
      case ALL:
        return noFilter;
      case FILTERED:
        return filtered;
      case REVERSED:
        return reversed;
      default:
        assert false;
        return pdsDataset.getCatalogEntries ();
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("Filter: %-10s %,d + %,d = %,d", key, filtered.size (),
        reversed.size (), noFilter.size ());
  }
}
