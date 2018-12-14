package com.bytezone.xmit.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;

import com.bytezone.xmit.Reader;

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
  private final List<TreeItemSelectionListener> listeners = new ArrayList<> ();

  private Reader reader;
  private final Map<String, Reader> readers = new HashMap<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

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

    // selection
    model.selectedItemProperty ().addListener ( (obs, oldSelection, newSelection) ->
    {
      if (newSelection == null)
        return;

      File file = newSelection.getValue ();
      String key = file.getAbsolutePath ();
      if (readers.containsKey (key))
        reader = readers.get (key);
      else
      {
        try
        {
          reader = new Reader (Files.readAllBytes (file.toPath ()));
        }
        catch (IOException e)
        {
          e.printStackTrace ();
        }
        readers.put (key, reader);
      }

      for (TreeItemSelectionListener listener : listeners)
        listener.treeItemSelected (reader);
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

  // ---------------------------------------------------------------------------------//
  // addListener
  // ---------------------------------------------------------------------------------//

  public void addListener (TreeItemSelectionListener listener)
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  // removeListener
  // ---------------------------------------------------------------------------------//

  public void removeListener (TreeItemSelectionListener listener)
  {
    listeners.remove (listener);
  }
}
