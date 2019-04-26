package com.bytezone.xmit.gui;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Dataset;

// ----------------------------------------------------------------------------------- //
class OutputHeaderBar extends HeaderBar
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener
//----------------------------------------------------------------------------------- //
{
  //  private final OutputPane parent;
  private LineDisplayStatus lineDisplayStatus;
  Dataset dataset;                // usually file #1 in the Reader
  DataFile dataFile;              // FlatFile or PdsMember
  CatalogEntry catalogEntry;      // needed for alias members

  //----------------------------------------------------------------------------------- //
  public OutputHeaderBar ()
  //----------------------------------------------------------------------------------- //
  {
    //    this.parent = parent;
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  //----------------------------------------------------------------------------------- //
  {
    this.dataset = dataset;

    //    if (dataset != null && dataset.isPds ())
    //    {
    //      String datasetName = dataset.getReader ().getFileName ();
    //      if (!datasets.containsKey (datasetName))
    //        datasets.put (datasetName, (PdsDataset) dataset);
    //    }
    rightLabel.setText (dataset == null ? "" : dataset.getDisposition ().toString ());
    updateNameLabel (lineDisplayStatus.truncateLines);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  //----------------------------------------------------------------------------------- //
  {
    updateNameLabel (lineDisplayStatus.truncateLines);
    if (dataset == null || dataset.isPs ())
      return;

    this.catalogEntry = catalogEntry;
    dataFile = catalogEntry == null ? null : catalogEntry.getMember ();
  }

  //----------------------------------------------------------------------------------- //
  void updateNameLabel (boolean truncateLines)
  //----------------------------------------------------------------------------------- //
  {
    //    Dataset dataset = parent.dataset;
    //    CatalogEntry catalogEntry = parent.catalogEntry;
    //    DataFile dataFile = parent.dataFile;

    if (dataset == null || catalogEntry == null)
    {
      leftLabel.setText ("");
      return;
    }

    String indicator = truncateLines ? "<-" : "";

    if (dataset.isPds ())
    {
      String memberName = indicator + catalogEntry.getMemberName ();
      if (catalogEntry.isAlias ())
        leftLabel.setText (memberName + " -> " + catalogEntry.getAliasName ());
      else
        leftLabel.setText (memberName);
    }
    else
      leftLabel.setText (indicator + dataFile.getName ());
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void showLinesSelected (LineDisplayStatus lineDisplayStatus)
  //----------------------------------------------------------------------------------- //
  {
    this.lineDisplayStatus = lineDisplayStatus;
    updateNameLabel (lineDisplayStatus.truncateLines);
  }
}
