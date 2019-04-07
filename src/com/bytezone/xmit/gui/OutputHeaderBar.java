package com.bytezone.xmit.gui;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Dataset;

// ----------------------------------------------------------------------------------- //
public class OutputHeaderBar extends HeaderBar implements TreeItemSelectionListener
//----------------------------------------------------------------------------------- //
{
  private final OutputPane parent;
  //  private boolean truncateLines;

  //----------------------------------------------------------------------------------- //
  public OutputHeaderBar (OutputPane parent)
  //----------------------------------------------------------------------------------- //
  {
    this.parent = parent;
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  //----------------------------------------------------------------------------------- //
  {
    rightLabel.setText (dataset == null ? "" : dataset.getDisposition ().toString ());
  }

  //----------------------------------------------------------------------------------- //
  void updateNameLabel (boolean truncateLines)
  //----------------------------------------------------------------------------------- //
  {
    Dataset dataset = parent.dataset;
    CatalogEntry catalogEntry = parent.catalogEntry;
    DataFile dataFile = parent.dataFile;

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
}
