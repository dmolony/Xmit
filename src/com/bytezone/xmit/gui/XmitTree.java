package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.Reader;

import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.util.Callback;

// -----------------------------------------------------------------------------------//
class XmitTree extends TreeView<XmitFile>                                             //
    implements FontChangeListener, SaveState
// -----------------------------------------------------------------------------------//
{
  private static final String PREFS_LAST_PATH = "LastPath";
  private static String SEPARATOR = "/";

  private final MultipleSelectionModel<TreeItem<XmitFile>> model = getSelectionModel ();
  private final List<TreeItemSelectionListener> listeners = new ArrayList<> ();

  private final Image zipImage;
  private final Image[] xImage = new Image[4];
  private final Image folderImage;
  private Font font;

  private final DatasetStatus datasetStatus = new DatasetStatus ();

  // ---------------------------------------------------------------------------------//
  XmitTree (FileTreeItem fileTreeItem)
  // ---------------------------------------------------------------------------------//
  {
    super (fileTreeItem);

    xImage[0] = new Image (getClass ().getResourceAsStream ("/icons/X-green-icon.png"));
    xImage[1] = new Image (getClass ().getResourceAsStream ("/icons/X-pink-icon.png"));
    xImage[2] = new Image (getClass ().getResourceAsStream ("/icons/X-blue-icon.png"));
    xImage[3] = new Image (getClass ().getResourceAsStream ("/icons/X-black-icon.png"));
    folderImage = new Image (getClass ().getResourceAsStream ("/icons/folder-icon.png"));
    zipImage = new Image (getClass ().getResourceAsStream ("/icons/zip-icon.png"));

    fileTreeItem.setExpanded (true);

    setCellFactory (new Callback<TreeView<XmitFile>, TreeCell<XmitFile>> ()
    {
      @Override
      public TreeCell<XmitFile> call (TreeView<XmitFile> parm)
      {
        TreeCell<XmitFile> cell = new TreeCell<> ()
        {
          private ImageView imageView;

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
              setImageView (xmitFile);
              setGraphic (imageView);
              setFont (font);
            }
          }

          private void setImageView (XmitFile xmitFile)
          {
            if (imageView == null)
              imageView = new ImageView ();

            Image image = xmitFile.isCompressed () ? zipImage : xmitFile.isDirectory ()
                ? folderImage : xImage[xmitFile.getLevel () % 4];
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
        System.out.println ("Never happens");
        notifyListeners (null, null);
        return;
      }

      XmitFile xmitFile = newSel.getValue ();
      Reader reader = xmitFile.getReader ((FileTreeItem) newSel);

      if (reader == null)
        notifyListeners (null, null);
      else
        notifyListeners (reader.getActiveDataset (), xmitFile.getName ());
    });
  }

  // ---------------------------------------------------------------------------------//
  private void notifyListeners (Dataset dataset, String name)
  // ---------------------------------------------------------------------------------//
  {
    datasetStatus.datasetSelected (dataset, name);
    for (TreeItemSelectionListener listener : listeners)
      listener.treeItemSelected (datasetStatus);
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
      Optional<TreeItem<XmitFile>> optionalNode = getNode (lastPath);
      if (optionalNode.isPresent ())
      {
        int row = getRow (optionalNode.get ());
        model.select (row);
        scrollTo (model.getSelectedIndex ());
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  public void setRootFolder (FileTreeItem fileTreeItem)
  // ---------------------------------------------------------------------------------//
  {
    setRoot (fileTreeItem);
    fileTreeItem.setExpanded (true);
  }

  // ---------------------------------------------------------------------------------//
  Optional<TreeItem<XmitFile>> getNode (String path)
  // ---------------------------------------------------------------------------------//
  {
    TreeItem<XmitFile> node = getRoot ();
    Optional<TreeItem<XmitFile>> optionalNode = Optional.empty ();

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
  private Optional<TreeItem<XmitFile>> search (TreeItem<XmitFile> parentNode, String name)
  // ---------------------------------------------------------------------------------//
  {
    parentNode.setExpanded (true);
    for (TreeItem<XmitFile> childNode : parentNode.getChildren ())
      if (childNode.getValue ().getName ().equals (name))
        return Optional.of (childNode);

    return Optional.empty ();
  }

  // ---------------------------------------------------------------------------------//
  String getSelectedItemPath ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder pathBuilder = new StringBuilder ();

    TreeItem<XmitFile> item = model.getSelectedItem ();
    while (item != null)
    {
      pathBuilder.insert (0, SEPARATOR + item.getValue ().getName ());
      item = item.getParent ();
    }

    return pathBuilder.toString ();
  }

  // ---------------------------------------------------------------------------------//
  //
  // ---------------------------------------------------------------------------------//

  //  private void findNode (TreeItem<XmitFile> treeNode, String name)
  //  {
  //    if (treeNode.getChildren ().isEmpty ())
  //    {
  //      // Do nothing node is empty.
  //    }
  //    else
  //    {
  //      // Loop through each child node.
  //      for (TreeItem<XmitFile> node : treeNode.getChildren ())
  //      {
  //        node.setExpanded (true);
  //        if (node.getValue ().getName ().equals (name))
  //          model.select (node);
  //
  //        // If the current node has children then check them.
  //        if (!treeNode.getChildren ().isEmpty ())
  //          findNode (node, name);
  //      }
  //    }
  //  }

  // ---------------------------------------------------------------------------------//
  public void addListener (TreeItemSelectionListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  public void removeListener (TreeItemSelectionListener listener)
  // ---------------------------------------------------------------------------------//
  {
    listeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    this.font = font;
    refresh ();
  }
}
