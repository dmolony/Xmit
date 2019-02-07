package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

import com.bytezone.xmit.*;
import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg.Org;

import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;

// ---------------------------------------------------------------------------------//
class OutputPane extends HeaderTabPane implements TreeItemSelectionListener,
    TableItemSelectionListener, ShowLinesListener, FontChangeListener
// ---------------------------------------------------------------------------------//
{
  private static final String PREFS_LAST_TAB = "lastTab";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final XmitTab headersTab;
  private final XmitTab blocksTab;
  private final XmitTab hexTab;
  private final XmitTab outputTab;

  private final Label lblMemberName = new Label ();
  private final Label lblDisposition = new Label ();

  //  private Reader reader;
  private Dataset dataset;
  private DataFile dataFile;
  private CatalogEntry catalogEntry;      // needed for alias members

  private boolean showLines;
  private boolean stripLines;
  private boolean truncateLines;
  private Disposition disposition;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  OutputPane ()
  {
    headersTab = createTab ("Headers", KeyCode.H, () -> updateHeadersTab ());
    blocksTab = createTab ("Blocks", KeyCode.B, () -> updateBlocksTab ());
    hexTab = createTab ("Hex", KeyCode.X, () -> updateHexTab ());
    outputTab = createTab ("Output", KeyCode.O, () -> updateOutputTab ());

    setTop (getHBox (lblMemberName, lblDisposition));
  }

  // ---------------------------------------------------------------------------------//
  // updateHeadersTab
  // ---------------------------------------------------------------------------------//

  private void updateHeadersTab ()
  {
    if (dataset == null)
      return;

    Reader reader = dataset.getReader ();

    StringBuilder text = new StringBuilder ();
    if (reader.size () > 1)
    {
      Dataset firstDataset = reader.getDatasets ().get (0);
      Disposition disposition = firstDataset.getDisposition ();
      if (firstDataset.isPs ())
      {
        FlatFile file = ((PsDataset) firstDataset).getMember ();
        text.append (file.getLines (false, false, false));
        text.append ("\n\n");
      }
      else
        text.append ("Unexpected disposition for file #1: " + disposition.getOrg ());
    }

    for (ControlRecord controlRecord : dataset.getReader ().getControlRecords ())
    {
      text.append (controlRecord.toString ());
      text.append ("\n");
    }
    text.deleteCharAt (text.length () - 1);

    if (disposition.getOrg () == Org.PDS)
    {
      text.append ("COPYR1\n");
      text.append (((PdsDataset) dataset).getCopyR1 ());
      text.append ("\n\n");
      text.append ("COPYR2\n");
      text.append (((PdsDataset) dataset).getCopyR2 ());
      text.append ("\n\n");

      text.append (
          String.format ("%s Catalog Blocks:%n", dataset.getReader ().getName ()));
      text.append (
          "   --name-- ---id--- -ttr-- versn    ss -created--  -modified-  hh mm ");
      text.append ("Size1 Size2       -------- user ---------\n");

      for (CatalogEntry catalogEntry : ((PdsDataset) dataset).getCatalogEntries ())
      {
        text.append (catalogEntry.debugLine ());
        text.append ("\n");
      }
    }

    Utility.removeTrailingNewlines (text);
    headersTab.setText (text.toString ());
  }

  // ---------------------------------------------------------------------------------//
  // updateBlocksTab
  // ---------------------------------------------------------------------------------//

  private void updateBlocksTab ()
  {
    if (dataFile != null)
      blocksTab.setText (dataFile.toString ());
  }

  // ---------------------------------------------------------------------------------//
  // updateHexTab
  // ---------------------------------------------------------------------------------//

  private void updateHexTab ()
  {
    if (dataFile == null)
      return;

    byte[] buffer = dataFile.getDataBuffer ();

    if (buffer != null)
    {
      int max = Math.min (0x20000, buffer.length);
      hexTab.setText (Utility.getHexDump (buffer, 0, max));
    }
  }

  // ---------------------------------------------------------------------------------//
  // updateOutputTab
  // ---------------------------------------------------------------------------------//

  private void updateOutputTab ()
  {
    if (dataFile != null)
      outputTab.setText (dataFile.getLines (showLines, stripLines, truncateLines));
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  void restore ()
  {
    tabPane.getSelectionModel ().select (prefs.getInt (PREFS_LAST_TAB, 0));
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.putInt (PREFS_LAST_TAB, tabPane.getSelectionModel ().getSelectedIndex ());
  }

  // ---------------------------------------------------------------------------------//
  // setTabVisible
  // ---------------------------------------------------------------------------------//

  void setTabVisible (boolean headersVisible, boolean blocksVisible, boolean hexVisible)
  {
    tabPane.getTabs ().clear ();

    if (headersVisible)
      tabPane.getTabs ().add (headersTab.tab);
    if (blocksVisible)
      tabPane.getTabs ().add (blocksTab.tab);
    if (hexVisible)
      tabPane.getTabs ().add (hexTab.tab);

    tabPane.getTabs ().add (outputTab.tab);         // always visible
  }

  // ---------------------------------------------------------------------------------//
  // treeItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemSelected (Dataset dataset, String name)
  {
    this.dataset = dataset;

    dataFile = null;

    if (dataset == null)
    {
      lblMemberName.setText ("");
      disposition = null;
      lblDisposition.setText ("");
    }
    else
    {
      disposition = dataset.getDisposition ();
      lblDisposition.setText (disposition.toString ());
      if (disposition.getOrg () == Org.PS)
      {
        dataFile = ((PsDataset) dataset).getMember ();
        updateName ();
      }
    }

    clearText ();
    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  // tableItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  {
    this.catalogEntry = catalogEntry;
    this.dataFile = catalogEntry.getMember ();
    updateName ();
    clearText ();
    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  // updateName
  // ---------------------------------------------------------------------------------//

  private void updateName ()
  {
    if (dataset == null)
      return;

    String indicator = "";
    if (truncateLines && stripLines)
      indicator = "<--";
    else if (truncateLines || stripLines)
      indicator = "<-";

    if (dataset.isPds ())
    {
      if (catalogEntry.isAlias ())
        lblMemberName.setText (indicator + catalogEntry.getMemberName ().trim () + " -> "
            + catalogEntry.getAliasName ());
      else
        lblMemberName.setText (indicator + catalogEntry.getMemberName ());
    }
    else
      lblMemberName.setText (indicator + dataFile.getName ());
  }

  // ---------------------------------------------------------------------------------//
  // showLinesSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void showLinesSelected (boolean showLines, boolean stripLines,
      boolean truncateLines)
  {
    this.showLines = showLines;
    this.stripLines = stripLines;
    this.truncateLines = truncateLines;

    saveScrollBars ();
    clearText ();
    updateCurrentTab ();
    updateName ();              // toggle the '<-' indicator
    restoreScrollBars ();
  }

  // ---------------------------------------------------------------------------------//
  // selectCodePage
  // ---------------------------------------------------------------------------------//

  public void selectCodePage ()
  {
    saveScrollBars ();
    clearText ();
    updateCurrentTab ();
    restoreScrollBars ();
  }
}
