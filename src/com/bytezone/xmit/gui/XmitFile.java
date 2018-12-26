package com.bytezone.xmit.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Reader;

public class XmitFile
{
  private static final List<String> xmitSuffixes = Arrays.asList ("xmi", "xmit");
  private static final List<String> compressionSuffixes = Arrays.asList ("zip");

  private final File file;
  private final String suffix;
  private String name;
  private Reader reader;
  //  private Reader parentReader;
  private CatalogEntry catalogEntry;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public XmitFile (File file)                   // plain file
  {
    this.file = file;
    suffix = getSuffix (file.getName ());
  }

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public XmitFile (File file, String name)      // an unzipped file
  {
    this (file);
    this.name = name;             // display this name instead of the tmp file name
  }

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public XmitFile (CatalogEntry catalogEntry)      // an xmit member
  {
    suffix = "";
    file = null;
    this.catalogEntry = catalogEntry;
    name = catalogEntry.getMemberName ();
  }

  // ---------------------------------------------------------------------------------//
  // isFile
  // ---------------------------------------------------------------------------------//

  public boolean isFile ()
  {
    return file == null ? false : file.isFile ();
  }

  // ---------------------------------------------------------------------------------//
  // isDirectory
  // ---------------------------------------------------------------------------------//

  public boolean isDirectory ()
  {
    return file == null ? false : file.isDirectory ();
  }

  // ---------------------------------------------------------------------------------//
  // isMember
  // ---------------------------------------------------------------------------------//

  public boolean isMember ()
  {
    return catalogEntry != null;
  }

  // ---------------------------------------------------------------------------------//
  // listFiles
  // ---------------------------------------------------------------------------------//

  public File[] listFiles ()
  {
    if (file != null)
      return file.listFiles ();
    return new File[0];
  }

  // ---------------------------------------------------------------------------------//
  // getAbsolutePath
  // ---------------------------------------------------------------------------------//

  String getAbsolutePath ()
  {
    if (file != null)
      return file.getAbsolutePath ();
    return reader.getFileName ();   // should be parent.getAbsolutePath()+"."+memberName
  }

  // ---------------------------------------------------------------------------------//
  // toPath
  // ---------------------------------------------------------------------------------//

  Path toPath ()
  {
    return file.toPath ();
  }

  // ---------------------------------------------------------------------------------//
  //isCompressed
  // ---------------------------------------------------------------------------------//

  public boolean isCompressed ()
  {
    return isCompressionSuffix (suffix);
  }

  // ---------------------------------------------------------------------------------//
  // setReader
  // ---------------------------------------------------------------------------------//

  void setReader (Reader reader)
  {
    this.reader = reader;
  }

  // ---------------------------------------------------------------------------------//
  // getReader
  // ---------------------------------------------------------------------------------//

  Reader getReader (FileTreeItem fileTreeItem)
  {
    Reader reader = getReader ();
    if (reader != null && reader.getXmitFiles ().size () > 0)
      fileTreeItem.buildChildren ();
    return reader;
  }

  // ---------------------------------------------------------------------------------//
  // getReader
  // ---------------------------------------------------------------------------------//

  Reader getReader ()
  {
    if (reader == null && catalogEntry != null)
    {
      reader = new Reader (catalogEntry.getXmitBuffer ());
    }
    if (reader == null && isFile () && !isCompressed ())
      try
      {
        //        System.out.println ("reading");
        reader = new Reader (Files.readAllBytes (file.toPath ()));
      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }
    return reader;
  }

  // ---------------------------------------------------------------------------------//
  // hasReader
  // ---------------------------------------------------------------------------------//

  boolean hasReader ()
  {
    return reader != null;
  }

  // ---------------------------------------------------------------------------------//
  //getName
  // ---------------------------------------------------------------------------------//

  //  @Override
  public String getName ()
  {
    return name == null ? file.getName () : name;
  }

  // ---------------------------------------------------------------------------------//
  // isValidFileName
  // ---------------------------------------------------------------------------------//

  public static boolean isValidFileName (String fileName)
  {
    if (fileName.startsWith ("__MACOSX/._"))      // no idea what this is
      return false;

    String suffix = getSuffix (fileName);
    return isXmitSuffix (suffix) || isCompressionSuffix (suffix);
  }

  // ---------------------------------------------------------------------------------//
  // getSuffix
  // ---------------------------------------------------------------------------------//

  public static String getSuffix (String filename)
  {
    int dotPos = filename.lastIndexOf ('.');
    return dotPos > 0 ? filename.substring (dotPos + 1).toLowerCase () : "";
  }

  // ---------------------------------------------------------------------------------//
  // isXmitSuffix
  // ---------------------------------------------------------------------------------//

  public static boolean isXmitSuffix (String suffix)
  {
    return xmitSuffixes.contains (suffix);
  }

  // ---------------------------------------------------------------------------------//
  // isCompressionSuffix
  // ---------------------------------------------------------------------------------//

  public static boolean isCompressionSuffix (String suffix)
  {
    return compressionSuffixes.contains (suffix);
  }

  // ---------------------------------------------------------------------------------//
  // decompressZip
  // ---------------------------------------------------------------------------------//

  public static Map<ZipEntry, XmitFile> decompressZip (Path path)
  {
    Map<ZipEntry, XmitFile> fileMap = new HashMap<> ();

    try (ZipFile zipFile = new ZipFile (path.toString ()))
    {
      Enumeration<? extends ZipEntry> entries = zipFile.entries ();
      while (entries.hasMoreElements ())
      {
        ZipEntry entry = entries.nextElement ();
        String entryName = entry.getName ();
        if (entryName.endsWith ("/"))
        {
          System.out.println ("folder");
        }
        else if (XmitFile.isValidFileName (entryName))
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
          tmp.deleteOnExit ();
          fileMap.put (entry, new XmitFile (tmp, entryName));
        }
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