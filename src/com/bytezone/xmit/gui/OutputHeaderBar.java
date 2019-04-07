package com.bytezone.xmit.gui;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PsDataset;

// ----------------------------------------------------------------------------------- //
public class OutputHeaderBar extends HeaderBar
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener
//----------------------------------------------------------------------------------- //
{
  private boolean truncateLines;
  Dataset dataset;                // usually file #1 in the Reader
  DataFile dataFile;              // FlatFile or PdsMember
  CatalogEntry catalogEntry;      // needed for alias members

  //----------------------------------------------------------------------------------- //
  public OutputHeaderBar ()
  //----------------------------------------------------------------------------------- //
  {
    super ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  //----------------------------------------------------------------------------------- //
  {
    this.dataset = dataset;
    dataFile = null;

    if (dataset == null)
    {
      leftLabel.setText ("");
      rightLabel.setText ("");
    }
    else
    {
      rightLabel.setText (dataset.getDisposition ().toString ());
      if (dataset.isPs ())
      {
        dataFile = ((PsDataset) dataset).getFlatFile ();
        updateNameLabel ();
      }
    }
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  //----------------------------------------------------------------------------------- //
  {
    if (dataset == null || dataset.isPs ())
      return;

    this.catalogEntry = catalogEntry;
    dataFile = catalogEntry == null ? null : catalogEntry.getMember ();
    updateNameLabel ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void showLinesSelected (boolean showLines, boolean stripLines,
      boolean truncateLines, boolean expandInclude)
  //----------------------------------------------------------------------------------- //
  {
    this.truncateLines = truncateLines;
    updateNameLabel ();              // toggle the '<-' indicator
  }

  //----------------------------------------------------------------------------------- //
  private void updateNameLabel ()
  //----------------------------------------------------------------------------------- //
  {
    if (dataset == null || catalogEntry == null)
    {
      leftLabel.setText ("");
      return;
    }

    String indicator = truncateLines ? "<-" : "";

    if (dataset.isPds ())
    {
      if (catalogEntry.isAlias ())
        leftLabel.setText (indicator + catalogEntry.getMemberName () + " -> "
            + catalogEntry.getAliasName ());
      else
        leftLabel.setText (indicator + catalogEntry.getMemberName ());
    }
    else
      leftLabel.setText (indicator + dataFile.getName ());
  }
}
