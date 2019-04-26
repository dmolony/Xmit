package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.xmit.*;

import javafx.scene.input.KeyCode;

class OutputTab extends XmitTab implements ShowLinesListener, TreeItemSelectionListener,
    TableItemSelectionListener, FilterChangeListener
{
  private static final int MAX_LINES = 2500;
  private static final String TRUNCATE_MESSAGE_1 =
      "*** Output truncated at %,d lines to improve rendering time ***";
  private static final String TRUNCATE_MESSAGE_2 =
      "***      To see the entire file, use File -> Save Output      ***";

  private static Pattern includePattern =
      Pattern.compile ("^//\\s+JCLLIB\\s+ORDER=\\((" + Utility.validName + ")\\)$");
  private static Pattern memberPattern = Pattern.compile (
      "^//(" + Utility.validPart + ")?\\s+INCLUDE\\s+MEMBER=(" + Utility.validPart + ")");
  //  private static Pattern dsnPattern = Pattern
  //      .compile ("DSN=(" + Utility.validName + ")\\((" + Utility.validPart + ")\\)");

  LineDisplayStatus lineDisplayStatus;
  Dataset dataset;                // usually file #1 in the Reader
  DataFile dataFile;              // FlatFile or PdsMember
  CatalogEntry catalogEntry;      // needed for alias members

  // keep track of all PDS datasets seen so that we can INCLUDE members
  final Map<String, PdsDataset> datasets = new TreeMap<> ();

  //----------------------------------------------------------------------------------- //
  public OutputTab (String title, KeyCode keyCode)
  //----------------------------------------------------------------------------------- //
  {
    super (title, keyCode);

    textFormatter = new TextFormatterJcl ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  List<String> getLines ()
  //----------------------------------------------------------------------------------- //
  {
    if (dataFile == null)
      return new ArrayList<> ();

    return getLines (MAX_LINES);
  }

  //----------------------------------------------------------------------------------- //
  List<String> getLines (int maxLines)
  //----------------------------------------------------------------------------------- //
  {
    List<String> newLines = new ArrayList<> ();

    List<String> lines = dataFile.getLines ();              // improve this
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
        line = strip (line);

      if (lineDisplayStatus.truncateLines && line.length () > 0)
        newLines.add (line.substring (1));
      else
        newLines.add (line);

      if (isJCL)
        includeDatasetName = checkInclude (includeDatasetName, line, newLines);
    }

    return newLines;
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
        append (newLines, includeDatasetName, m.group (2), "//*");
    }

    Matcher m = includePattern.matcher (line);
    if (m.find ())
      includeDatasetName = m.group (1);

    //    if (false)        // will expand output datasets too
    //    {
    //      m = dsnPattern.matcher (line);
    //      if (m.find ())
    //        append (newLines, m.group (1), m.group (2), "*");
    //    }

    return includeDatasetName;
  }

  //----------------------------------------------------------------------------------- //
  private void append (List<String> newLines, String datasetName, String memberName,
      String commentIndicator)
  //----------------------------------------------------------------------------------- //
  {
    List<String> lines = findMember (datasetName, memberName);
    if (lines.size () == 0)
    {
      newLines.add (
          String.format ("==> %s(%s): dataset not seen yet", datasetName, memberName));
      return;
    }

    for (String line : lines)
      if (!line.startsWith (commentIndicator))
        newLines.add (line);
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
  public void showLinesSelected (LineDisplayStatus lineDisplayStatus)
  //----------------------------------------------------------------------------------- //
  {
    this.lineDisplayStatus = lineDisplayStatus;
    textFormatter.setShowLines (lineDisplayStatus.showLines);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void setFilter (FilterStatus filterStatus)
  //----------------------------------------------------------------------------------- //
  {
    textFormatter.setFilter (filterStatus);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  //----------------------------------------------------------------------------------- //
  {
    this.dataset = dataset;

    if (dataset != null && dataset.isPds ())
    {
      String datasetName = dataset.getReader ().getFileName ();
      if (!datasets.containsKey (datasetName))
        datasets.put (datasetName, (PdsDataset) dataset);
    }
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
  }
}
