package com.bytezone.xmit.gui;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.bytezone.xmit.*;

// -----------------------------------------------------------------------------------//
class TreeNodeData implements Iterable<Dataset>
// -----------------------------------------------------------------------------------//
{
  static final List<String> fileSuffixes = Arrays.asList ("xmi", "xmit", "aws");
  static final List<String> compressionSuffixes = Arrays.asList ("zip");

  private final String name;
  private final File file;
  private Dataset dataset;
  private final PdsMember member;
  private final String suffix;

  private Reader reader;
  private boolean merged;

  // ---------------------------------------------------------------------------------//
  TreeNodeData (File file)
  // ---------------------------------------------------------------------------------//
  {
    this.name = file.getName ();
    this.file = file;
    this.dataset = null;
    this.member = null;
    this.suffix = file.isDirectory () ? "" : getSuffix (name);
  }

  // ---------------------------------------------------------------------------------//
  TreeNodeData (File file, String displayName)              // decompressed temporary file
  // ---------------------------------------------------------------------------------//
  {
    this.name = displayName;
    this.file = file;
    this.dataset = null;
    this.member = null;
    this.suffix = getSuffix (file.getName ());

    assert isValidFileSuffix (suffix);
  }

  // ---------------------------------------------------------------------------------//
  TreeNodeData (Dataset dataset)
  // ---------------------------------------------------------------------------------//
  {
    this.name = dataset.getName ();
    this.file = null;
    this.dataset = dataset;
    this.member = null;
    this.suffix = "";

    this.reader = dataset.getReader ();
  }

  // ---------------------------------------------------------------------------------//
  TreeNodeData (PdsMember member)                       // a member containg an xmit file
  // ---------------------------------------------------------------------------------//
  {
    this.name = member.getName ();
    this.file = null;
    this.dataset = member.getDataset ();
    this.member = member;
    this.suffix = "";
  }

  // ---------------------------------------------------------------------------------//
  void merge ()
  // ---------------------------------------------------------------------------------//
  {
    dataset = reader.getDataset (reader.size () - 1);       // last dataset
    merged = true;
  }

  // ---------------------------------------------------------------------------------//
  String getName ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }

  // ---------------------------------------------------------------------------------//
  boolean isFile ()
  // ---------------------------------------------------------------------------------//
  {
    return file != null;
  }

  // ---------------------------------------------------------------------------------//
  File getFile ()
  // ---------------------------------------------------------------------------------//
  {
    return file;
  }

  // ---------------------------------------------------------------------------------//
  boolean isReadableFile ()
  // ---------------------------------------------------------------------------------//
  {
    return file != null && !file.isDirectory () && !isCompressedFile ();
  }

  // ---------------------------------------------------------------------------------//
  boolean isCompressedFile ()
  // ---------------------------------------------------------------------------------//
  {
    return file != null && isValidCompressionSuffix (suffix);
  }

  // ---------------------------------------------------------------------------------//
  boolean isDatasetContainer ()
  // ---------------------------------------------------------------------------------//
  {
    return isMember () || isReadableFile ();
  }

  // ---------------------------------------------------------------------------------//
  boolean isDirectory ()
  // ---------------------------------------------------------------------------------//
  {
    return file != null && file.isDirectory ();
  }

  // ---------------------------------------------------------------------------------//
  boolean isDataset ()
  // ---------------------------------------------------------------------------------//
  {
    return dataset != null;
  }

  // ---------------------------------------------------------------------------------//
  Dataset getDataset ()
  // ---------------------------------------------------------------------------------//
  {
    return dataset;
  }

  // ---------------------------------------------------------------------------------//
  String getDatasetName ()
  // ---------------------------------------------------------------------------------//
  {
    return dataset.getName ();
  }

  // ---------------------------------------------------------------------------------//
  boolean isPartitionedDataset ()
  // ---------------------------------------------------------------------------------//
  {
    return dataset != null && dataset.isPartitionedDataset ();
  }

  // ---------------------------------------------------------------------------------//
  boolean isPhysicalSequentialDataset ()
  // ---------------------------------------------------------------------------------//
  {
    return dataset != null && dataset.isPhysicalSequential ();
  }

  // ---------------------------------------------------------------------------------//
  Disposition getDisposition ()
  // ---------------------------------------------------------------------------------//
  {
    return isDataset () ? dataset.getDisposition () : null;
  }

  // ---------------------------------------------------------------------------------//
  boolean isMember ()
  // ---------------------------------------------------------------------------------//
  {
    return member != null;
  }

  // ---------------------------------------------------------------------------------//
  boolean isDataFile ()
  // ---------------------------------------------------------------------------------//
  {
    return isPartitionedDataset () || isPhysicalSequentialDataset ();
  }

  // ---------------------------------------------------------------------------------//
  DataFile getDataFile ()
  // ---------------------------------------------------------------------------------//
  {
    if (isPhysicalSequentialDataset ())                 // test this first
      return ((PsDataset) dataset).getFlatFile ();
    if (isMember ())
      return member;

    System.out.printf ("%s is not a datafile%n", name);
    return null;
  }

  // ---------------------------------------------------------------------------------//
  Reader getReader ()
  // ---------------------------------------------------------------------------------//
  {
    return reader;
  }

  // ---------------------------------------------------------------------------------//
  boolean isXmit ()
  // ---------------------------------------------------------------------------------//
  {
    return "xmi".equals (suffix) || "xmit".equals (suffix)
        || (reader != null && reader.isXmit ());
  }

  // ---------------------------------------------------------------------------------//
  boolean isTape ()
  // ---------------------------------------------------------------------------------//
  {
    return "aws".equals (suffix) || (reader != null && reader.isTape ());
  }

  // ---------------------------------------------------------------------------------//
  boolean isValidFileName ()
  // ---------------------------------------------------------------------------------//
  {
    return isValidFileSuffix (suffix) || isValidCompressionSuffix (suffix);
  }

  // ---------------------------------------------------------------------------------//
  static boolean isValidFileName (String fileName)
  // ---------------------------------------------------------------------------------//
  {
    if (fileName.startsWith ("__MACOSX/._"))      // no idea what this is
      return false;

    String suffix = getSuffix (fileName);
    return isValidFileSuffix (suffix) || isValidCompressionSuffix (suffix);
  }

  // ---------------------------------------------------------------------------------//
  static boolean isValidFileSuffix (String suffix)
  // ---------------------------------------------------------------------------------//
  {
    return fileSuffixes.contains (suffix);
  }

  // ---------------------------------------------------------------------------------//
  static boolean isValidCompressionSuffix (String suffix)
  // ---------------------------------------------------------------------------------//
  {
    return compressionSuffixes.contains (suffix);
  }

  // ---------------------------------------------------------------------------------//
  static String getSuffix (String filename)
  // ---------------------------------------------------------------------------------//
  {
    int dotPos = filename.lastIndexOf ('.');
    return dotPos > 0 ? filename.substring (dotPos + 1).toLowerCase () : "";
  }

  // ---------------------------------------------------------------------------------//
  List<PdsMember> getPdsXmitMembers ()
  // ---------------------------------------------------------------------------------//
  {
    assert isPartitionedDataset ();
    return ((PdsDataset) dataset).getPdsXmitMembers ();
  }

  // ---------------------------------------------------------------------------------//
  private void createReader ()
  // ---------------------------------------------------------------------------------//
  {
    if (isMember ())
      reader = new XmitReader (member);
    else if (isXmit ())
      reader = new XmitReader (file);
    else if (isTape ())
      reader = new AwsTapeReader (file);
    else
    {
      System.out.println ("Unknown suffix in createReader(): " + suffix);
      System.out.println (this);
    }
    assert reader != null;
  }

  // ---------------------------------------------------------------------------------//
  int size ()
  // ---------------------------------------------------------------------------------//
  {
    if (!isDatasetContainer ())
      return 0;

    if (reader == null)
      createReader ();

    return reader.size ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<Dataset> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    if (reader == null)
      createReader ();

    return reader.iterator ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Name ........... %s%n", name));
    text.append (String.format ("isContainer .... %s%n", isDatasetContainer ()));
    if (isDatasetContainer ())
      text.append (String.format (" datasets ...... %s%n", size ()));
    text.append (String.format ("isFile ......... %s%n", isFile ()));

    if (isFile ())
    {
      text.append (String.format (" file .......... %s%n", file));
      text.append (String.format (" suffix ........ %s%n", suffix));
      text.append (String.format (" isReadable .... %s%n", isReadableFile ()));
      text.append (String.format (" isCompressed .. %s%n", isCompressedFile ()));
      text.append (String.format (" isDirectory ... %s%n", isDirectory ()));
      text.append (String.format (" isXmit ........ %s%n", isXmit ()));
      text.append (String.format (" isTape ........ %s%n", isTape ()));
    }

    text.append (String.format ("isDataset ...... %s%n", isDataset ()));

    if (isDataset ())
    {
      text.append (String.format (" dataset ....... %s%n", dataset.getName ()));
      text.append (String.format (" isPDS ......... %s%n", isPartitionedDataset ()));
      text.append (
          String.format (" isSequential .. %s%n", isPhysicalSequentialDataset ()));
      if (isPartitionedDataset ())
      {
        text.append (String.format (" members ....... %s%n",
            ((PdsDataset) dataset).getCatalogEntries ().size ()));
        text.append (String.format (" xmit members .. %s%n",
            ((PdsDataset) dataset).getPdsXmitMembers ().size ()));
      }
    }

    text.append (String.format ("isMember ....... %s%n", isMember ()));

    if (isMember ())
      text.append (String.format (" member ........ %s%n", member.getName ()));
    text.append (String.format ("Reader ......... %s%n", reader));
    text.append (String.format (" datasets ...... %s%n", size ()));
    text.append (String.format (" merged ........ %s%n", merged));

    return text.toString ();
  }
}
