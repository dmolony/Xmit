package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.*;
import com.bytezone.xmit.CatalogEntry.ModuleType;
import com.bytezone.xmit.textunit.ControlRecord;

import javafx.scene.input.KeyCode;

public class HeadersTab extends XmitTab
{

  public HeadersTab (OutputPane parent, String title, KeyCode keyCode)
  {
    super (parent, title, keyCode);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  List<String> getLines ()
  //----------------------------------------------------------------------------------- //
  {
    List<String> lines = new ArrayList<> ();
    if (parent.dataset == null)
      return lines;

    Reader reader = parent.dataset.getReader ();

    if (reader.size () > 1)
    {
      Dataset firstDataset = reader.getDatasets ().get (0);
      if (firstDataset.isPs ())
      {
        FlatFile file = ((PsDataset) firstDataset).getMember ();
        for (String s : file.getLines ())
          lines.add (s);
        lines.add ("");
      }
      else
        lines.add (
            "Unexpected disposition for file #1: " + firstDataset.getDisposition ());
    }

    for (ControlRecord controlRecord : parent.dataset.getReader ().getControlRecords ())
      lines.add (controlRecord.toString ());

    if (parent.dataset.isPds ())
    {
      PdsDataset pdsDataset = (PdsDataset) parent.dataset;
      lines.add ("COPYR1");
      //      lines.add (pdsDataset.getCopyR1 ());
      lines.add ("");
      lines.add ("COPYR2");
      //      lines.add (pdsDataset.getCopyR2 ());
      lines.add ("");

      lines.add (
          String.format ("%s Catalog Blocks:", parent.dataset.getReader ().getName ()));

      if (pdsDataset.getModuleType () == ModuleType.BASIC)
        lines.add (BasicModule.getDebugHeader ());
      else
        lines.add (LoadModule.getDebugHeader ());

      for (CatalogEntry catalogEntry : pdsDataset)
        lines.add (catalogEntry.getDebugLine ());
    }

    return lines;
  }

}
