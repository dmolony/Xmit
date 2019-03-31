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
  private static final int MAX_HEX_BYTES = 0x20_000;
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

  //----------------------------------------------------------------------------------- //
  OutputPane ()
  //----------------------------------------------------------------------------------- //
  {
    headersTab = createStringTab ("Headers", KeyCode.H, () -> updateHeadersTab ());
    blocksTab = createStringTab ("Blocks", KeyCode.B, () -> updateBlocksTab ());
    hexTab = createStringTab ("Hex", KeyCode.X, () -> updateHexTab ());
    outputTab = createStringTab ("Output", KeyCode.O, () -> updateOutputTab ());

    setTop (getHBox (lblMemberName, lblDisposition));
  }

  //----------------------------------------------------------------------------------- //
  private List<String> updateHeadersTab ()
  //----------------------------------------------------------------------------------- //
  {
    List<String> lines = new ArrayList<> ();
    if (dataset == null)
      return lines;

    Reader reader = dataset.getReader ();

    //    StringBuilder text = new StringBuilder ();
    if (reader.size () > 1)
    {
      Dataset firstDataset = reader.getDatasets ().get (0);
      if (firstDataset.isPs ())
      {
        FlatFile file = ((PsDataset) firstDataset).getMember ();
        for (String s : file.getLines ())
          lines.add (s);
        lines.add ("");
        lines.add ("");
      }
      else
        lines.add (
            "Unexpected disposition for file #1: " + firstDataset.getDisposition ());
    }

    for (ControlRecord controlRecord : dataset.getReader ().getControlRecords ())
    {
      lines.add (controlRecord.toString ());
      //      text.append ("\n");
    }
    //    text.deleteCharAt (text.length () - 1);

    if (dataset.isPds ())
    {
      PdsDataset pdsDataset = (PdsDataset) dataset;
      lines.add ("COPYR1");
      //      lines.add (pdsDataset.getCopyR1 ());
      lines.add ("");
      lines.add ("COPYR2");
      //      lines.add (pdsDataset.getCopyR2 ());
      lines.add ("");

      lines.add (String.format ("%s Catalog Blocks:", dataset.getReader ().getName ()));

      if (pdsDataset.getModuleType () == ModuleType.BASIC)
        lines.add (BasicModule.getDebugHeader ());
      else
        lines.add (LoadModule.getDebugHeader ());

      for (CatalogEntry catalogEntry : pdsDataset)
        lines.add (catalogEntry.getDebugLine ());
    }

    //    Utility.removeTrailingNewlines (text);
    return lines;
  }

  //----------------------------------------------------------------------------------- //
  private List<String> updateBlocksTab ()
  //----------------------------------------------------------------------------------- //
  {
    List<String> lines = new ArrayList<> ();
    if (dataFile == null)
      return lines;

    //    StringBuilder text = new StringBuilder ();

    if (dataFile instanceof PdsMember)
      ((PdsMember) dataFile).listSizeCounts (lines);
    lines.add (dataFile.toString ());

    return lines;
  }

  //----------------------------------------------------------------------------------- //
  private List<String> updateHexTab ()
  //----------------------------------------------------------------------------------- //
  {
    List<String> lines = new ArrayList<> ();
    if (dataFile == null)
      return lines;

    byte[] buffer = dataFile.getDataBuffer ();
    return Utility.getHexDumpLines (buffer, 0, Math.min (MAX_HEX_BYTES, buffer.length));
  }

  //----------------------------------------------------------------------------------- //
  private List<String> updateOutputTab ()
  //----------------------------------------------------------------------------------- //
  {
    if (dataFile == null)
      return new ArrayList<> ();

    return getLines (MAX_LINES);
  }

  //----------------------------------------------------------------------------------- //
  private List<String> getLines (int maxLines)
  //----------------------------------------------------------------------------------- //
  {
    //    StringBuilder text = new StringBuilder ();
    List<String> newLines = new ArrayList<> ();

    List<String> lines = dataFile.getLines ();
    int lineNo = 0;
    String includeDatasetName = "";

    boolean isJCL = expandInclude && isJCL (lines);

    if (maxLines == 0)
      maxLines = Integer.MAX_VALUE;

    for (String line : lines)
    {
      if (++lineNo > maxLines)
      {
        newLines.add (String.format (TRUNCATE_MESSAGE1, maxLines));
        newLines.add (TRUNCATE_MESSAGE2);
        break;
      }

      if (stripLines)
        line = strip (line);

      if (showLines)
        newLines.add (String.format ("%05d %s", lineNo, line));
      else if (truncateLines && line.length () > 0)
        newLines.add (line.substring (1));
      else
        newLines.add (line);

      if (isJCL)
        includeDatasetName = checkInclude (includeDatasetName, line, newLines);
    }

    //    Utility.removeTrailingNewlines (text);
    //    return text.toString ();
    return newLines;
  }

  //----------------------------------------------------------------------------------- //
  private boolean isJCL (List<String> lines)
  //----------------------------------------------------------------------------------- //
  {
    return Utility.jobCardPattern.matcher (getFirstNonComment (lines)).find ();
  }

  //----------------------------------------------------------------------------------- //
  private String getFirstNonComment (List<String> lines)
  //----------------------------------------------------------------------------------- //
  {
    for (String line : lines)
      if (!line.startsWith ("//*"))
        return line;
    return "";
  }

  //----------------------------------------------------------------------------------- //
  private String checkInclude (String includeDatasetName, String line,
      List<String> newLines)
  //----------------------------------------------------------------------------------- //
  {
    if (!includeDatasetName.isEmpty ())
    {
      Matcher m = memberPattern.matcher (line);
      if (m.find ())
        append (newLines, includeDatasetName, m.group (1), "//*");
    }

    Matcher m = includePattern.matcher (line);
    if (m.find ())
      includeDatasetName = m.group (1);

    if (false)        // will expand output datasets too
    {
      m = dsnPattern.matcher (line);
      if (m.find ())
        append (newLines, m.group (1), m.group (2), "*");
    }

    return includeDatasetName;
  }

  //----------------------------------------------------------------------------------- //
  private void append (List<String> newLines, String datasetName, String memberName,
      String commentIndicator)
  //----------------------------------------------------------------------------------- //
  {
    String lineGap = showLines ? "      " : "";
    List<String> lines = findMember (datasetName, memberName);
    if (lines.size () == 0)
    {
      newLines.add (String.format ("%s==> %s(%s): dataset not seen yet", lineGap,
          datasetName, memberName));
      return;
    }

    for (String line : lines)
      if (!line.startsWith (commentIndicator))
        newLines.add (String.format ("%s%s", lineGap, line));
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

    //    saveScrollBars ();
    clearText ();
    updateCurrentTab ();
    //    restoreScrollBars ();
  }

  //----------------------------------------------------------------------------------- //
  public void selectCodePage ()
  //----------------------------------------------------------------------------------- //
  {
    //    saveScrollBars ();
    clearText ();
    updateCurrentTab ();
    //    restoreScrollBars ();
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
      for (String line : getLines (0))
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
