package com.bytezone.xmit.gui;

// ----------------------------------------------------------------------------------- //
class OutputHeaderBar extends HeaderBar
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener
//----------------------------------------------------------------------------------- //
{
  //  private final OutputPane parent;
  private LineDisplayStatus lineDisplayStatus;
  private DatasetStatus datasetStatus;
  //  Dataset dataset;                // usually file #1 in the Reader
  //  DataFile dataFile;              // FlatFile or PdsMember
  //  CatalogEntry catalogEntry;      // needed for alias members

  //----------------------------------------------------------------------------------- //
  public OutputHeaderBar ()
  //----------------------------------------------------------------------------------- //
  {
    //    this.parent = parent;
  }

  //----------------------------------------------------------------------------------- //
  void updateNameLabel (boolean truncateLines)
  //----------------------------------------------------------------------------------- //
  {
    //    Dataset dataset = parent.dataset;
    //    CatalogEntry catalogEntry = parent.catalogEntry;
    //    DataFile dataFile = parent.dataFile;

    if (datasetStatus == null || datasetStatus.dataset == null
        || datasetStatus.catalogEntry == null)
    {
      leftLabel.setText ("");
      return;
    }

    String indicator = truncateLines ? "<-" : "";

    if (datasetStatus.dataset.isPds ())
    {
      String memberName = indicator + datasetStatus.catalogEntry.getMemberName ();
      if (datasetStatus.catalogEntry.isAlias ())
        leftLabel
            .setText (memberName + " -> " + datasetStatus.catalogEntry.getAliasName ());
      else
        leftLabel.setText (memberName);
    }
    else
      leftLabel.setText (indicator + datasetStatus.dataFile.getName ());
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void showLinesSelected (LineDisplayStatus lineDisplayStatus)
  //----------------------------------------------------------------------------------- //
  {
    this.lineDisplayStatus = lineDisplayStatus;
    updateNameLabel (lineDisplayStatus.truncateLines);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
    this.datasetStatus = datasetStatus;

    //    if (dataset != null && dataset.isPds ())
    //    {
    //      String datasetName = dataset.getReader ().getFileName ();
    //      if (!datasets.containsKey (datasetName))
    //        datasets.put (datasetName, (PdsDataset) dataset);
    //    }
    rightLabel.setText (datasetStatus.dataset == null ? ""
        : datasetStatus.dataset.getDisposition ().toString ());
    updateNameLabel (lineDisplayStatus.truncateLines);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void tableItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
    updateNameLabel (lineDisplayStatus.truncateLines);

    //    if (dataset == null || dataset.isPs ())
    //      return;
    //
    //    this.catalogEntry = catalogEntry;
    //    dataFile = catalogEntry == null ? null : catalogEntry.getMember ();
  }
}
