package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.FlatFile;
import com.bytezone.xmit.PsDataset;
import com.bytezone.xmit.Reader;

import javafx.scene.input.KeyCode;

public class CommentsTab extends XmitTextTab implements TreeItemSelectionListener
{
  DatasetStatus datasetStatus;

  //----------------------------------------------------------------------------------- //
  public CommentsTab (String title, KeyCode keyCode)
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

    if (datasetStatus == null || datasetStatus.dataset == null)
      return lines;

    Reader reader = datasetStatus.dataset.getReader ();

    if (reader.size () > 1)
    {
      Dataset firstDataset = reader.getDatasets ().get (0);
      if (firstDataset.isPs ())
      {
        FlatFile file = ((PsDataset) firstDataset).getFlatFile ();
        for (String s : file.getLines ())
          lines.add (s);
        lines.add ("");
      }
      else
        lines.add (
            "Unexpected disposition for file #1: " + firstDataset.getDisposition ());
    }
    else
      lines.add ("No comments");

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
}
