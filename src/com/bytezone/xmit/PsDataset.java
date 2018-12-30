package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.Dsorg.Org;

public class PsDataset extends Dataset
{
  private final List<String> lines = new ArrayList<> ();        // sequential file

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  PsDataset (Org org, int lrecl)
  {
    super (org, lrecl);
  }

  // ---------------------------------------------------------------------------------//
  // processPS
  // ---------------------------------------------------------------------------------//

  void process ()
  {
    int max = blockPointerLists.size ();
    //    if (max > 300)
    //    {
    //      lines.add (String.format ("File contains %,d BlockPointerLists", max));
    //      max = 5;
    //    }

    for (int i = 0; i < max; i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      byte[] buffer = bpl.getRawBuffer ();
      if (lrecl == 0)
        lines.add (Utility.getHexDump (buffer));
      else
      {
        int ptr = 0;
        while (ptr < buffer.length)
        {
          int len = Math.min (lrecl, buffer.length - ptr);
          lines.add (Utility.getString (buffer, ptr, len).stripTrailing ());
          ptr += len;
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // getLines
  // ---------------------------------------------------------------------------------//

  // this should be converted to an abstract File which Member would also use
  // only OutputPane uses this
  public String getLines ()
  {
    //    lines.clear ();
    //    Dataset dataset = datasets.get (datasets.size () - 1);
    //    for (Dataset dataset : datasets)
    //      if (dataset.org == Org.PS)
    //      {
    //        processPS (dataset);
    //        break;
    //      }
    //    if (currentDataset.org == Org.PS)
    //    processPS ();

    StringBuilder text = new StringBuilder ();
    for (String line : lines)
      text.append (line + "\n");
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }
}
