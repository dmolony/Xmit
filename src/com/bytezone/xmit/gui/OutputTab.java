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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.appbase.AppBase;
import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.DataFile;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.PdsMember;
import com.bytezone.xmit.Utility;
import com.bytezone.xmit.gui.XmitTree.TreeNodeListener;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;

// -----------------------------------------------------------------------------------//
class OutputTab extends XmitTextTab implements    //
    ShowLinesListener,                            //
    TableItemSelectionListener,                   //
    FilterChangeListener,                         //
    OutputWriter,                                 //
    CodePageSelectedListener,                     //
    TreeNodeListener
// -----------------------------------------------------------------------------------//
{
  private static final int MAX_LINES = 2500;
  private static final String TRUNCATE_MESSAGE_1 =
      "*** Output truncated at %,d lines to improve rendering time ***";
  private static final String TRUNCATE_MESSAGE_2 =
      "***      To see the entire file, use File -> Save Output      ***";

  private static Pattern includePattern =
      Pattern.compile ("^//\\s+JCLLIB\\s+ORDER=\\((" + Utility.validName + ")\\)$");
  private static Pattern memberPattern = Pattern
      .compile ("^//(" + Utility.validPart + ")?\\s+INCLUDE\\s+MEMBER=(" + Utility.validPart + ")");

  LineDisplayStatus lineDisplayStatus;
  private DataFile dataFile;                    // the item to display

  // keep track of all PDS datasets seen so that we can INCLUDE members
  private final Map<String, PdsDataset> datasets = new TreeMap<> ();

  // ---------------------------------------------------------------------------------//
  public OutputTab (String title, KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    super (title, keyCode);

    textFormatter = new TextFormatterJcl ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  List<String> getLines ()
  // ---------------------------------------------------------------------------------//
  {
    return dataFile == null ? new ArrayList<> () : getLines (MAX_LINES);
  }

  // ---------------------------------------------------------------------------------//
  private List<String> getLines (int maxLines)
  // ---------------------------------------------------------------------------------//
  {
    List<String> newLines = new ArrayList<> ();

    List<String> lines = dataFile.getLines ();     // improve this
    int lineNo = 0;
    String includeDatasetName = "";

    boolean isJCL = lineDisplayStatus.expandInclude && Utility.isJCL (lines);

    if (maxLines == 0)
      maxLines = Integer.MAX_VALUE;

    for (String line : lines)
    {
      if (++lineNo > maxLines)
      {
        newLines.add ("");
        newLines.add (String.format (TRUNCATE_MESSAGE_1, maxLines));
        newLines.add (TRUNCATE_MESSAGE_2);
        break;
      }

      if (lineDisplayStatus.stripLines)
        line = Utility.stripLineNumber (line);

      if (lineDisplayStatus.truncateLines && line.length () > 0)
        newLines.add (line.substring (1));
      else
        newLines.add (line);

      if (isJCL)
        includeDatasetName = checkInclude (includeDatasetName, line, newLines);
    }

    return newLines;
  }

  // ---------------------------------------------------------------------------------//
  private String checkInclude (String includeDatasetName, String line, List<String> newLines)
  // ---------------------------------------------------------------------------------//
  {
    if (!includeDatasetName.isEmpty ())
    {
      Matcher m = memberPattern.matcher (line);
      if (m.find ())
        append (newLines, includeDatasetName, m.group (2), "//*");
    }

    Matcher m = includePattern.matcher (line);
    if (m.find ())
      includeDatasetName = m.group (1);

    return includeDatasetName;
  }

  // ---------------------------------------------------------------------------------//
  private void append (List<String> newLines, String datasetName, String memberName,
      String commentIndicator)
  // ---------------------------------------------------------------------------------//
  {
    Optional<PdsMember> optMember = findMember (datasetName, memberName);
    if (optMember.isEmpty ())
      newLines.add (String.format ("==> %s(%s): dataset not seen yet", datasetName, memberName));
    else
      for (String line : optMember.get ().getLines ())
        if (!line.startsWith (commentIndicator))
          newLines.add (line);
  }

  // ---------------------------------------------------------------------------------//
  Optional<PdsMember> findMember (String datasetName, String memberName)
  // ---------------------------------------------------------------------------------//
  {
    if (datasets.containsKey (datasetName))
      return datasets.get (datasetName).findMember (memberName);

    return Optional.empty ();
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
      for (String line : getLines (0))
        output.write (line + "\n");
      AppBase.showAlert (AlertType.INFORMATION, "Success", "File Saved: " + file.getName ());
    }
    catch (IOException e)
    {
      AppBase.showAlert (AlertType.ERROR, "Error", "File Error: " + e.getMessage ());
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void showLinesSelected (LineDisplayStatus lineDisplayStatus)
  // ---------------------------------------------------------------------------------//
  {
    this.lineDisplayStatus = lineDisplayStatus;
    textFormatter.setShowLineNumbers (lineDisplayStatus.showLines);
    refresh ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (FilterStatus filterStatus)
  // ---------------------------------------------------------------------------------//
  {
    textFormatter.setFilter (filterStatus);
    refresh ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    dataFile = catalogEntry == null ? null : catalogEntry.getMember ();
    refresh ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void selectCodePage (String codePageName)
  // ---------------------------------------------------------------------------------//
  {
    refresh ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeNodeSelected (TreeNodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    if (nodeData.isPartitionedDataset ())
    {
      Dataset dataset = nodeData.getDataset ();
      String datasetName = dataset.getName ();
      if (!datasets.containsKey (datasetName))
        datasets.put (datasetName, (PdsDataset) dataset);
      dataFile = null;
    }
    else if (nodeData.isPhysicalSequentialDataset ())
      dataFile = nodeData.getDataFile ();
    else
      dataFile = null;

    refresh ();
  }
}
