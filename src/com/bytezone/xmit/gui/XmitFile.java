package com.bytezone.xmit.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class XmitFile extends File
{
  private static final List<String> xmitSuffixes = Arrays.asList ("xmi", "xmit");
  private static final List<String> compressionSuffixes = Arrays.asList ("zip");

  private final File file;
  private final String suffix;
  private String name;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public XmitFile (File file)                   // plain file
  {
    super (file.getAbsolutePath ());
    this.file = file;
    suffix = getSuffix (file.getName ());
  }

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public XmitFile (File file, String name)      // an extracted file
  {
    this (file);
    this.name = name;             // display this name instead of the tmp file name
  }

  // ---------------------------------------------------------------------------------//
  //isCompressed
  // ---------------------------------------------------------------------------------//

  public boolean isCompressed ()
  {
    return isCompressionSuffix (suffix);
  }

  // ---------------------------------------------------------------------------------//
  //getName
  // ---------------------------------------------------------------------------------//

  @Override
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
