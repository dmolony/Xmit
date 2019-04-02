package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.PdsMember;
import com.bytezone.xmit.Utility;

import javafx.scene.input.KeyCode;

public class OutputTab extends XmitTab implements ShowLinesListener
{
  private static final int MAX_LINES = 2500;
  private static final String TRUNCATE_MESSAGE1 =
      "\n*** Output truncated at %,d lines to improve rendering time ***";
  private static final String TRUNCATE_MESSAGE2 =
      "\n***      To see the entire file, use File -> Save Output      ***";
  private static Pattern includePattern =
      Pattern.compile ("^//\\s+JCLLIB\\s+ORDER=\\((" + Utility.validName + ")\\)$");
  private static Pattern memberPattern =
      Pattern.compile ("INCLUDE\\s+MEMBER=(" + Utility.validPart + ")");
  private static Pattern dsnPattern = Pattern
      .compile ("DSN=(" + Utility.validName + ")\\((" + Utility.validPart + ")\\)");

  private boolean showLines;
  private boolean stripLines;
  private boolean truncateLines;
  private boolean expandInclude;

  //----------------------------------------------------------------------------------- //
  public OutputTab (OutputPane parent, String title, KeyCode keyCode)
  //----------------------------------------------------------------------------------- //
  {
    super (parent, title, keyCode);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  List<String> getLines ()
  //----------------------------------------------------------------------------------- //
  {
    if (parent.dataFile == null)
      return new ArrayList<> ();

    return getLines (MAX_LINES);
  }

  //----------------------------------------------------------------------------------- //
  List<String> getLines (int maxLines)
  //----------------------------------------------------------------------------------- //
  {
    List<String> newLines = new ArrayList<> ();

    List<String> lines = parent.dataFile.getLines ();
    int lineNo = 0;
    String includeDatasetName = "";

    boolean isJCL = expandInclude && Utility.isJCL (lines);

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
  private List<String> findMember (String datasetName, String memberName)
  //----------------------------------------------------------------------------------- //
  {
    if (parent.datasets.containsKey (datasetName))
    {
      PdsDataset dataset = parent.datasets.get (datasetName);
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
  public void showLinesSelected (boolean showLines, boolean stripLines,
      boolean truncateLines, boolean expandInclude)
  //----------------------------------------------------------------------------------- //
  {
    this.showLines = showLines;
    this.stripLines = stripLines;
    this.truncateLines = truncateLines;
    this.expandInclude = expandInclude;
  }
}
