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
    if (datasetStatus == null || !datasetStatus.hasDataset ()
        || !datasetStatus.hasCatalogEntry ())
    {
      leftLabel.setText ("");
      return;
    }

    String indicator = truncateLines ? "<-" : "";

    if (datasetStatus.isPds ())
    {
      String memberName = indicator + datasetStatus.getMemberName ();
      if (datasetStatus.isAlias ())
        leftLabel.setText (memberName + " -> " + datasetStatus.getAliasName ());
      else
        leftLabel.setText (memberName);
    }
    else
      leftLabel.setText (indicator + datasetStatus.getDataFileName ());
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

    rightLabel.setText (
        !datasetStatus.hasDataset () ? "" : datasetStatus.getDisposition ().toString ());
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
