package com.bytezone.xmit.textunit;

public class Recfm extends TextUnit
{
  public enum RecordFormat
  {
    F, FB, FBA, FS, FBS, FBM, V, VB, VBA, VS, VBS, VBM
  }

  public enum Organisation
  {
    PS, PSU, PO, POU, DA, DAU, GDG, IS, ISU, PDSM, VSAM
  }

  String type;

  public Recfm (byte[] buffer, int ptr)
  {
    super (buffer, ptr);

  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%-8s  %s", mnemonics[keyId], type);
  }
}
