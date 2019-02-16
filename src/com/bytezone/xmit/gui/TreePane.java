package com.bytezone.xmit.gui;

import javafx.scene.control.Label;

// ---------------------------------------------------------------------------------//
class TreePane extends HeaderPane
//---------------------------------------------------------------------------------//
{
  private final Label lblSaveFolder = new Label ();
  private final String home = System.getProperty ("user.home");
  private final XmitTree tree;

  // ---------------------------------------------------------------------------------//
  public TreePane (XmitTree tree)
  // ---------------------------------------------------------------------------------//
  {
    this.tree = tree;
    setCenter (tree);
    setTop (getHBox (lblSaveFolder));
    setFolderName ();
  }

  // ---------------------------------------------------------------------------------//
  void setRootFolder (FileTreeItem treeItem)
  // ---------------------------------------------------------------------------------//
  {
    tree.setRootFolder (treeItem);
    setFolderName ();
  }

  // ---------------------------------------------------------------------------------//
  void setFolderName ()
  // ---------------------------------------------------------------------------------//
  {
    String pathName = tree.getRoot ().getValue ().toPath ().toString ();
    if (pathName.startsWith (home))
      pathName = pathName.replace (home, "~");
    lblSaveFolder.setText (pathName);
  }
}
