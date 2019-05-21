package com.bytezone.xmit;

// https://www.ibm.com/support/knowledgecenter/SSLTBW_2.3.0/
// com.ibm.zos.v2r3.ieab200/destow.htm
// -----------------------------------------------------------------------------------//
public class LoadModule extends CatalogEntry
// -----------------------------------------------------------------------------------//
{
  final boolean reentrant;
  final boolean reusable;
  final boolean overlay;
  final boolean test;
  final boolean loadOnly;
  public final boolean scatter;
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
  public final boolean lpo;
  final boolean pageAligned;
  public final boolean ssi;
  public final boolean apfBlock;
  final boolean ptb3Valid;
  final boolean objSigned;
  public final boolean attr;

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
  int apf;

  long ssiWord;
  int aliasTtr;

  // ---------------------------------------------------------------------------------//
  LoadModule (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (ModuleType.LOAD, buffer);

    ttrText = (int) Utility.getValue (buffer, 12, 3);
    int zero = buffer[15] & 0xFF;
    ttrNoteList = (int) Utility.getValue (buffer, 16, 3);   // or ttrScatter
    notes = buffer[19] & 0xFF;

    byte attr1 = buffer[20];
    byte attr2 = buffer[21];

    storage = (int) Utility.getValue (buffer, 22, 3);
    firstTextBlock = Utility.getTwoBytes (buffer, 25);
    epa = (int) Utility.getValue (buffer, 27, 3);

    byte vsFlag1 = buffer[30];
    byte vsFlag2 = buffer[31];
    byte vsFlag3 = buffer[32];

    int ptr = 33;

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
    apfBlock = (vsFlag1 & 0x08) != 0;
    ptb3Valid = (vsFlag1 & 0x04) != 0;
    objSigned = (vsFlag1 & 0x02) != 0;
    attr = (vsFlag1 & 0x01) != 0;

    nameGen = (vsFlag2 & 0x80) != 0;
    free2 = (vsFlag2 & 0x40) != 0;
    free3 = (vsFlag2 & 0x20) != 0;
    rMode = (vsFlag2 & 0x10) == 0 ? 24 : 31;
    aliasAMode = (vsFlag2 & 0x0C) >>> 2;
    int aaMode = (vsFlag2 & 0x03);

    aMode = aaMode == 0 ? 24 : aaMode == 1 ? 64 : aaMode == 2 ? 31 : 255;

    if (scatter)
    {
      int slsz = (int) Utility.getValue (buffer, ptr, 2);
      int ttsz = (int) Utility.getValue (buffer, ptr + 2, 2);
      int esdt = (int) Utility.getValue (buffer, ptr + 4, 2);
      int esdc = (int) Utility.getValue (buffer, ptr + 6, 2);
      ptr += 8;
    }

    if (isAlias ())
    {
      if (ptr + 11 <= buffer.length)
      {
        aliasTtr = (int) Utility.getValue (buffer, ptr, 3);
        setAliasName (Utility.getString (buffer, ptr + 3, 8).trim ());
      }
      ptr += 11;
    }

    if (ssi)
    {
      if (ptr % 2 == 1)
        ptr++;
      ssiWord = Utility.getValue (buffer, ptr, 4);
      ptr += 4;
    }

    if (apfBlock)
    {
      int len1 = buffer[ptr++] & 0xFF;
      apf = buffer[ptr++] & 0xFF;
    }

    if (lpo)
    {
      int len1 = buffer[ptr++] & 0xFF;
      long fullWord1 = Utility.getValue (buffer, ptr + 1, 4);
      long fullWord2 = Utility.getValue (buffer, ptr + 5, 4);
      long fullWord3 = Utility.getValue (buffer, ptr + 9, 4);
      ptr += 13;
    }

    if (false && attr)
    {
      int byte0 = buffer[ptr++] & 0xFF;
      int byte1 = buffer[ptr++] & 0xFF;
      int reserved = buffer[ptr++] & 0xFF;
      ptr += (byte1 & 0x0F);
    }

    //    System.out.printf ("[%2d  %2d]%n", ptr, buffer.length);
    //    if (ptr < buffer.length)
    //      System.out.println (Utility.getHexDump (buffer, ptr, buffer.length - ptr));
  }

  // ---------------------------------------------------------------------------------//
  public static String getDebugHeader ()
  // ---------------------------------------------------------------------------------//
  {
    return CatalogEntry.getDebugHeader ()
        + "-ttr2- 00 -ttr3- nt a1 a2 --stor-- -txt- --epa--- v1 v2 v3  "
        + "------- scatter -------  ------------- alias ------------  "
        + "--- ssi ---  -apf-  ----------------- lpo ----------------";
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getDebugLine ()
  // ---------------------------------------------------------------------------------//
  {
    String scatterText = "";
    String aliasText = "";
    String ssiText = "";
    String apfText = "";
    String lpoText = "";
    String extra = "";
    int ptr = 33;

    if (scatter)
    {
      scatterText = Utility.getHexValues (directoryData, ptr, 8);
      ptr += 8;
    }

    if (isAlias ())
    {
      if (ptr + 11 <= directoryData.length)
        aliasText = Utility.getHexValuesWithText (directoryData, ptr, 11);
      ptr += 11;
    }

    if (ssi)
    {
      if (ptr % 2 == 1)
        ptr++;
      ssiText = Utility.getHexValues (directoryData, ptr, 4);
      ptr += 4;
    }

    if (apfBlock)
    {
      apfText = Utility.getHexValues (directoryData, ptr, 2);
      ptr += 2;
    }

    if (lpo)
    {
      lpoText = Utility.getHexValues (directoryData, ptr, 13);
      ptr += 13;
    }

    if (false && attr)
    {
      int byte0 = directoryData[ptr++] & 0xFF;
      int byte1 = directoryData[ptr++] & 0xFF;
      int reserved = directoryData[ptr++] & 0xFF;
      ptr += (byte1 & 0x0F);
    }

    if (ptr < directoryData.length)
      extra =
          Utility.getHexValuesWithText (directoryData, ptr, directoryData.length - ptr);

    long ttr2 = Utility.getValue (directoryData, 12, 3);
    int zero = directoryData[15] & 0xFF;
    long ttr3 = Utility.getValue (directoryData, 16, 3);
    String hex = Utility.getHexValues (directoryData, 19, 14);

    return String.format ("%23s %06X %02X %06X %-42s %-24s %-33s %-12s %-6s %-39s %s",
        super.toString (), ttr2, zero, ttr3, hex, scatterText, aliasText, ssiText,
        apfText, lpoText, extra).trim ();
  }

  // ---------------------------------------------------------------------------------//
  public int getStorage ()
  // ---------------------------------------------------------------------------------//
  {
    return storage;
  }

  // ---------------------------------------------------------------------------------//
  public int getEpa ()
  // ---------------------------------------------------------------------------------//
  {
    return epa;
  }

  // ---------------------------------------------------------------------------------//
  public int getAMode ()
  // ---------------------------------------------------------------------------------//
  {
    return aMode;
  }

  // ---------------------------------------------------------------------------------//
  public int getRMode ()
  // ---------------------------------------------------------------------------------//
  {
    return rMode;
  }

  // ---------------------------------------------------------------------------------//
  public long getSsi ()
  // ---------------------------------------------------------------------------------//
  {
    return ssiWord;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isApf ()
  // ---------------------------------------------------------------------------------//
  {
    return apf == 1;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isReentrant ()
  // ---------------------------------------------------------------------------------//
  {
    return reentrant;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isReusable ()
  // ---------------------------------------------------------------------------------//
  {
    return reusable;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isOverlay ()
  // ---------------------------------------------------------------------------------//
  {
    return overlay;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isTest ()
  // ---------------------------------------------------------------------------------//
  {
    return test;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%s  %-8s  ", isAlias () ? "A" : ".", getMemberName ());
  }
}
