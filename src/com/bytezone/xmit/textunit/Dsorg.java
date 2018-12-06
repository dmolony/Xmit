package com.bytezone.xmit.textunit;

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

  String type;

  public Dsorg (byte[] buffer, int ptr)
  {
    super (buffer, ptr);

    //    type = getType (dataList.get (0).data[0]);
  }

  //  private String getType (byte code)
  //  {
  //    int recfm = code & 0xD6;      // ???
  //    //    System.out.printf ("RECFM: %02X%n", code);
  //    
  //  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%-8s  %s", mnemonics[keyId], type);
  }
}
