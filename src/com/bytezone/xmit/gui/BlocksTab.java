package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.PdsMember;

import javafx.scene.input.KeyCode;

// ----------------------------------------------------------------------------------- //
class BlocksTab extends XmitTab
    implements TreeItemSelectionListener, TableItemSelectionListener
//----------------------------------------------------------------------------------- //
{
  //  Dataset dataset;                // usually file #1 in the Reader
  //  DataFile dataFile;              // FlatFile or PdsMember
  //  CatalogEntry catalogEntry;      // needed for alias members
  DatasetStatus datasetStatus;

  //----------------------------------------------------------------------------------- //
  public BlocksTab (String title, KeyCode keyCode)
  //----------------------------------------------------------------------------------- //
  {
    super (title, keyCode);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  List<String> getLines ()
  //----------------------------------------------------------------------------------- //
  {
    List<String> lines = new ArrayList<> ();

    //    DataFile dataFile = parent.dataFile;              // improve this
    if (datasetStatus.dataFile == null)
      return lines;

    if (datasetStatus.dataFile instanceof PdsMember)
      ((PdsMember) datasetStatus.dataFile).listSizeCounts (lines);

    lines.add (datasetStatus.dataFile.toString ());

    return lines;
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
    this.datasetStatus = datasetStatus;
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void tableItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
    //    if (datasetStatus.dataset == null || datasetStatus.dataset.isPs ())
    //      return;
    //
    ////    this.catalogEntry = catalogEntry;
    //    dataFile = catalogEntry == null ? null : catalogEntry.getMember ();
  }
}
