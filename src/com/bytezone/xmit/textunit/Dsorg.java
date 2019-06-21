package com.bytezone.xmit.textunit;

import com.bytezone.xmit.Utility;

// -----------------------------------------------------------------------------------//
public class Dsorg extends TextUnit
// -----------------------------------------------------------------------------------//
{
  public final static int VSAM = 0x0008;        // VSAM
  public final static int PDS = 0x0200;         // partioned organisation
  public final static int PS = 0x4000;          // physical sequential

  //  public enum Organisation
  //  {
  //    PS, PSU, PO, POU, DA, DAU, GDG, IS, ISU, PDSM, VSAM
  //  }

  // ---------------------------------------------------------------------------------//
  public enum Org
  // ---------------------------------------------------------------------------------//
  {
    VSAM, PDS, PS
  }

  public final Org type;

  // ---------------------------------------------------------------------------------//
  public Dsorg (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    super (buffer, ptr);

    int value = Utility.getTwoBytes (dataList.get (0).data, 0);
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
      default:
        System.out.printf ("** Unknown DSORG value: %04X%n", value);
        type = null;
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return type == null ? super.toString ()
        : String.format ("%04X  %-8s  %s", keys[keyId], mnemonics[keyId], type);
  }
}
