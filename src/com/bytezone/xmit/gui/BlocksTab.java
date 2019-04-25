package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsMember;

import javafx.scene.input.KeyCode;

// ----------------------------------------------------------------------------------- //
class BlocksTab extends XmitTab
    implements TreeItemSelectionListener, TableItemSelectionListener
//----------------------------------------------------------------------------------- //
{
  Dataset dataset;                // usually file #1 in the Reader
  DataFile dataFile;              // FlatFile or PdsMember
  CatalogEntry catalogEntry;      // needed for alias members

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
    if (dataFile == null)
      return lines;

    if (dataFile instanceof PdsMember)
      ((PdsMember) dataFile).listSizeCounts (lines);

    lines.add (dataFile.toString ());

    return lines;
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  //----------------------------------------------------------------------------------- //
  {
    this.dataset = dataset;
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
  }
}
