package com.bytezone.xmit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Xmit
{
  public static void main (String[] args)
  {
    String[] fileNames = { "/Users/denismolony/code/xmit/FILE069.XMI",
                           "/Users/denismolony/code/xmit/pds.xmi",
                           "/Users/denismolony/code/xmit/pds3380.xmi",
                           "/Users/denismolony/code/xmit/pds33903.xmi",
                           "/Users/denismolony/code/xmit/pds33909.xmi",
                           "/Users/denismolony/code/xmit/seq.xmi",
                           "/Users/denismolony/code/xmit/testpds.xmi",
                           "/Users/denismolony/code/xmit/testpdse.xmi" };

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
