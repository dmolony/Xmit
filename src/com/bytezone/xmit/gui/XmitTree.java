package com.bytezone.xmit.gui;

import java.io.File;
import java.util.Optional;
import java.util.prefs.Preferences;

import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class XmitTree extends TreeView<File>
{
  private static final String PREFS_LAST_PATH = "LastPath";
  private static String SEPARATOR = "/";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  //  private Path rootFolderPath;
  private final MultipleSelectionModel<TreeItem<File>> model = getSelectionModel ();

  XmitTree (FileTreeItem fileTreeItem)
  {
    super (fileTreeItem);

    fileTreeItem.setExpanded (true);

    setCellFactory (new Callback<TreeView<File>, TreeCell<File>> ()
    {
      @Override
      public TreeCell<File> call (TreeView<File> parm)
      {
        TreeCell<File> cell = new TreeCell<> ()
        {
          public void updateItem (File item, boolean empty)
          {
            super.updateItem (item, empty);
            if (empty || item == null)
            {
              setText (null);
              setGraphic (null);
            }
            else
            {
              setText (item.getName ());
              setGraphic (getTreeItem ().getGraphic ());
            }
          }
        };
        return cell;
      }
    });
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  public void exit ()
  {
    prefs.put (PREFS_LAST_PATH, getSelectedItemPath ());
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  public void restore ()
  {
    String lastPath = prefs.get (PREFS_LAST_PATH, "");

    if (!lastPath.isEmpty ())
    {
      Optional<TreeItem<File>> optionalNode = getNode (lastPath);
      if (optionalNode.isPresent ())
      {
        model.clearSelection ();
        model.select (optionalNode.get ());
        scrollTo (model.getSelectedIndex ());
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // getNode
  // ---------------------------------------------------------------------------------//

  Optional<TreeItem<File>> getNode (String path)
  {
    TreeItem<File> node = getRoot ();
    Optional<TreeItem<File>> optionalNode = Optional.empty ();

    String[] chunks = path.split (SEPARATOR);

    for (int i = 2; i < chunks.length; i++)
    {
      optionalNode = search (node, chunks[i]);
      if (!optionalNode.isPresent ())
        break;
      node = optionalNode.get ();
    }
    return optionalNode;
  }

  // ---------------------------------------------------------------------------------//
  // search
  // ---------------------------------------------------------------------------------//

  private Optional<TreeItem<File>> search (TreeItem<File> parentNode, String name)
  {
    parentNode.setExpanded (true);
    for (TreeItem<File> childNode : parentNode.getChildren ())
      if (childNode.getValue ().getName ().equals (name))
        return Optional.of (childNode);

    return Optional.empty ();
  }

  // ---------------------------------------------------------------------------------//
  // getSelectedItemPath
  // ---------------------------------------------------------------------------------//

  String getSelectedItemPath ()
  {
    StringBuilder pathBuilder = new StringBuilder ();

    TreeItem<File> item = model.getSelectedItem ();
    while (item != null)
    {
      pathBuilder.insert (0, SEPARATOR + item.getValue ().getName ());
      item = item.getParent ();
    }
    return pathBuilder.toString ();
  }
}
