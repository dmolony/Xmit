package com.bytezone.xmit.gui;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Utility;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

// ---------------------------------------------------------------------------------//
public class StatusBar extends HBox implements TreeItemSelectionListener,
    TableItemSelectionListener, FilterChangeListener, FilterActionListener
// ---------------------------------------------------------------------------------//
{
  private final Label status = new Label ();
  private final ProgressBar progressBar = new ProgressBar ();
  private final HBox progressBox = new HBox (10);
  private final Label progressCount = new Label ();
  private final Label slash = new Label (" / ");
  private final Label progressMax = new Label ();

  // ---------------------------------------------------------------------------------//
  public StatusBar ()
  // ---------------------------------------------------------------------------------//
  {
    super (10);

    progressBox.getChildren ().addAll (progressCount, slash, progressMax, progressBar);

    Region filler = new Region ();
    HBox.setHgrow (filler, Priority.ALWAYS);
    getChildren ().addAll (status, filler);
    setPadding (new Insets (5));
    status.setFont (Utility.statusFont);
    //    showProgress ();
  }

  // ---------------------------------------------------------------------------------//
  public void setText (String text)
  // ---------------------------------------------------------------------------------//
  {
    status.setText (text);
  }

  // ---------------------------------------------------------------------------------//
  void showProgress ()
  // ---------------------------------------------------------------------------------//
  {
    if (!getChildren ().contains (progressBox))
      getChildren ().add (progressBox);
    //    System.out.println ("show");
  }

  // ---------------------------------------------------------------------------------//
  void setProgress (int current, int max)
  // ---------------------------------------------------------------------------------//
  {
    progressCount.setText (current + "");
    progressMax.setText (max + "");
  }

  // ---------------------------------------------------------------------------------//
  void hideProgress ()
  // ---------------------------------------------------------------------------------//
  {
    if (getChildren ().contains (progressBox))
      getChildren ().remove (progressBox);
    //    System.out.println ("hide");
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
    if (!filter.isEmpty ())
      status.setText (String.format ("Filter: %s %s", filter, fullFilter ? "(exc)" : ""));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void filtering (int found, int max, boolean done)
  // ---------------------------------------------------------------------------------//
  {
    //    System.out.printf ("Found: %d  Max: %d  Done: %s%n", found, max, done);
    if (done)
      hideProgress ();
    else if (found == 0)
      showProgress ();
  }
}
