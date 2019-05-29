package com.bytezone.xmit.gui;

import javafx.scene.layout.BorderPane;

// -----------------------------------------------------------------------------------//
class TreePane extends BorderPane
// -----------------------------------------------------------------------------------//
{
  private final String home = System.getProperty ("user.home");
  private final XmitTreeV1 tree;
  private final HeaderBar treeHeaderBar = new HeaderBar ();

  // ---------------------------------------------------------------------------------//
  public TreePane (XmitTreeV1 tree)
  // ---------------------------------------------------------------------------------//
  {
    this.tree = tree;
    setCenter (tree);
    setTop (treeHeaderBar);
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
    treeHeaderBar.leftLabel.setText (pathName);
  }
}
