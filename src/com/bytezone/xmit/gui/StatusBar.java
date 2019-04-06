package com.bytezone.xmit.gui;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Utility;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

// ---------------------------------------------------------------------------------//
public class StatusBar extends HBox
    implements TreeItemSelectionListener, TableItemSelectionListener, FilterListener
// ---------------------------------------------------------------------------------//
{
  private final Label status = new Label ();

  // ---------------------------------------------------------------------------------//
  public StatusBar ()
  // ---------------------------------------------------------------------------------//
  {
    super (10);

    getChildren ().add (status);
    setPadding (new Insets (5));
    status.setFont (Utility.statusFont);
  }

  // ---------------------------------------------------------------------------------//
  public void setText (String text)
  // ---------------------------------------------------------------------------------//
  {
    status.setText (text);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  // ---------------------------------------------------------------------------------//
  {
    if (dataset == null)
      status.setText ("");
    else
      status.setText (dataset.getDisposition ().toString ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    if (catalogEntry != null)
      status.setText (catalogEntry.getMemberName ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (String filter, boolean fullFilter)
  // ---------------------------------------------------------------------------------//
  {
    status.setText ("Filter: " + filter);
  }
}
