package com.bytezone.xmit;

// ---------------------------------------------------------------------------------//
public class LoadModule
// ---------------------------------------------------------------------------------//
{
  final boolean reentrant;
  final boolean reusable;
  final boolean overlay;
  final boolean test;
  final boolean loadOnly;
  final boolean scatter;
  final boolean executable;
  final boolean multiBlock;

  final boolean dc;
  final boolean zeroOrg;
  final boolean zeroEp;
  final boolean rld;
  final boolean edit;
  final boolean sym;
  final boolean fLvl;
  final boolean refreshable;

  final boolean aosLinkEditor;
  final boolean lpo;
  final boolean pageAligned;
  final boolean ssi;
  final boolean apf;
  final boolean ptb3Valid;
  final boolean objSigned;
  final boolean free1;

  final boolean nameGen;
  final boolean free2;
  final boolean free3;

  final int rMode;
  final int aMode;
  final int aliasAMode;

  int ttrText;
  int ttrNoteList;
  int notes;
  int storage;
  int firstTextBlock;
  int epa;

  // ---------------------------------------------------------------------------------//
  LoadModule (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    //    System.out.println (Utility.getHexValues (buffer));
    boolean alias = (buffer[11] & 0x80) != 0;

    ttrText = (int) Utility.getValue (buffer, 12, 3);
    ttrNoteList = (int) Utility.getValue (buffer, 16, 3);
    notes = buffer[19] & 0xFF;
    byte attr1 = buffer[20];
    byte attr2 = buffer[21];
    storage = (int) Utility.getValue (buffer, 22, 3);
    firstTextBlock = Utility.getTwoBytes (buffer, 25);
    epa = (int) Utility.getValue (buffer, 27, 3);
    byte vsFlag1 = buffer[30];
    byte vsFlag2 = buffer[31];
    byte vsFlag3 = buffer[32];

    reentrant = (attr1 & 0x80) != 0;
    reusable = (attr1 & 0x40) != 0;
    overlay = (attr1 & 0x20) != 0;
    test = (attr1 & 0x10) != 0;
    loadOnly = (attr1 & 0x08) != 0;
    scatter = (attr1 & 0x04) != 0;
    executable = (attr1 & 0x02) != 0;
    multiBlock = (attr1 & 0x01) != 0;

    dc = (attr2 & 0x80) != 0;
    zeroOrg = (attr2 & 0x40) != 0;
    zeroEp = (attr2 & 0x20) != 0;
    rld = (attr2 & 0x10) != 0;
    edit = (attr2 & 0x08) != 0;
    sym = (attr2 & 0x04) != 0;
    fLvl = (attr2 & 0x02) != 0;
    refreshable = (attr2 & 0x01) != 0;

    aosLinkEditor = (vsFlag1 & 0x80) != 0;
    lpo = (vsFlag1 & 0x40) != 0;
    pageAligned = (vsFlag1 & 0x20) != 0;
    ssi = (vsFlag1 & 0x10) != 0;
    apf = (vsFlag1 & 0x08) != 0;
    ptb3Valid = (vsFlag1 & 0x04) != 0;
    objSigned = (vsFlag1 & 0x02) != 0;
    free1 = (vsFlag1 & 0x01) != 0;

    nameGen = (vsFlag2 & 0x80) != 0;
    free2 = (vsFlag2 & 0x40) != 0;
    free3 = (vsFlag2 & 0x20) != 0;
    rMode = (vsFlag2 & 0x10) == 0 ? 24 : 31;
    aliasAMode = (vsFlag2 & 0x0C) >>> 2;
    int aaMode = (vsFlag2 & 0x03);

    aMode = aaMode == 0 ? 24 : aaMode == 1 ? 64 : aaMode == 2 ? 31 : 255;

    if (scatter)
    {

    }

    if (alias)
    {

    }

    if (ssi)
    {

    }

    if (apf)
    {

    }

    if (lpo)
    {

    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return "load module";
  }
}