package com.bytezone.xmit.gui;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Utility;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

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

  // ---------------------------------------------------------------------------------//
  public StatusBar ()
  // ---------------------------------------------------------------------------------//
  {
    super (10);

    //    progressBox.getChildren ().addAll
    //    (progressCount, slash, progressMax, progressBar);

    Region filler = new Region ();
    HBox.setHgrow (filler, Priority.ALWAYS);
    getChildren ().addAll (status, filler);
    setPadding (new Insets (5));
    status.setFont (Utility.statusFont);
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
  public void setFilter (String filter, boolean fullFilter)
  // ---------------------------------------------------------------------------------//
  {
    this.filter = filter;
    if (filter.isEmpty ())
      status.setText ("");
    //    System.out.printf ("%s%n", filter);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void filtering (int found, int max, boolean done)
  // ---------------------------------------------------------------------------------//
  {
    if (filter.isEmpty ())
      status.setText ("");
    else if (done)
      status.setText (String.format ("Filter found %d member%s from %d", found,
          (found == 1 ? "" : "s"), max));
    //    System.out.printf ("%s Found: %d  Max: %d  Done: %s%n", filter, found, max, done);
  }
}
