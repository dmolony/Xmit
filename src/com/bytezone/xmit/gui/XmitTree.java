package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.util.Callback;

public class XmitTree extends TreeView<NodeData> implements SaveState, FontChangeListener
{
  private static final String PREFS_LAST_PATH = "LastPath";
  private static String SEPARATOR = "/";
  static final boolean merging = true;

  private final Image zipImage =
      new Image (getClass ().getResourceAsStream ("/icons/zip-icon.png"));
  private final Image xImage =
      new Image (getClass ().getResourceAsStream ("/icons/X-green-icon.png"));
  private final Image mImage =
      new Image (getClass ().getResourceAsStream ("/icons/M-blue-icon.png"));
  private final Image dImage =
      new Image (getClass ().getResourceAsStream ("/icons/D-pink-icon.png"));
  private final Image tImage =
      new Image (getClass ().getResourceAsStream ("/icons/T-black-icon.png"));
  private final Image folderImage =
      new Image (getClass ().getResourceAsStream ("/icons/folder-icon.png"));
  private Font font;

  private final MultipleSelectionModel<TreeItem<NodeData>> model = getSelectionModel ();
  private final List<NodeDataListener> listeners = new ArrayList<> ();

  public XmitTree (TreeItem<NodeData> root)
  {
    super (root);

    setCellFactory (new Callback<TreeView<NodeData>, TreeCell<NodeData>> ()
    {
      @Override
      public TreeCell<NodeData> call (TreeView<NodeData> parm)
      {
        TreeCell<NodeData> cell = new TreeCell<> ()
        {
          private final ImageView imageView = new ImageView ();

          public void updateItem (NodeData nodeData, boolean empty)
          {
            super.updateItem (nodeData, empty);
            if (empty || nodeData == null)
            {
              setText (null);
              setGraphic (null);
            }
            else
            {
              setText (nodeData.getName ());
              setImageView (nodeData);
              setGraphic (imageView);
              setFont (font);
            }
          }

          private void setImageView (NodeData nodeData)
          {
            Image image = nodeData.isCompressedFile () ? zipImage :         //
            nodeData.isDirectory () ? folderImage :                         //
            nodeData.isMember () ? mImage :                                 //
            nodeData.isFile () ?                                            //
            nodeData.getName ().endsWith (".aws") ? tImage : xImage :       //
            nodeData.isDataset () ? dImage :                                //
            null;

            imageView.setImage (image);
          }
        };

        return cell;
      }
    });

    model.selectedItemProperty ().addListener ( (obs, oldSel, newSel) ->
    {
      if (newSel == null)
      {
        System.out.println ("Should never happen - newSel is null");
        return;
      }

      NodeData nodeData = newSel.getValue ();
      XmitTreeItem treeItem = (XmitTreeItem) newSel;

      int datasets = nodeData.size ();    // forces the reader to exist (if possible)

      if (merging && nodeData.isDatasetContainer () && nodeData.isXmit ())
        nodeData.merge ();

      if (nodeData.isPartitionedDataset () && nodeData.getPdsXmitMembers ().size () > 0)
        treeItem.setLeaf (false);                 // show the open triangle

      //      if (!merging && nodeData.isXmit () && datasets > 1)
      //        treeItem.setLeaf (false);       // show the open triangle

      for (NodeDataListener listener : listeners)
        listener.treeNodeSelected (nodeData);
    });
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void save (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    prefs.put (PREFS_LAST_PATH, getSelectedItemPath ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    String lastPath = prefs.get (PREFS_LAST_PATH, "");

    if (!lastPath.isEmpty ())
    {
      Optional<TreeItem<NodeData>> optionalNode = getNode (lastPath);
      if (optionalNode.isPresent ())
      {
        int row = getRow (optionalNode.get ());
        model.select (row);
        scrollTo (model.getSelectedIndex ());
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  Optional<TreeItem<NodeData>> getNode (String path)
  // ---------------------------------------------------------------------------------//
  {
    TreeItem<NodeData> node = getRoot ();
    Optional<TreeItem<NodeData>> optionalNode = Optional.empty ();

    String[] chunks = path.split (SEPARATOR);

    for (int i = 2; i < chunks.length; i++)
    {
      model.select (node);
      optionalNode = search (node, chunks[i]);
      if (!optionalNode.isPresent ())
        break;
      node = optionalNode.get ();
    }

    setShowRoot (false);        // workaround for stupid javafx bug
    return optionalNode;
  }

  // ---------------------------------------------------------------------------------//
  private Optional<TreeItem<NodeData>> search (TreeItem<NodeData> parentNode, String name)
  // ---------------------------------------------------------------------------------//
  {
    parentNode.setExpanded (true);

    for (TreeItem<NodeData> childNode : parentNode.getChildren ())
      if (childNode.getValue ().getName ().equals (name))
        return Optional.of (childNode);

    return Optional.empty ();
  }

  // ---------------------------------------------------------------------------------//
  String getSelectedItemPath ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder pathBuilder = new StringBuilder ();

    TreeItem<NodeData> item = model.getSelectedItem ();
    while (item != null)
    {
      pathBuilder.insert (0, SEPARATOR + item.getValue ().getName ());
      item = item.getParent ();
    }

    return pathBuilder.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public void addListener (NodeDataListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  public void setRootFolder (XmitTreeItem xmitTreeItem)
  // ---------------------------------------------------------------------------------//
  {
    setRoot (xmitTreeItem);
    xmitTreeItem.setExpanded (true);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    this.font = font;
    refresh ();
  }

  // ---------------------------------------------------------------------------------//
  interface NodeDataListener
  // ---------------------------------------------------------------------------------//
  {
    public void treeNodeSelected (NodeData nodeData);
  }
}
