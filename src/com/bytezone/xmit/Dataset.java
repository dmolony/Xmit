package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;

public abstract class Dataset
{
  final List<BlockPointerList> blockPointerLists = new ArrayList<> ();
  final List<String> lines = new ArrayList<> ();
  int lrecl;
  String name;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  Dataset (String name)
  {
    this.name = name;
  }

  // ---------------------------------------------------------------------------------//
  //
  // ---------------------------------------------------------------------------------//

  abstract byte[] getDataBuffer ();

  abstract String getLines (boolean showLines);

  // ---------------------------------------------------------------------------------//
  // createDataLines
  // ---------------------------------------------------------------------------------//

  void createDataLines (byte[] buffer)
  {
    int ptr = 0;
    int length = buffer.length;
    while (length > 0)
    {
      int len = Math.min (lrecl, length);
      lines.add (Utility.getString (buffer, ptr, len).stripTrailing ());
      ptr += len;
      length -= len;
    }
  }

  // ---------------------------------------------------------------------------------//
  // hexDump
  // ---------------------------------------------------------------------------------//

  void hexDump ()
  {
    if (blockPointerLists.size () == 0)
      return;

    if (blockPointerLists.get (0).isXmit ())
      lines.add ("Appears to be XMIT");

    // FILE600.XMI
    byte[] buffer = getDataBuffer ();
    lines.add (Utility.getHexDump (buffer));
  }

  // ---------------------------------------------------------------------------------//
  // isRdw
  // ---------------------------------------------------------------------------------//

  boolean isRdw ()
  {
    if (blockPointerLists.size () == 0)
      return false;

    for (BlockPointerList bpl : blockPointerLists)
    {
      byte[] buffer = bpl.getDataBuffer ();
      if (buffer.length == 0)
        continue;

      int len = Utility.getTwoBytes (buffer, 0);
      if (len != buffer.length)
        return false;
    }

    return true;
  }

  // ---------------------------------------------------------------------------------//
  // rdw
  // ---------------------------------------------------------------------------------//

  void rdw ()
  {
    byte[] buffer = getDataBuffer ();
    int ptr = 4;
    while (ptr < buffer.length)
    {
      int len = Utility.getTwoBytes (buffer, ptr);

      String line = Utility.getString (buffer, ptr + 4, len - 4);
      lines.add (String.format ("%s%n", line));
      ptr += len;
    }
  }

  // ---------------------------------------------------------------------------------//
  // isXmit
  // ---------------------------------------------------------------------------------//

  boolean isXmit ()
  {
    return blockPointerLists.size () > 0 && blockPointerLists.get (0).isXmit ();
  }

  // ---------------------------------------------------------------------------------//
  // xmitList
  // ---------------------------------------------------------------------------------//

  void xmitList ()
  {
    byte[] xmitBuffer = getDataBuffer ();
    try
    {
      Reader reader = new Reader (xmitBuffer);
      for (ControlRecord controlRecord : reader.getControlRecords ())
        lines.add (String.format ("%s", controlRecord));

      if (reader.getOrg () == Dsorg.Org.PDS)
      {
        lines.add (String.format ("Members: %s%n", reader.getCatalogEntries ().size ()));
        lines.add (" Member     User      Size  Offset     Date        Time     Alias");
        lines.add ("--------  --------  ------  ------  -----------  --------  --------");
        for (CatalogEntry catalogEntry : reader.getCatalogEntries ())
          lines.add (catalogEntry.toString ());
      }
    }
    catch (Exception e)
    {
      lines.add ("Data length: " + xmitBuffer.length);
      lines.add (e.getMessage ());
      //      lines.add ("\n\n");
      lines.add (Utility.getHexDump (xmitBuffer));
    }
  }

  // ---------------------------------------------------------------------------------//
  // partialDump
  // ---------------------------------------------------------------------------------//

  void partialDump (int max)
  {
    lines.add (toString ());
    lines.add ("");
    lines.add ("Data too large to display");
    lines.add ("");
    lines.add ("Showing first " + max + " of " + blockPointerLists.size () + " buffers");
    lines.add ("");

    if (blockPointerLists.get (0).isXmit ())
      lines.add ("Appears to be XMIT");

    for (int i = 0; i < max; i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (bpl.getDataLength () > 0)
        createDataLines (bpl.getDataBuffer ());
    }
  }
}
