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
import javafx.scene.input.KeyCode;

// ------------------------------------------------------------------------------------ //
class OutputPane extends HeaderTabPane
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener,
    FontChangeListener, OutputWriter, SaveState, FilterListener
//------------------------------------------------------------------------------------- //
{
  private static final String PREFS_LAST_TAB = "lastTab";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  Dataset dataset;                // usually file #1 in the Reader
  DataFile dataFile;              // FlatFile or PdsMember
  CatalogEntry catalogEntry;      // needed for alias members
  private boolean truncateLines;

  // keep track of all PDS datasets seen so that we can INCLUDE members
  final Map<String, PdsDataset> datasets = new TreeMap<> ();

  final HeadersTab headersTab = new HeadersTab (this, "Headers", KeyCode.H);
  final BlocksTab blocksTab = new BlocksTab (this, "Blocks", KeyCode.B);
  final HexTab hexTab = new HexTab (this, "Hex", KeyCode.X);
  final OutputTab outputTab = new OutputTab (this, "Output", KeyCode.O);

  final OutputHeaderBar outputHeaderBar = new OutputHeaderBar (this);

  //----------------------------------------------------------------------------------- //
  OutputPane ()
  //----------------------------------------------------------------------------------- //
  {
    xmitTabs.add (headersTab);
    xmitTabs.add (blocksTab);
    xmitTabs.add (hexTab);
    xmitTabs.add (outputTab);

    setTop (outputHeaderBar);
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
      tabPane.getTabs ().add (headersTab);
    if (blocksVisible)
      tabPane.getTabs ().add (blocksTab);
    if (hexVisible)
      tabPane.getTabs ().add (hexTab);

    tabPane.getTabs ().add (outputTab);         // always visible
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  //----------------------------------------------------------------------------------- //
  {
    this.dataset = dataset;
    dataFile = null;

    if (dataset != null)
      if (dataset.isPs ())
        dataFile = ((PsDataset) dataset).getFlatFile ();
      else if (dataset.isPds ())
      {
        String datasetName = dataset.getReader ().getFileName ();
        if (!datasets.containsKey (datasetName))
          datasets.put (datasetName, (PdsDataset) dataset);
      }

    clearText ();
    updateCurrentTab ();
    outputHeaderBar.updateNameLabel (truncateLines);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  //----------------------------------------------------------------------------------- //
  {
    if (dataset == null || dataset.isPs ())
      return;

    this.catalogEntry = catalogEntry;
    dataFile = catalogEntry == null ? null : catalogEntry.getMember ();

    clearText ();
    updateCurrentTab ();
    outputHeaderBar.updateNameLabel (truncateLines);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void showLinesSelected (boolean showLines, boolean stripLines,
      boolean truncateLines, boolean expandInclude)
  //----------------------------------------------------------------------------------- //
  {
    this.truncateLines = truncateLines;
    outputTab.showLinesSelected (showLines, stripLines, truncateLines, expandInclude);

    clearText ();
    updateCurrentTab ();
    outputHeaderBar.updateNameLabel (truncateLines);
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
      for (String line : outputTab.getLines (0))
        output.write (line + "\n");
      Utility.showAlert (AlertType.INFORMATION, "Success",
          "File Saved: " + file.getName ());
    }
    catch (IOException e)
    {
      Utility.showAlert (AlertType.ERROR, "Error", "File Error: " + e.getMessage ());
    }
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void setFilter (String filter, boolean fullFilter)
  //----------------------------------------------------------------------------------- //
  {
    outputTab.setFilter (filter, fullFilter);
    clearText ();
    updateCurrentTab ();
  }
}
