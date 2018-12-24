package com.bytezone.xmit.gui;

import java.io.File;

public class XmitFile extends File
{
  File file;

  public XmitFile (File file)
  {
    super (file.getAbsolutePath ());
    this.file = file;
  }
}
