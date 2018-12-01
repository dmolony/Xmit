package com.bytezone.xmit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Xmit
{
  public static void main (String[] args)
  {
    String[] fileNames = { "files/FILE069.XMI", "files/pds.xmi", "files/pds3380.xmi",
                           "files/pds33903.xmi", "files/pds33909.xmi", "files/seq.xmi",
                           "files/testpds.xmi", "files/testpdse.xmi" };

    int i = 0;

    try
    {
      System.out.printf ("fileName: %s%n", fileNames[i]);
      new Reader (Files.readAllBytes (Paths.get (fileNames[i])));
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }
}
