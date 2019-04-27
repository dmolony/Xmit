package com.bytezone.xmit.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.xmit.PdsMember;
import com.bytezone.xmit.Utility;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;

class OutputTab extends XmitTextTab
    implements ShowLinesListener, TreeItemSelectionListener, TableItemSelectionListener,
    FilterChangeListener, OutputWriter
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
  DatasetStatus datasetStatus;

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
    if (datasetStatus.dataFile == null)
      return new ArrayList<> ();

    return getLines (MAX_LINES);
  }

  //----------------------------------------------------------------------------------- //
  List<String> getLines (int maxLines)
  //----------------------------------------------------------------------------------- //
  {
    List<String> newLines = new ArrayList<> ();

    List<String> lines = datasetStatus.dataFile.getLines ();              // improve this
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
    Optional<PdsMember> optMember = datasetStatus.findMember (datasetName, memberName);
    if (optMember.isEmpty ())
      newLines.add (
          String.format ("==> %s(%s): dataset not seen yet", datasetName, memberName));
    else
      for (String line : optMember.get ().getLines ())
        if (!line.startsWith (commentIndicator))
          newLines.add (line);
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
  public void treeItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
    this.datasetStatus = datasetStatus;
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void tableItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
  }
}
