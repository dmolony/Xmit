package com.bytezone.xmit.textunit;

import com.bytezone.xmit.Utility;

public class Dsorg extends TextUnit
{
  //  Describes INMDSORG

  public final static int VSAM = 0x0008;
  public final static int PDS = 0x0200;
  public final static int PS = 0x4000;

  //   Describes INMTYPE

  public final static int DataLib = 0x80;
  public final static int PgmLib = 0x40;
  public final static int XFSDS = 0x04;
  public final static int LFSDS = 0x01;

  public enum Organisation
  {
    PS, PSU, PO, POU, DA, DAU, GDG, IS, ISU, PDSM, VSAM
  }

  public enum Org
  {
    VSAM, PDS, PS
  }

  public Org type;

  public Dsorg (byte[] buffer, int ptr)
  {
    super (buffer, ptr);

    int value = Utility.getWord (dataList.get (0).data, 0);
    switch (value)
    {
      case PS:
        type = Org.PS;
        break;
      case PDS:
        type = Org.PDS;
        break;
      case VSAM:
        type = Org.VSAM;
        break;
    }
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%04X  %-8s  %s", keys[keyId], mnemonics[keyId], type);
  }
}
