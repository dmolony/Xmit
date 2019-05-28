package com.bytezone.xmit.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.bytezone.xmit.*;

import javafx.scene.control.Alert.AlertType;

// -----------------------------------------------------------------------------------//
class XmitFileNode
{
  private static final List<String> validSuffixes = Arrays.asList ("xmi", "xmit", "aws");
  private static final List<String> compressionSuffixes = Arrays.asList ("zip");

  private final File file;
  private final String suffix;
  private final String name;

  private Reader reader;                          // can contain multiple datasets
  private Dataset dataset;
  private DataFile dataFile;                      // PDS member 

  // ---------------------------------------------------------------------------------//
  public XmitFileNode (File file)                 // plain .xmi or .aws file
  // ---------------------------------------------------------------------------------//
  {
    this.file = file;
    name = file.getName ();
    suffix = getSuffix (name);
  }

  // ---------------------------------------------------------------------------------//
  public XmitFileNode (File file, String name)   // an unzipped .xmi or .aws file
  // ---------------------------------------------------------------------------------//
  {
    this.file = file;
    this.name = name;                 // display this name instead of the tmp file name
    suffix = getSuffix (name);
  }

  // ---------------------------------------------------------------------------------//
  public XmitFileNode (Dataset dataset)           // PDS or PS
  // ---------------------------------------------------------------------------------//
  {
    file = null;
    this.dataset = dataset;
    name = dataset.getName ();
    suffix = "";
  }

  // ---------------------------------------------------------------------------------//
  public XmitFileNode (PdsMember pdsMember)      // an xmit file from a PDS member
  // ---------------------------------------------------------------------------------//
  {
    file = null;
    name = pdsMember.getName ();
    suffix = "";
    dataFile = pdsMember;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isFile ()
  // ---------------------------------------------------------------------------------//
  {
    return file == null ? false : file.isFile ();
  }

  // ---------------------------------------------------------------------------------//
  public boolean isDirectory ()
  // ---------------------------------------------------------------------------------//
  {
    return file == null ? false : file.isDirectory ();
  }

  // ---------------------------------------------------------------------------------//
  public boolean isMember ()
  // ---------------------------------------------------------------------------------//
  {
    return dataFile != null;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isCompressed ()
  // ---------------------------------------------------------------------------------//
  {
    return isCompressionSuffix (suffix);
  }

  // ---------------------------------------------------------------------------------//
  boolean isLeaf ()
  // ---------------------------------------------------------------------------------//
  {
    return (isFile () && !isCompressed ()) || isMember ();
  }

  // ---------------------------------------------------------------------------------//
  int getLevel ()
  // ---------------------------------------------------------------------------------//
  {
    return dataFile == null ? 0 : dataFile.getLevel ();
  }

  // ---------------------------------------------------------------------------------//
  public File[] listFiles ()
  // ---------------------------------------------------------------------------------//
  {
    if (file != null)
      return file.listFiles ();
    return new File[0];
  }

  // ---------------------------------------------------------------------------------//
  public Path toPath ()
  // ---------------------------------------------------------------------------------//
  {
    return file.toPath ();
  }

  // ---------------------------------------------------------------------------------//
  void setReader (Reader reader)
  // ---------------------------------------------------------------------------------//
  {
    this.reader = reader;
  }

  // called from XmitTree
  // ---------------------------------------------------------------------------------//
  Reader getReader (FileTreeItem fileTreeItem)
  // ---------------------------------------------------------------------------------//
  {
    Reader reader = getReader ();
    if (reader != null)
    {
      Dataset dataset = reader.getActiveDataset ();
      if (dataset.isPartitionedDataset ()
          && ((PdsDataset) dataset).getPdsXmitMembers ().size () > 0)
        fileTreeItem.buildChildren ();
    }
    return reader;
  }

  // called from FileTreeNode
  // ---------------------------------------------------------------------------------//
  Reader getReader ()
  // ---------------------------------------------------------------------------------//
  {
    if (reader == null)
      if (isMember ())                            // xmit file contained in a PdsMember
        reader = new XmitReader (dataFile);
      else if (isFile () && !isCompressed ())
      {
        if (file.getName ().endsWith (".aws"))
          reader = new AwsTapeReader (file);
        else
          reader = new XmitReader (file);
      }

    return reader;
  }

  // ---------------------------------------------------------------------------------//
  public String getName ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isValidFileName (File file)
  // ---------------------------------------------------------------------------------//
  {
    return isValidFileName (file.getName ().toUpperCase ());
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isValidFileName (String fileName)
  // ---------------------------------------------------------------------------------//
  {
    if (fileName.startsWith ("__MACOSX/._"))      // no idea what this is
      return false;

    String suffix = getSuffix (fileName);
    return isValidSuffix (suffix) || isCompressionSuffix (suffix);
  }

  // ---------------------------------------------------------------------------------//
  public static String getSuffix (String filename)
  // ---------------------------------------------------------------------------------//
  {
    int dotPos = filename.lastIndexOf ('.');
    return dotPos > 0 ? filename.substring (dotPos + 1).toLowerCase () : "";
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isValidSuffix (String suffix)
  // ---------------------------------------------------------------------------------//
  {
    return validSuffixes.contains (suffix);
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isCompressionSuffix (String suffix)
  // ---------------------------------------------------------------------------------//
  {
    return compressionSuffixes.contains (suffix);
  }

  // ---------------------------------------------------------------------------------//
  public static Map<ZipEntry, XmitFileNode> decompressZip (Path path)
  // ---------------------------------------------------------------------------------//
  {
    Map<ZipEntry, XmitFileNode> fileMap = new HashMap<> ();

    try (ZipFile zipFile = new ZipFile (path.toString ()))
    {
      Enumeration<? extends ZipEntry> entries = zipFile.entries ();
      List<String> invalidNames = new ArrayList<> ();
      boolean containsFolder = false;
      while (entries.hasMoreElements ())
      {
        ZipEntry entry = entries.nextElement ();
        String entryName = entry.getName ();
        if (entryName.endsWith ("/"))
        {
          containsFolder = true;
        }
        else if (XmitFileNode.isValidFileName (entryName))
        {
          int pos = entryName.lastIndexOf ('.');
          String suffix = pos < 0 ? "" : entryName.substring (pos).toLowerCase ();
          InputStream stream = zipFile.getInputStream (entry);
          File tmp = File.createTempFile (entry.getName (), suffix);

          FileOutputStream fos = new FileOutputStream (tmp);

          int bytesRead;
          byte[] buffer = new byte[1024];
          while ((bytesRead = stream.read (buffer)) > 0)
            fos.write (buffer, 0, bytesRead);

          stream.close ();
          fos.close ();
          tmp.deleteOnExit ();          // why not delete it now?
          fileMap.put (entry, new XmitFileNode (tmp, entryName));
        }
        else
          invalidNames.add (entryName);
      }

      if (fileMap.isEmpty ())
      {
        int size = invalidNames.size ();
        String message = String.format ("Zip file contains %d file%s, but no .XMI files",
            size, size == 1 ? "" : "s");
        if (containsFolder)
          message += "\nFile contains unexamined subfolders";
        Utility.showAlert (AlertType.INFORMATION, "", message);
      }
    }
    catch (IOException e)
    {
      System.out.println (e);
      System.err.println (e.getMessage ());
    }

    return fileMap;
  }
}
