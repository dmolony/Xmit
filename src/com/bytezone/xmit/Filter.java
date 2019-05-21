package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class Filter
// -----------------------------------------------------------------------------------//
{
  private final String key;
  private final PdsDataset pdsDataset;

  private List<CatalogEntry> filtered;
  private List<CatalogEntry> reversed;

  public enum FilterMode
  {
    ON, REVERSED, OFF
  }

  // ---------------------------------------------------------------------------------//
  public Filter (PdsDataset pdsDataset, String key)
  // ---------------------------------------------------------------------------------//
  {
    this.key = key;
    this.pdsDataset = pdsDataset;
  }

  // ---------------------------------------------------------------------------------//
  public List<CatalogEntry> getCatalogEntries (FilterMode filterMode)
  // ---------------------------------------------------------------------------------//
  {
    // nothing to do
    if (filterMode == FilterMode.OFF || key.isEmpty ())
      return pdsDataset.getCatalogEntries ();

    // lazy fill
    if (filtered == null)
    {
      filtered = new ArrayList<> ();
      reversed = new ArrayList<> ();

      for (CatalogEntry catalogEntry : pdsDataset.getCatalogEntries ())
        if (catalogEntry.contains (key))
          filtered.add (catalogEntry);
        else
          reversed.add (catalogEntry);
    }

    return filterMode == FilterMode.ON ? filtered : reversed;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    if (filtered == null)
      return String.format ("Filter: %-10s  %,d", key,
          pdsDataset.getCatalogEntries ().size ());

    return String.format ("Filter: %-10s %,d + %,d = %,d", key, filtered.size (),
        reversed.size (), pdsDataset.getCatalogEntries ().size ());
  }
}
