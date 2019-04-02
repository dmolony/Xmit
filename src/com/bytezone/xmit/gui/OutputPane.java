package com.bytezone.xmit.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import com.bytezone.xmit.*;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;

// ------------------------------------------------------------------------------------ //
class OutputPane extends HeaderTabPane
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener,
    FontChangeListener, OutputWriter, SaveState
//------------------------------------------------------------------------------------- //
{
  private static final String PREFS_LAST_TAB = "lastTab";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final Label lblMemberName = new Label ();
  private final Label lblDisposition = new Label ();

  Dataset dataset;                // usually file #1 in the Reader
  DataFile dataFile;              // FlatFile or PdsMember
  CatalogEntry catalogEntry;      // needed for alias members
  final Map<String, PdsDataset> datasets = new TreeMap<> ();

  private boolean truncateLines;

  //----------------------------------------------------------------------------------- //
  OutputPane ()
  //----------------------------------------------------------------------------------- //
  {
    xmitTabs.add (new HeadersTab (this, "Headers", KeyCode.H));
    xmitTabs.add (new BlocksTab (this, "Blocks", KeyCode.B));
    xmitTabs.add (new HexTab (this, "Hex", KeyCode.X));
    xmitTabs.add (new OutputTab (this, "Output", KeyCode.O));

    setTop (getHBox (lblMemberName, lblDisposition));
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void restore ()
  //----------------------------------------------------------------------------------- //
  {
    tabPane.getSelectionModel ().select (prefs.getInt (PREFS_LAST_TAB, 0));
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void save ()
  //----------------------------------------------------------------------------------- //
  {
    prefs.putInt (PREFS_LAST_TAB, tabPane.getSelectionModel ().getSelectedIndex ());
  }

  //----------------------------------------------------------------------------------- //
  void setTabVisible (boolean headersVisible, boolean blocksVisible, boolean hexVisible)
  //----------------------------------------------------------------------------------- //
  {
    tabPane.getTabs ().clear ();

    if (headersVisible)
      tabPane.getTabs ().add (xmitTabs.get (0).tab);
    if (blocksVisible)
      tabPane.getTabs ().add (xmitTabs.get (1).tab);
    if (hexVisible)
      tabPane.getTabs ().add (xmitTabs.get (2).tab);

    tabPane.getTabs ().add (xmitTabs.get (3).tab);         // always visible
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  //----------------------------------------------------------------------------------- //
  {
    this.dataset = dataset;
    dataFile = null;

    if (dataset == null)
    {
      lblMemberName.setText ("");
      lblDisposition.setText ("");
    }
    else
    {
      lblDisposition.setText (dataset.getDisposition ().toString ());
      if (dataset.isPs ())
      {
        dataFile = ((PsDataset) dataset).getMember ();
        updateNameLabel ();
      }
      else if (dataset.isPds ())
      {
        String datasetName = dataset.getReader ().getFileName ();
        if (!datasets.containsKey (datasetName))
          datasets.put (datasetName, (PdsDataset) dataset);
      }
    }

    clearText ();
    updateCurrentTab ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  //----------------------------------------------------------------------------------- //
  {
    if (dataset != null && dataset.isPs ())
      return;

    this.catalogEntry = catalogEntry;
    clearText ();
    dataFile = catalogEntry == null ? null : catalogEntry.getMember ();
    updateNameLabel ();
    updateCurrentTab ();
  }

  //----------------------------------------------------------------------------------- //
  private void updateNameLabel ()
  //----------------------------------------------------------------------------------- //
  {
    if (dataset == null || catalogEntry == null)
    {
      lblMemberName.setText ("");
      return;
    }

    String indicator = truncateLines ? "<-" : "";

    if (dataset.isPds ())
    {
      if (catalogEntry.isAlias ())
        lblMemberName.setText (indicator + catalogEntry.getMemberName () + " -> "
            + catalogEntry.getAliasName ());
      else
        lblMemberName.setText (indicator + catalogEntry.getMemberName ());
    }
    else
      lblMemberName.setText (indicator + dataFile.getName ());
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void showLinesSelected (boolean showLines, boolean stripLines,
      boolean truncateLines, boolean expandInclude)
  //----------------------------------------------------------------------------------- //
  {
    this.truncateLines = truncateLines;
    ((OutputTab) xmitTabs.get (3)).showLinesSelected (showLines, stripLines,
        truncateLines, expandInclude);

    updateNameLabel ();              // toggle the '<-' indicator

    clearText ();
    updateCurrentTab ();
  }

  //----------------------------------------------------------------------------------- //
  public void selectCodePage ()
  //----------------------------------------------------------------------------------- //
  {
    clearText ();
    updateCurrentTab ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void write (File file)
  //----------------------------------------------------------------------------------- //
  {
    if (file == null)
      return;

    try (BufferedWriter output = new BufferedWriter (new FileWriter (file)))
    {
      for (String line : ((OutputTab) xmitTabs.get (3)).getLines (0))
        output.write (line + "\n");
      Utility.showAlert (AlertType.INFORMATION, "Success",
          "File Saved: " + file.getName ());
    }
    catch (IOException e)
    {
      Utility.showAlert (AlertType.ERROR, "Error", "File Error: " + e.getMessage ());
    }
  }
}
