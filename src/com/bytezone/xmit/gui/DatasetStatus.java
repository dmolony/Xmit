package com.bytezone.xmit.gui;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.PdsMember;

// ----------------------------------------------------------------------------------- //
public class DatasetStatus
//----------------------------------------------------------------------------------- //
{
  Dataset dataset;                // usually file #1 in the Reader
  DataFile dataFile;              // FlatFile or PdsMember
  CatalogEntry catalogEntry;      // needed for alias members
  String name;

  // keep track of all PDS datasets seen so that we can INCLUDE members
  private final Map<String, PdsDataset> datasets = new TreeMap<> ();

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

  //----------------------------------------------------------------------------------- //
  Optional<PdsMember> findMember (String datasetName, String memberName)
  //----------------------------------------------------------------------------------- //
  {
    if (datasets.containsKey (datasetName))
      return datasets.get (datasetName).findMember (memberName);

    return Optional.empty ();
  }

  //---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  //---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Name............. %s%n", name));
    text.append (String.format ("Dataset.......... %s%n", dataset));
    text.append (String.format ("Datafile......... %s%n", dataFile.getName ()));
    text.append (String.format ("CatalogEntry..... %s", catalogEntry));

    return text.toString ();
  }
}
