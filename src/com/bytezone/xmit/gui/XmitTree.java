package com.bytezone.xmit.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.bytezone.xmit.Reader;

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
  //  private Path rootFolderPath;
  private final MultipleSelectionModel<TreeItem<XmitFile>> model = getSelectionModel ();
  private final List<TreeItemSelectionListener> listeners = new ArrayList<> ();

  private Reader reader;
  private final Map<String, Reader> readers = new HashMap<> ();

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

    // selection
    model.selectedItemProperty ().addListener ( (obs, oldSelection, newSelection) ->
    {
      if (newSelection == null)
      {
        for (TreeItemSelectionListener listener : listeners)
          listener.treeItemSelected (null);
        return;
      }

      File file = newSelection.getValue ();
      String suffix = getSuffix (file.getName ());

      if (!file.isFile () || isCompressionSuffix (suffix))
      {
        for (TreeItemSelectionListener listener : listeners)
          listener.treeItemSelected (null);
        return;
      }

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
      Optional<TreeItem<XmitFile>> optionalNode = getNode (lastPath);
      if (optionalNode.isPresent ())
      {
        model.clearSelection ();
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

  Optional<TreeItem<XmitFile>> getNode (String path)
  {
    TreeItem<XmitFile> node = getRoot ();
    Optional<TreeItem<XmitFile>> optionalNode = Optional.empty ();

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

  private Optional<TreeItem<XmitFile>> search (TreeItem<XmitFile> parentNode, String name)
  {
    parentNode.setExpanded (true);
    for (TreeItem<XmitFile> childNode : parentNode.getChildren ())
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

    TreeItem<XmitFile> item = model.getSelectedItem ();
    while (item != null)
    {
      pathBuilder.insert (0, SEPARATOR + item.getValue ().getName ());
      item = item.getParent ();
    }
    return pathBuilder.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // decompressZip
  // ---------------------------------------------------------------------------------//

  public static Map<ZipEntry, XmitFile> decompressZip (Path path)
  {
    String fileName = path.toString ();
    Map<ZipEntry, XmitFile> fileList = new HashMap<> ();
    try (ZipFile zipFile = new ZipFile (fileName))
    {
      Enumeration<? extends ZipEntry> entries = zipFile.entries ();
      File tmp = null;

      while (entries.hasMoreElements ())
      {
        ZipEntry entry = entries.nextElement ();
        String entryName = entry.getName ();
        if (entryName.endsWith ("/"))
        {
          System.out.println ("folder");
        }
        else if (isValidFileName (entryName))
        {
          int pos = entryName.lastIndexOf ('.');
          String suffix = pos < 0 ? "" : entryName.substring (pos).toLowerCase ();
          InputStream stream = zipFile.getInputStream (entry);
          tmp = File.createTempFile (entry.getName (), suffix);

          FileOutputStream fos = new FileOutputStream (tmp);

          int bytesRead;
          byte[] buffer = new byte[1024];
          while ((bytesRead = stream.read (buffer)) > 0)
            fos.write (buffer, 0, bytesRead);

          stream.close ();
          fos.close ();
          tmp.deleteOnExit ();
          fileList.put (entry, new XmitFile (tmp, entry.getName ()));
        }
      }
    }
    catch (IOException e)
    {
      System.out.println (e);
      System.err.println (e.getMessage ());
    }

    return fileList;
  }

  // ---------------------------------------------------------------------------------//
  // helpers
  // ---------------------------------------------------------------------------------//

  private static final List<String> xmitSuffixes = Arrays.asList ("xmi", "xmit");
  private static final List<String> compressionSuffixes = Arrays.asList ("zip");

  public static boolean isValidFileName (String fileName)
  {
    if (fileName.startsWith ("__MACOSX/._"))      // no idea what this is
      return false;

    String suffix = getSuffix (fileName);
    return isXmitSuffix (suffix) || isCompressionSuffix (suffix);
  }

  public static String getSuffix (String filename)
  {
    int dotPos = filename.lastIndexOf ('.');
    return dotPos > 0 ? filename.substring (dotPos + 1).toLowerCase () : "";
  }

  public static boolean isXmitSuffix (String suffix)
  {
    return xmitSuffixes.contains (suffix);
  }

  public static boolean isCompressionSuffix (String suffix)
  {
    return compressionSuffixes.contains (suffix);
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
