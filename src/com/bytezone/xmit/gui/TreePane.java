package com.bytezone.xmit.gui;

import javafx.scene.control.Label;

// ---------------------------------------------------------------------------------//
// TreePane
// ---------------------------------------------------------------------------------//

class TreePane extends HeaderPane
{
  private final Label lblSaveFolder = new Label ();
  private final String home = System.getProperty ("user.home");
  private final XmitTree tree;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public TreePane (XmitTree tree)
  {
    this.tree = tree;
    setCenter (tree);
    setTop (getHBox (lblSaveFolder));
    setFolderName ();
  }

  // ---------------------------------------------------------------------------------//
  // setRootFolder
  // ---------------------------------------------------------------------------------//

  void setRootFolder (FileTreeItem treeItem)
  {
    tree.setRootFolder (treeItem);
    setFolderName ();
  }

  // ---------------------------------------------------------------------------------//
  // setFolderName
  // ---------------------------------------------------------------------------------//

  void setFolderName ()
  {
    String pathName = tree.getRoot ().getValue ().toPath ().toString ();
    if (pathName.startsWith (home))
      pathName = pathName.replace (home, "~");
    lblSaveFolder.setText (pathName);
  }
}
