package com.bytezone.xmit.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.xmit.*;
import com.bytezone.xmit.textunit.ControlRecord;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;

// ---------------------------------------------------------------------------------//
class OutputPane extends HeaderTabPane implements TreeItemSelectionListener,
    TableItemSelectionListener, ShowLinesListener, FontChangeListener, OutputWriter
// ---------------------------------------------------------------------------------//
{
  private static final int MAX_HEX_BYTES = 0x20000;
  private static final int MAX_LINES = 2500;
  private static final String TRUNCATE_MESSAGE =
      "\n*** Output truncated at %,d lines to improve rendering time ***";
  private static final String PREFS_LAST_TAB = "lastTab";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final XmitTab headersTab;
  private final XmitTab blocksTab;
  private final XmitTab hexTab;
  private final XmitTab outputTab;

  private final Label lblMemberName = new Label ();
  private final Label lblDisposition = new Label ();

  private Dataset dataset;                // usually file #1 in the Reader
  private DataFile dataFile;              // FlatFile or PdsMember
  private CatalogEntry catalogEntry;      // needed for alias members

  private boolean showLines;
  private boolean stripLines;
  private boolean truncateLines;

  // ---------------------------------------------------------------------------------//
  OutputPane ()
  // ---------------------------------------------------------------------------------//
  {
    headersTab = createTab ("Headers", KeyCode.H, () -> updateHeadersTab ());
    blocksTab = createTab ("Blocks", KeyCode.B, () -> updateBlocksTab ());
    hexTab = createTab ("Hex", KeyCode.X, () -> updateHexTab ());
    outputTab = createTab ("Output", KeyCode.O, () -> updateOutputTab ());

    setTop (getHBox (lblMemberName, lblDisposition));
  }

  // ---------------------------------------------------------------------------------//
  private void updateHeadersTab ()
  // ---------------------------------------------------------------------------------//
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
        for (String s : file.getLines ())
          text.append (s + "\n");
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

    if (dataset.isPds ())
    {
      PdsDataset pdsDataset = (PdsDataset) dataset;
      text.append ("COPYR1\n");
      text.append (pdsDataset.getCopyR1 ());
      text.append ("\n\n");
      text.append ("COPYR2\n");
      text.append (pdsDataset.getCopyR2 ());
      text.append ("\n\n");

      text.append (
          String.format ("%s Catalog Blocks:%n", dataset.getReader ().getName ()));
      text.append (
          "   --name-- ---id--- -ttr-- versn    ss -created--  -modified-  hh mm ");
      text.append ("Size1 Size2       -------- user ---------\n");

      for (CatalogEntry catalogEntry : pdsDataset.getCatalogEntries ())
      {
        text.append (debugLine (catalogEntry));
        text.append ("\n");
      }
    }

    Utility.removeTrailingNewlines (text);
    headersTab.setText (text.toString ());
  }

  // ---------------------------------------------------------------------------------//
  private String debugLine (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    String hex = "";
    String t1 = "";
    byte[] directoryData = catalogEntry.getDirectoryData ();

    int extra = directoryData[11] & 0xFF;      // indicator byte
    if (extra == 0x2E)
      hex =
          Utility.getHexValues (directoryData, 12, 22) + "                              "
              + Utility.getHexValues (directoryData, 34, 6);
    else if (extra == 0x31)
      hex =
          Utility.getHexValues (directoryData, 12, 22) + "                              "
              + Utility.getHexValues (directoryData, 34, 12);
    else
      hex = Utility.getHexValues (directoryData, 12, directoryData.length - 12);

    if (extra == 0xB6)
      t1 = Utility.getString (directoryData, 48, 8);

    return String.format ("%02X %-8s %-8s %06X %-129s %8s %8s", directoryData[11],
        catalogEntry.getMemberName (), catalogEntry.getUserName (),
        catalogEntry.getOffset (), hex, catalogEntry.getAliasName (), t1).trim ();
  }

  // ---------------------------------------------------------------------------------//
  private void updateBlocksTab ()
  // ---------------------------------------------------------------------------------//
  {
    if (dataFile != null)
      blocksTab.setText (dataFile.toString ());
  }

  // ---------------------------------------------------------------------------------//
  private void updateHexTab ()
  // ---------------------------------------------------------------------------------//
  {
    if (dataFile == null)
      return;

    byte[] buffer = dataFile.getDataBuffer ();
    hexTab.setText (
        Utility.getHexDump (buffer, 0, Math.min (MAX_HEX_BYTES, buffer.length)));
  }

  // ---------------------------------------------------------------------------------//
  private void updateOutputTab ()
  // ---------------------------------------------------------------------------------//
  {
    if (dataFile == null)
      return;

    outputTab.setText (getLines (MAX_LINES, showLines, stripLines, truncateLines));
  }

  // ---------------------------------------------------------------------------------//
  private String getLines (int maxLines, boolean showLines, boolean stripLines,
      boolean truncate)
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = dataFile.getLines ();

    StringBuilder text = new StringBuilder ();
    int lineNo = 0;
    if (maxLines == 0)
      maxLines = Integer.MAX_VALUE;

    for (String line : lines)
    {
      if (++lineNo > maxLines)
      {
        text.append (String.format (TRUNCATE_MESSAGE, maxLines));
        break;
      }
      if (stripLines)
        line = strip (line);

      if (showLines)
        text.append (String.format ("%05d %s%n", lineNo, line));
      else if (truncate && line.length () > 0)
        text.append (String.format ("%s%n", line.substring (1)));
      else
        text.append (String.format ("%s%n", line));
    }

    Utility.removeTrailingNewlines (text);
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private String strip (String line)
  // ---------------------------------------------------------------------------------//
  {
    if (line.length () < 72 || line.length () > 80)
      return line;
    String numbers = line.substring (72);
    for (char c : numbers.toCharArray ())
      if ((c < '0' || c > '9') && c != ' ')
        return line;
    return line.substring (0, 72).stripTrailing ();
  }

  // ---------------------------------------------------------------------------------//
  void restore ()
  // ---------------------------------------------------------------------------------//
  {
    tabPane.getSelectionModel ().select (prefs.getInt (PREFS_LAST_TAB, 0));
  }

  // ---------------------------------------------------------------------------------//
  void exit ()
  // ---------------------------------------------------------------------------------//
  {
    prefs.putInt (PREFS_LAST_TAB, tabPane.getSelectionModel ().getSelectedIndex ());
  }

  // ---------------------------------------------------------------------------------//
  void setTabVisible (boolean headersVisible, boolean blocksVisible, boolean hexVisible)
  // ---------------------------------------------------------------------------------//
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
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  // ---------------------------------------------------------------------------------//
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
    }

    clearText ();
    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    this.catalogEntry = catalogEntry;
    this.dataFile = catalogEntry.getMember ();
    updateNameLabel ();
    clearText ();
    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  private void updateNameLabel ()
  // ---------------------------------------------------------------------------------//
  {
    if (dataset == null)
      return;

    String indicator = truncateLines ? "<-" : "";

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
  @Override
  public void showLinesSelected (boolean showLines, boolean stripLines,
      boolean truncateLines)
  // ---------------------------------------------------------------------------------//
  {
    this.showLines = showLines;
    this.stripLines = stripLines;
    this.truncateLines = truncateLines;

    saveScrollBars ();
    clearText ();
    updateCurrentTab ();
    updateNameLabel ();              // toggle the '<-' indicator
    restoreScrollBars ();
  }

  // ---------------------------------------------------------------------------------//
  public void selectCodePage ()
  // ---------------------------------------------------------------------------------//
  {
    saveScrollBars ();
    clearText ();
    updateCurrentTab ();
    restoreScrollBars ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void write (File file)
  // ---------------------------------------------------------------------------------//
  {
    if (file == null)
      return;

    try (BufferedWriter output = new BufferedWriter (new FileWriter (file)))
    {
      output.write (getLines (0, showLines, stripLines, truncateLines));
      Utility.showAlert (AlertType.INFORMATION, "Success",
          "File Saved: " + file.getName ());
    }
    catch (IOException e)
    {
      Utility.showAlert (AlertType.ERROR, "Error", "File Error: " + e.getMessage ());
    }
  }
}
