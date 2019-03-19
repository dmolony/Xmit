package com.bytezone.xmit.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.xmit.*;
import com.bytezone.xmit.CatalogEntry.ModuleType;
import com.bytezone.xmit.textunit.ControlRecord;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;

// ------------------------------------------------------------------------------------ //
class OutputPane extends HeaderTabPane
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener,
    FontChangeListener, OutputWriter, SaveState
//------------------------------------------------------------------------------------- //
{
  private static Pattern includePattern =
      Pattern.compile ("^//\\s+JCLLIB\\s+ORDER=\\((" + Utility.validName + ")\\)$");
  private static Pattern memberPattern =
      Pattern.compile ("INCLUDE\\s+MEMBER=(" + Utility.validPart + ")");
  private static Pattern dsnPattern = Pattern
      .compile ("DSN=(" + Utility.validName + ")\\((" + Utility.validPart + ")\\)");

  //  private static final String divider =
  //      "//* --------------------------------------------------------------------\n";
  private static final int MAX_HEX_BYTES = 0x20000;
  private static final int MAX_LINES = 2500;
  private static final String TRUNCATE_MESSAGE1 =
      "\n*** Output truncated at %,d lines to improve rendering time ***";
  private static final String TRUNCATE_MESSAGE2 =
      "\n***      To see the entire file, use File -> Save Output      ***";
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
  private final Map<String, PdsDataset> datasets = new TreeMap<> ();

  private boolean showLines;
  private boolean stripLines;
  private boolean truncateLines;
  private boolean expandInclude;

  private String includeDatasetName;

  //----------------------------------------------------------------------------------- //
  OutputPane ()
  //----------------------------------------------------------------------------------- //
  {
    headersTab = createTab ("Headers", KeyCode.H, () -> updateHeadersTab ());
    blocksTab = createTab ("Blocks", KeyCode.B, () -> updateBlocksTab ());
    hexTab = createTab ("Hex", KeyCode.X, () -> updateHexTab ());
    outputTab = createTab ("Output", KeyCode.O, () -> updateOutputTab ());

    setTop (getHBox (lblMemberName, lblDisposition));
  }

  //----------------------------------------------------------------------------------- //
  private String updateHeadersTab ()
  //----------------------------------------------------------------------------------- //
  {
    if (dataset == null)
      return "";

    Reader reader = dataset.getReader ();

    StringBuilder text = new StringBuilder ();
    if (reader.size () > 1)
    {
      Dataset firstDataset = reader.getDatasets ().get (0);
      if (firstDataset.isPs ())
      {
        FlatFile file = ((PsDataset) firstDataset).getMember ();
        for (String s : file.getLines ())
          text.append (s + "\n");
        text.append ("\n\n");
      }
      else
        text.append (
            "Unexpected disposition for file #1: " + firstDataset.getDisposition ());
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

      if (pdsDataset.getModuleType () == ModuleType.BASIC)
        text.append (BasicModule.getDebugHeader () + "\n");
      else
        text.append (LoadModule.getDebugHeader () + "\n");

      for (CatalogEntry catalogEntry : pdsDataset)
        text.append (catalogEntry.getDebugLine () + "\n");
    }

    Utility.removeTrailingNewlines (text);
    return text.toString ();
  }

  //----------------------------------------------------------------------------------- //
  private String updateBlocksTab ()
  //----------------------------------------------------------------------------------- //
  {
    if (dataFile == null)
      return "";

    StringBuilder text = new StringBuilder ();

    if (dataFile instanceof PdsMember)
      text.append (((PdsMember) dataFile).listSizeCounts ());
    text.append (dataFile.toString ());

    return text.toString ();
  }

  //----------------------------------------------------------------------------------- //
  private String updateHexTab ()
  //----------------------------------------------------------------------------------- //
  {
    if (dataFile == null)
      return "";

    byte[] buffer = dataFile.getDataBuffer ();
    return Utility.getHexDump (buffer, 0, Math.min (MAX_HEX_BYTES, buffer.length));
  }

  //----------------------------------------------------------------------------------- //
  private String updateOutputTab ()
  //----------------------------------------------------------------------------------- //
  {
    if (dataFile == null)
      return "";

    return getLines (MAX_LINES, showLines, stripLines, truncateLines);
  }

  //----------------------------------------------------------------------------------- //
  private String getLines (int maxLines, boolean showLines, boolean stripLines,
      boolean truncate)
  //----------------------------------------------------------------------------------- //
  {
    List<String> lines = dataFile.getLines ();

    StringBuilder text = new StringBuilder ();
    int lineNo = 0;
    includeDatasetName = "";

    if (maxLines == 0)
      maxLines = Integer.MAX_VALUE;

    for (String line : lines)
    {
      if (++lineNo > maxLines)
      {
        text.append (String.format (TRUNCATE_MESSAGE1, maxLines));
        text.append (TRUNCATE_MESSAGE2);
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

      if (expandInclude)
        checkInclude (line, text);
    }

    Utility.removeTrailingNewlines (text);
    return text.toString ();
  }

  //----------------------------------------------------------------------------------- //
  private void checkInclude (String line, StringBuilder text)
  //----------------------------------------------------------------------------------- //
  {
    if (!includeDatasetName.isEmpty ())
    {
      String leader = "  ==> ";
      String leader2 = showLines ? "      " : "";
      Matcher m2 = memberPattern.matcher (line);
      if (m2.find ())
      {
        String memberName = m2.group (1);
        List<String> lines = findMember (includeDatasetName, memberName);
        if (lines.size () == 0)
          text.append (leader + includeDatasetName + "(" + memberName + ")"
              + ": dataset not seen yet\n");
        else
          for (String line2 : lines)
            if (!line2.startsWith ("//*"))
              text.append (leader2 + line2 + "\n");
      }
    }
    Matcher m = includePattern.matcher (line);
    if (m.find ())
      includeDatasetName = m.group (1);

    Matcher m2 = dsnPattern.matcher (line);
    if (m2.find ())
    {
      String datasetName = m2.group (1);
      String memberName = m2.group (2);
      List<String> lines = findMember (datasetName, memberName);
      String leader1 = showLines ? "        ==> " : "  ==> ";
      String leader2 = showLines ? "        " : "  ";
      if (lines.size () == 0)
        text.append (
            leader1 + datasetName + "(" + memberName + ")" + ": dataset not seen yet\n");
      for (String line2 : lines)
        if (!line2.startsWith ("*"))
          text.append (leader2 + line2 + "\n");
    }
  }

  //----------------------------------------------------------------------------------- //
  private String strip (String line)
  //----------------------------------------------------------------------------------- //
  {
    if (line.length () < 72 || line.length () > 80)
      return line;
    String numbers = line.substring (72);
    for (char c : numbers.toCharArray ())
      if ((c < '0' || c > '9') && c != ' ')
        return line;
    return line.substring (0, 72).stripTrailing ();
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
      tabPane.getTabs ().add (headersTab.tab);
    if (blocksVisible)
      tabPane.getTabs ().add (blocksTab.tab);
    if (hexVisible)
      tabPane.getTabs ().add (hexTab.tab);

    tabPane.getTabs ().add (outputTab.tab);         // always visible
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
  private List<String> findMember (String datasetName, String memberName)
  //----------------------------------------------------------------------------------- //
  {
    if (datasets.containsKey (datasetName))
    {
      PdsDataset dataset = datasets.get (datasetName);
      Optional<PdsMember> optMember = dataset.findMember (memberName);
      if (optMember.isPresent ())
        return optMember.get ().getLines ();
    }
    return new ArrayList<String> ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void showLinesSelected (boolean showLines, boolean stripLines,
      boolean truncateLines, boolean expandInclude)
  //----------------------------------------------------------------------------------- //
  {
    this.showLines = showLines;
    this.stripLines = stripLines;
    this.truncateLines = truncateLines;
    this.expandInclude = expandInclude;

    updateNameLabel ();              // toggle the '<-' indicator

    saveScrollBars ();
    clearText ();
    updateCurrentTab ();
    restoreScrollBars ();
  }

  //----------------------------------------------------------------------------------- //
  public void selectCodePage ()
  //----------------------------------------------------------------------------------- //
  {
    saveScrollBars ();
    clearText ();
    updateCurrentTab ();
    restoreScrollBars ();
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
