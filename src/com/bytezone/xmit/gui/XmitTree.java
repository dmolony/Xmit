package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

import com.bytezone.xmit.Reader;

import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class XmitTree extends TreeView<XmitFile>
{
  private static final String PREFS_LAST_PATH = "LastPath";
  private static String SEPARATOR = "/";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final MultipleSelectionModel<TreeItem<XmitFile>> model = getSelectionModel ();
  private final FocusModel<TreeItem<XmitFile>> focusModel = getFocusModel ();
  private final List<TreeItemSelectionListener> listeners = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  XmitTree (FileTreeItem fileTreeItem)
  {
    super (fileTreeItem);

    setStyle ("-fx-font-size: 13; -fx-font-family: monospaced");

    fileTreeItem.setExpanded (true);

    setCellFactory (new Callback<TreeView<XmitFile>, TreeCell<XmitFile>> ()
    {
      @Override
      public TreeCell<XmitFile> call (TreeView<XmitFile> parm)
      {
        TreeCell<XmitFile> cell = new TreeCell<> ()
        {
          public void updateItem (XmitFile xmitFile, boolean empty)
          {
            super.updateItem (xmitFile, empty);
            if (empty || xmitFile == null)
            {
              setText (null);
              setGraphic (null);
            }
            else
            {
              setText (xmitFile.getName ());
              setGraphic (getTreeItem ().getGraphic ());
            }
          }
        };
        return cell;
      }
    });

    model.selectedItemProperty ().addListener ( (obs, oldSelection, newSelection) ->
    {
      if (newSelection == null)
      {
        for (TreeItemSelectionListener listener : listeners)
          listener.treeItemSelected (null, null, null);
        return;
      }

      XmitFile xmitFile = newSelection.getValue ();
      Reader reader = xmitFile.getReader ((FileTreeItem) newSelection);

      if (reader == null)
        for (TreeItemSelectionListener listener : listeners)
          listener.treeItemSelected (null, null, null);
      else
        for (TreeItemSelectionListener listener : listeners)
          listener.treeItemSelected (reader, reader.getActiveDataset (),
              newSelection.getValue ().getName ());
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
      Optional<FileTreeItem> optionalNode = getNode (lastPath);
      if (optionalNode.isPresent ())
      {
        model.select (optionalNode.get ());
        scrollTo (model.getSelectedIndex ());
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // setRootFolder
  // ---------------------------------------------------------------------------------//

  public void setRootFolder (FileTreeItem fileTreeItem)
  {
    setRoot (fileTreeItem);
    fileTreeItem.setExpanded (true);
  }

  // ---------------------------------------------------------------------------------//
  // getNode
  // ---------------------------------------------------------------------------------//

  Optional<FileTreeItem> getNode (String path)
  {
    FileTreeItem node = (FileTreeItem) getRoot ();
    Optional<FileTreeItem> optionalNode = Optional.empty ();

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

  private Optional<FileTreeItem> search (FileTreeItem parentNode, String name)
  {
    parentNode.setExpanded (true);

    for (TreeItem<XmitFile> childNode : parentNode.getChildren ())
      if (childNode.getValue ().getName ().equals (name))
        return Optional.of ((FileTreeItem) childNode);

    return Optional.empty ();
  }

  // ---------------------------------------------------------------------------------//
  // getSelectedItemPath
  // ---------------------------------------------------------------------------------//

  String getSelectedItemPath ()
  {
    StringBuilder pathBuilder = new StringBuilder ();

    FileTreeItem item = (FileTreeItem) model.getSelectedItem ();
    while (item != null)
    {
      pathBuilder.insert (0, SEPARATOR + item.getValue ().getName ());
      item = (FileTreeItem) item.getParent ();
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
