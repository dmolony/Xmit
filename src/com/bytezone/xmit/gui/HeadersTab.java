package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.*;
import com.bytezone.xmit.CatalogEntry.ModuleType;
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

    Reader reader = datasetStatus.getReader ();
    if (reader instanceof XmitReader)
      for (ControlRecord controlRecord : ((XmitReader) datasetStatus.getReader ())
          .getControlRecords ())
        lines.add (controlRecord.toString ());
    else if (reader instanceof AwsTapeReader)
    {
      if (datasetStatus.isPds ())
      {
        PdsDataset dataset = (PdsDataset) datasetStatus.getDataset ();

        lines.add ("HDR1");
        lines.add ("-----------------------------------------------------------");
        String header1 = dataset.getAwsTapeDataset ().header1 ();
        for (String line : header1.split ("\n"))
          lines.add (line);
        lines.add ("");

        lines.add ("HDR2");
        lines.add ("-----------------------------------------------------------");
        String header2 = dataset.getAwsTapeDataset ().header2 ();
        for (String line : header2.split ("\n"))
          lines.add (line);
        lines.add ("");
      }
    }

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
