package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.BasicModule;
import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.CatalogEntry.ModuleType;
import com.bytezone.xmit.LoadModule;
import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.XmitReader;
import com.bytezone.xmit.textunit.ControlRecord;

import javafx.scene.input.KeyCode;

// -----------------------------------------------------------------------------------//
class HeadersTab extends XmitTextTab implements TreeItemSelectionListener
// -----------------------------------------------------------------------------------//
{
  DatasetStatus datasetStatus;

  // ---------------------------------------------------------------------------------//
  public HeadersTab (String title, KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    super (title, keyCode);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  List<String> getLines ()
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = new ArrayList<> ();

    if (datasetStatus == null || !datasetStatus.hasDataset ())
      return lines;

    for (ControlRecord controlRecord : ((XmitReader) datasetStatus.getReader ())
        .getControlRecords ())
      lines.add (controlRecord.toString ());

    if (datasetStatus.isPds ())
    {
      PdsDataset pdsDataset = (PdsDataset) datasetStatus.getDataset ();
      lines.add ("COPYR1");
      lines.addAll (pdsDataset.getCopyR1 ().toLines ());
      lines.add ("");
      lines.add ("COPYR2");
      lines.addAll (pdsDataset.getCopyR2 ().toLines ());
      lines.add ("");

      lines
          .add (String.format ("%s Catalog Blocks:", datasetStatus.getReaderFileName ()));

      if (pdsDataset.getModuleType () == ModuleType.BASIC)
        lines.add (BasicModule.getDebugHeader ());
      else
        lines.add (LoadModule.getDebugHeader ());

      for (CatalogEntry catalogEntry : pdsDataset)
        lines.add (catalogEntry.getDebugLine ());
    }

    return lines;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (DatasetStatus datasetStatus)
  // ---------------------------------------------------------------------------------//
  {
    this.datasetStatus = datasetStatus;
    refresh ();
  }
}
