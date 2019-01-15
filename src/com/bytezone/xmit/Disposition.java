package com.bytezone.xmit;

import com.bytezone.xmit.textunit.Dsorg.Org;

public class Disposition
{
  private static String[] recfmTypes = { "?", "V", "F", "U" };

  int lrecl;
  int blksize;
  Org dsorg;
  int recfm;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public Disposition (Org dsorg, int recfm, int lrecl, int blksize)
  {
    this.dsorg = dsorg;
    this.recfm = recfm;
    this.lrecl = lrecl;
    this.blksize = blksize;
  }

  // ---------------------------------------------------------------------------------//
  // getOrg
  // ---------------------------------------------------------------------------------//

  public Org getOrg ()
  {
    return dsorg;
  }

  // ---------------------------------------------------------------------------------//
  // getLrecl
  // ---------------------------------------------------------------------------------//

  public int getLrecl ()
  {
    return lrecl;
  }

  // ---------------------------------------------------------------------------------//
  // getBlksize
  // ---------------------------------------------------------------------------------//

  public int getBlksize ()
  {
    return blksize;
  }

  // ---------------------------------------------------------------------------------//
  // getRecfm
  // ---------------------------------------------------------------------------------//

  public String getRecfm ()
  {
    String recfmText = recfmTypes[(recfm & 0xC000) >>> 14];
    if ((recfm & 0x1000) != 0)
      recfmText += "B";
    int carriageControl = (recfm & 0x0600) >> 9;
    if (carriageControl == 1 || carriageControl == 2)
      recfmText += "A";

    return recfmText;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%-3s %s %d / %d", dsorg, getRecfm (), lrecl, blksize);
  }
}
