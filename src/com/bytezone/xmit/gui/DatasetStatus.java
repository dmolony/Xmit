package com.bytezone.xmit.gui;

import java.util.Map;
import java.util.TreeMap;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsDataset;

// ----------------------------------------------------------------------------------- //
public class DatasetStatus
//----------------------------------------------------------------------------------- //
{
  Dataset dataset;                // usually file #1 in the Reader
  DataFile dataFile;              // FlatFile or PdsMember
  CatalogEntry catalogEntry;      // needed for alias members
  String name;

  // keep track of all PDS datasets seen so that we can INCLUDE members
  final Map<String, PdsDataset> datasets = new TreeMap<> ();

  //----------------------------------------------------------------------------------- //
  void datasetSelected (Dataset dataset, String name)
  //----------------------------------------------------------------------------------- //
  {
    this.dataset = dataset;
    this.name = name;

    if (dataset != null && dataset.isPds ())
    {
      String datasetName = dataset.getReader ().getFileName ();
      if (!datasets.containsKey (datasetName))
        datasets.put (datasetName, (PdsDataset) dataset);
    }
  }

  //----------------------------------------------------------------------------------- //
  void catalogEntrySelected (CatalogEntry catalogEntry)
  //----------------------------------------------------------------------------------- //
  {
    if (dataset == null || dataset.isPs ())
      return;

    this.catalogEntry = catalogEntry;
    dataFile = catalogEntry == null ? null : catalogEntry.getMember ();
  }
}
