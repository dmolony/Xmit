package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.FlatFile;
import com.bytezone.xmit.PsDataset;
import com.bytezone.xmit.Reader;
import com.bytezone.xmit.gui.XmitTree.TreeNodeListener;

import javafx.scene.input.KeyCode;

// -----------------------------------------------------------------------------------//
class CommentsTab extends XmitTextTab implements TreeNodeListener
// -----------------------------------------------------------------------------------//
{
  TreeNodeData nodeData;

  // ---------------------------------------------------------------------------------//
  public CommentsTab (String title, KeyCode keyCode)
  // ---------------------------------------------------------------------------------//
  {
    super (title, keyCode);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  List<String> getLines ()
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = new ArrayList<> ();

    if (nodeData == null || !nodeData.isDataset ())
      return lines;

    Reader reader = nodeData.getReader ();

    if (reader.size () > 1)
    {
      Dataset firstDataset = reader.getDataset (0);
      if (firstDataset.isPhysicalSequential ())
      {
        FlatFile file = ((PsDataset) firstDataset).getFlatFile ();
        for (String s : file.getLines ())
          lines.add (s);
        lines.add ("");
      }
      else
        lines.add (
            "Unexpected disposition for file #1: " + firstDataset.getDisposition ());
    }
    else
      lines.add ("No comments");

    return lines;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeNodeSelected (TreeNodeData nodeData)
  // ---------------------------------------------------------------------------------//
  {
    this.nodeData = nodeData;
    refresh ();
  }
}
