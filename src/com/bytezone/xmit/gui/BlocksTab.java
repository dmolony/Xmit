package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.PdsMember;

import javafx.scene.input.KeyCode;

// ----------------------------------------------------------------------------------- //
class BlocksTab extends XmitTextTab
    implements TreeItemSelectionListener, TableItemSelectionListener
//----------------------------------------------------------------------------------- //
{
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

    if (datasetStatus == null || datasetStatus.getDataFile () == null)
      return lines;

    if (datasetStatus.getDataFile () instanceof PdsMember)
      ((PdsMember) datasetStatus.getDataFile ()).listSizeCounts (lines);

    lines.add (datasetStatus.getDataFile ().toString ());

    return lines;
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
    this.datasetStatus = datasetStatus;
    refresh ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void tableItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
    this.datasetStatus = datasetStatus;
    refresh ();
  }
}
