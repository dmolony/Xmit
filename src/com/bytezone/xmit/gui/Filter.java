package com.bytezone.xmit.gui;

import java.util.List;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.PdsDataset;

import javafx.concurrent.Task;

// ---------------------------------------------------------------------------------//
public class Filter extends Task<List<CatalogEntry>>
// ---------------------------------------------------------------------------------//
{
  private final PdsDataset pdsDataset;
  private final String key;

  // ---------------------------------------------------------------------------------//
  public Filter (PdsDataset pdsDataset, String key)
  // ---------------------------------------------------------------------------------//
  {
    this.pdsDataset = pdsDataset;
    this.key = key;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  protected List<CatalogEntry> call () throws Exception
  // ---------------------------------------------------------------------------------//
  {
    return pdsDataset.getCatalogEntries (key);
  }
}
