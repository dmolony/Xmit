package com.bytezone.xmit;

import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;
import com.bytezone.xmit.textunit.Dsorg.Org;
import com.bytezone.xmit.textunit.Recfm;
import com.bytezone.xmit.textunit.TextUnit;
import com.bytezone.xmit.textunit.TextUnitNumber;

// -----------------------------------------------------------------------------------//
public class Disposition
// -----------------------------------------------------------------------------------//
{
  private static String[] recfmTypes = { "?", "V", "F", "U" };

  int lrecl;
  int blksize;
  Org dsorg;
  int recfm;

  boolean isPdse;

  // ---------------------------------------------------------------------------------//
  public Disposition (Org dsorg, int recfm, int lrecl, int blksize)
  // ---------------------------------------------------------------------------------//
  {
    this.dsorg = dsorg;
    this.recfm = recfm;
    this.lrecl = lrecl;
    this.blksize = blksize;
  }

  // ---------------------------------------------------------------------------------//
  public Disposition (String recfm, String lrecl, String blksize)
  // ---------------------------------------------------------------------------------//
  {
    this.dsorg = Org.PDS;
    this.recfm = recfm.equals ("V") ? 0x5000 : recfm.equals ("F") ? 0x9000 : 0xC000;
    this.lrecl = Integer.parseInt (lrecl);
    this.blksize = Integer.parseInt (blksize);
  }

  // ---------------------------------------------------------------------------------//
  public Disposition (ControlRecord inmr02)
  // ---------------------------------------------------------------------------------//
  {
    lrecl = (int) ((TextUnitNumber) inmr02.getTextUnit (TextUnit.INMLRECL)).getNumber ();
    blksize =
        (int) ((TextUnitNumber) inmr02.getTextUnit (TextUnit.INMBLKSZ)).getNumber ();
    dsorg = ((Dsorg) inmr02.getTextUnit (TextUnit.INMDSORG)).type;
    recfm = (int) ((Recfm) inmr02.getTextUnit (TextUnit.INMRECFM)).getNumber ();
  }

  // ---------------------------------------------------------------------------------//
  void setPdse (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    assert dsorg == Org.PDS;
    this.isPdse = value;
  }

  // ---------------------------------------------------------------------------------//
  public Org getOrg ()
  // ---------------------------------------------------------------------------------//
  {
    return dsorg;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isPds ()
  // ---------------------------------------------------------------------------------//
  {
    return dsorg == Org.PDS;        // includes PDS/E
  }

  // ---------------------------------------------------------------------------------//
  public int getLrecl ()
  // ---------------------------------------------------------------------------------//
  {
    return lrecl;
  }

  // ---------------------------------------------------------------------------------//
  public int getBlksize ()
  // ---------------------------------------------------------------------------------//
  {
    return blksize;
  }

  // ---------------------------------------------------------------------------------//
  public String getRecfm ()
  // ---------------------------------------------------------------------------------//
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
  public boolean matches (String recfm, int reclen)
  // ---------------------------------------------------------------------------------//
  {
    return getRecfm ().equals (recfm) && lrecl == reclen;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    String org = isPdse ? "PDSE" : dsorg.toString ();
    return String.format ("%-3s %s %d / %d", org, getRecfm (), lrecl, blksize);
  }
}
