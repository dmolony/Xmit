package com.bytezone.xmit.gui;

// ----------------------------------------------------------------------------------- //
class OutputHeaderBar extends HeaderBar
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener
//----------------------------------------------------------------------------------- //
{
  private LineDisplayStatus lineDisplayStatus;
  private DatasetStatus datasetStatus;

  //----------------------------------------------------------------------------------- //
  void updateNameLabel (boolean truncateLines)
  //----------------------------------------------------------------------------------- //
  {
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
  }
}
