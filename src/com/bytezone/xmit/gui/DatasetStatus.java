package com.bytezone.xmit.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.bytezone.xmit.*;

// ----------------------------------------------------------------------------------- //
class DatasetStatus
//----------------------------------------------------------------------------------- //
{
  Dataset dataset;                // usually file #1 in the Reader
  DataFile dataFile;              // FlatFile or PdsMember
  CatalogEntry catalogEntry;      // needed for alias members
  String name;

  // keep track of all PDS datasets seen so that we can INCLUDE members
  private final Map<String, PdsDataset> datasets = new TreeMap<> ();

  // keep track of last selected member for each dataset
  final Map<Dataset, String> selectedMembers = new HashMap<> ();

  //----------------------------------------------------------------------------------- //
  void datasetSelected (Dataset dataset, String name)
  //----------------------------------------------------------------------------------- //
  {
    this.dataset = dataset;
    this.name = name;
    catalogEntry = null;
    dataFile = null;

    if (dataset != null)
      if (dataset.isPds ())
      {
        String datasetName = dataset.getReader ().getFileName ();
        if (!datasets.containsKey (datasetName))
          datasets.put (datasetName, (PdsDataset) dataset);
      }
      else if (dataset.isPs ())
      {
        dataFile = ((PsDataset) dataset).getFlatFile ();
      }
  }

  //----------------------------------------------------------------------------------- //
  void catalogEntrySelected (CatalogEntry catalogEntry)
  //----------------------------------------------------------------------------------- //
  {
    this.catalogEntry = catalogEntry;

    if (catalogEntry != null)
    {
      dataFile = catalogEntry.getMember ();
      selectedMembers.put (dataset, catalogEntry.getMemberName ());
    }
  }

  //----------------------------------------------------------------------------------- //
  Optional<PdsMember> findMember (String datasetName, String memberName)
  //----------------------------------------------------------------------------------- //
  {
    if (datasets.containsKey (datasetName))
      return datasets.get (datasetName).findMember (memberName);

    return Optional.empty ();
  }

  //----------------------------------------------------------------------------------- //
  String previousSelection ()
  //----------------------------------------------------------------------------------- //
  {
    return (selectedMembers.containsKey (dataset) ? selectedMembers.get (dataset) : "");
  }

  //----------------------------------------------------------------------------------- //
  boolean isPds ()
  //----------------------------------------------------------------------------------- //
  {
    return dataset != null && dataset.isPds ();
  }

  //---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  //---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    String dataFileName = dataFile == null ? "" : dataFile.getName ();

    text.append (String.format ("Name............. %s%n", name));
    text.append (String.format ("Dataset.......... %s%n", dataset));
    text.append (String.format ("Datafile......... %s%n", dataFileName));
    text.append (String.format ("CatalogEntry..... %s", catalogEntry));

    return text.toString ();
  }
}
