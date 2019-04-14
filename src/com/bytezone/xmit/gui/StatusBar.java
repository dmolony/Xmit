package com.bytezone.xmit.gui;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Filter.FilterMode;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
public class StatusBar extends HBox implements TreeItemSelectionListener,
    TableItemSelectionListener, FilterChangeListener, FilterActionListener
// ---------------------------------------------------------------------------------//
{
  private final Label status = new Label ();
  //  private final ProgressBar progressBar = new ProgressBar ();
  //  private final HBox progressBox = new HBox (10);
  //  private final Label progressCount = new Label ();
  //  private final Label slash = new Label (" / ");
  //  private final Label progressMax = new Label ();
  private String filter = "";
  private boolean fullFilter;
  private FilterMode filterMode;
  private final Label filterSettings = new Label ();
  public static Font statusFont = Font.font ("Monaco", 12);

  // ---------------------------------------------------------------------------------//
  public StatusBar ()
  // ---------------------------------------------------------------------------------//
  {
    super (10);

    //    progressBox.getChildren ().addAll
    //    (progressCount, slash, progressMax, progressBar);

    Region filler = new Region ();
    HBox.setHgrow (filler, Priority.ALWAYS);
    getChildren ().addAll (status, filler, filterSettings);
    setPadding (new Insets (5));
    status.setFont (statusFont);
    filterSettings.setFont (statusFont);
  }

  // ---------------------------------------------------------------------------------//
  void setText (String text)
  // ---------------------------------------------------------------------------------//
  {
    status.setText (text);
  }

  // ---------------------------------------------------------------------------------//
  //  private void showProgress ()
  // ---------------------------------------------------------------------------------//
  //  {
  //    if (!getChildren ().contains (progressBox))
  //      getChildren ().add (progressBox);
  //  }

  // ---------------------------------------------------------------------------------//
  //  private void setProgress (int current, int max)
  // ---------------------------------------------------------------------------------//
  //  {
  //    progressCount.setText (current + "");
  //    progressMax.setText (max + "");
  //  }

  // ---------------------------------------------------------------------------------//
  //  private void hideProgress ()
  // ---------------------------------------------------------------------------------//
  //  {
  //    if (getChildren ().contains (progressBox))
  //      getChildren ().remove (progressBox);
  //  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  // ---------------------------------------------------------------------------------//
  {
    //    if (dataset == null)
    //      status.setText ("");
    //    else
    //      status.setText (dataset.getDisposition ().toString ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    //    if (catalogEntry != null)
    //      status.setText (catalogEntry.getMemberName ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (String filter, boolean fullFilter, FilterMode filterMode)
  // ---------------------------------------------------------------------------------//
  {
    this.filter = filter;
    this.fullFilter = fullFilter;
    this.filterMode = filterMode;

    if (filter.isEmpty ())
      status.setText ("");
    setFilterText ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void filtering (int found, int max, boolean done)
  // ---------------------------------------------------------------------------------//
  {
    if (!done)
      return;

    if (filter.isEmpty ())
      status.setText ("");
    else
    {
      String mode = filterMode.toString ();
      status.setText (String.format ("%s found %d member%s from %d", mode, found,
          (found == 1 ? "" : "s"), max));
    }
  }

  // ---------------------------------------------------------------------------------//
  private void setFilterText ()
  // ---------------------------------------------------------------------------------//
  {
    filterSettings.setText (String.format ("Filter: %-10s  Mode: %-10s  Exclusive: %-10s",
        filter, filterMode, fullFilter));
  }
}
