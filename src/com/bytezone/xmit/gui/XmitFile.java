package com.bytezone.xmit.gui;

import java.io.File;

public class XmitFile extends File
{
  File file;
  String suffix;
  String name;

  public XmitFile (File file)
  {
    super (file.getAbsolutePath ());
    this.file = file;
    suffix = XmitTree.getSuffix (file.getName ());
  }

  public XmitFile (File file, String name)
  {
    this (file);
    this.name = name;
  }

  public boolean isCompressed ()
  {
    return XmitTree.isCompressionSuffix (suffix);
  }

  @Override
  public String getName ()
  {
    return name == null ? file.getName () : name;
  }
}
