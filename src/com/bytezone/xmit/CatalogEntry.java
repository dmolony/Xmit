package com.bytezone.xmit;

import java.time.LocalDate;

// ---------------------------------------------------------------------------------//
public class CatalogEntry
//---------------------------------------------------------------------------------//
{
  private PdsMember member;                   // contains DataBlocks
  private LoadModule loadModule;
  private BasicModule basicModule;

  private final String name;
  private final String userName = "";
  private String aliasName = "";

  private final boolean usesAlias;
  private final int numTtr;

  private int sectionL;

  private final byte[] directoryData;

  private final int blockFrom;
  private byte[] ttl = new byte[5];

  private CopyR1 copyR1;
  private CopyR2 copyR2;

  int hw;

  // https://www.ibm.com/support/knowledgecenter/SSLTBW_2.3.0/
  //           com.ibm.zos.v2r3.ieab200/destow.htm
  // https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/
  //           com.ibm.zos.v2r3.idad400/pdsd.htm#pdsd__fg43

  // ---------------------------------------------------------------------------------//
  CatalogEntry (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    name = Utility.getString (buffer, ptr, 8).trim ();
    blockFrom = (int) Utility.getValue (buffer, ptr + 8, 3);    // TTR of first block

    numTtr = (buffer[ptr + 11] & 0x60) >>> 5;      // number of TTRs in user data
    usesAlias = (buffer[ptr + 11] & 0x80) != 0;    // name in the first field is an alias

    hw = buffer[ptr + 11] & 0xFF;
    int size = buffer[ptr + 11] & 0x1F;
    directoryData = new byte[12 + size * 2];
    System.arraycopy (buffer, ptr, directoryData, 0, directoryData.length);

    if (numTtr == 0)
      basicModule = size > 0 ? new BasicModule (directoryData) : new BasicModule ();
    else
      loadModule = new LoadModule (directoryData);

    if (false)
    {
      System.out.print (Utility.getHexValuesWithText (directoryData, 0, 11));
      System.out.printf (" | %d %d %02X |  ", usesAlias ? 1 : 0, numTtr, size);
      System.out.println (
          Utility.getHexValuesWithText (directoryData, 11, directoryData.length - 11));
    }

    if (usesAlias && directoryData.length >= 44)
      aliasName = Utility.getString (buffer, ptr + 36, 8);
  }

  // ---------------------------------------------------------------------------------//
  public boolean isBasic ()
  // ---------------------------------------------------------------------------------//
  {
    return basicModule != null;
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getDirectoryData ()
  // ---------------------------------------------------------------------------------//
  {
    return directoryData;
  }

  // ---------------------------------------------------------------------------------//
  public int getOffset ()
  // ---------------------------------------------------------------------------------//
  {
    return blockFrom;
  }

  // ---------------------------------------------------------------------------------//
  void setMember (PdsMember member)
  // ---------------------------------------------------------------------------------//
  {
    this.member = member;
  }

  // ---------------------------------------------------------------------------------//
  public PdsMember getMember ()
  // ---------------------------------------------------------------------------------//
  {
    return member;
  }

  // ---------------------------------------------------------------------------------//
  void setCopyRecords (CopyR1 copyR1, CopyR2 copyR2)
  // ---------------------------------------------------------------------------------//
  {
    this.copyR1 = copyR1;
    this.copyR2 = copyR2;
  }

  // ---------------------------------------------------------------------------------//
  private long notUsed ()
  // ---------------------------------------------------------------------------------//
  {
    int extents = copyR2.buffer[0] & 0xFF;
    //    int mult = copyR2.buffer[31];   // 0x0F;
    int mult = 0x0F;

    //    for (int index = 0; index < extents; index++)
    //    {
    // blockFrom:            xxxx  ff
    // copyR2   :     yyyy   zzzz
    // address  :       qq
    //               ----------------
    //               dd dd  00 rr  ff

    // qq = (xxxx + zzzz) / 15
    // rr = (xxxx + zzzz) % 15

    //    if (false)
    //    {
    //      int xxxx = (blockFrom & 0xFFFF00) >>> 8;
    //      int yyyy = Utility.getTwoBytes (copyR2.buffer, 22);
    //      int zzzz = Utility.getTwoBytes (copyR2.buffer, 24);
    //
    //      int dddd = (xxxx + zzzz) / 15 + yyyy;
    //
    //      ttl[0] = (byte) ((dddd & 0xFF00) >>> 8);
    //      ttl[1] = (byte) ((dddd & 0x00FF) >>> 0);
    //      ttl[2] = 0;
    //      ttl[3] = (byte) ((xxxx + zzzz) % 15);
    //      ttl[4] = (byte) (blockFrom & 0x0000FF);
    //    }

    int index = ((blockFrom & 0x00FF00) >>> 8) / mult;

    long lo = Utility.getFourBytes (copyR2.buffer, index * 16 + 22);
    long hi = Utility.getFourBytes (copyR2.buffer, index * 16 + 26);
    //      System.out.printf ("%02X  Range: %08X : %08X%n", index, lo, hi);

    byte[] temp = convert (index, mult);

    long val = Utility.getValue (temp, 0, 4);     // ignore last byte
    System.out.printf ("%02X  %06X -> %s ", index, blockFrom,
        Utility.getHexValues (temp));

    if (lo <= val && hi >= val)
    {
      System.out.println ("OK");
      ttl = temp;
    }
    else
    {
      System.out.printf (": Out of range%n");
    }

    return Utility.getValue (ttl, 0, 5);
  }

  // ---------------------------------------------------------------------------------//
  private byte[] convert (int index, int mult)
  // ---------------------------------------------------------------------------------//
  {
    byte[] tt = new byte[5];

    int xxxx = (blockFrom & 0xFFFF00) >>> 8;
    int yyyy = Utility.getTwoBytes (copyR2.buffer, index * 16 + 22);
    int zzzz = Utility.getTwoBytes (copyR2.buffer, index * 16 + 24);

    int dddd = (xxxx + zzzz) / mult + yyyy - index;

    tt[0] = (byte) ((dddd & 0xFF00) >>> 8);
    tt[1] = (byte) ((dddd & 0x00FF) >>> 0);
    tt[2] = 0;
    tt[3] = (byte) ((xxxx + zzzz) % mult);
    tt[4] = (byte) (blockFrom & 0x0000FF);

    return tt;
  }

  // ---------------------------------------------------------------------------------//
  public String getMemberName ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }

  // ---------------------------------------------------------------------------------//
  public String getUserName ()
  // ---------------------------------------------------------------------------------//
  {
    return userName;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isAlias ()
  // ---------------------------------------------------------------------------------//
  {
    return !aliasName.isEmpty ();
  }

  // ---------------------------------------------------------------------------------//
  public String getAliasName ()
  // ---------------------------------------------------------------------------------//
  {
    return aliasName;
  }

  // ---------------------------------------------------------------------------------//
  public int getSize ()
  // ---------------------------------------------------------------------------------//
  {
    return basicModule == null ? 0 : basicModule.size;
  }

  // ---------------------------------------------------------------------------------//
  public int getAMode ()
  // ---------------------------------------------------------------------------------//
  {
    return loadModule == null ? 0 : loadModule.aMode;
  }

  // ---------------------------------------------------------------------------------//
  public int getRMode ()
  // ---------------------------------------------------------------------------------//
  {
    return loadModule == null ? 0 : loadModule.rMode;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isApf ()
  // ---------------------------------------------------------------------------------//
  {
    return loadModule == null ? false : loadModule.apf;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isReentrant ()
  // ---------------------------------------------------------------------------------//
  {
    return loadModule == null ? false : loadModule.reentrant;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isReusable ()
  // ---------------------------------------------------------------------------------//
  {
    return loadModule == null ? false : loadModule.reusable;
  }

  // ---------------------------------------------------------------------------------//
  public int getInit ()
  // ---------------------------------------------------------------------------------//
  {
    return basicModule == null ? 0 : basicModule.init;
  }

  // ---------------------------------------------------------------------------------//
  public LocalDate getDateCreated ()
  // ---------------------------------------------------------------------------------//
  {
    return basicModule == null ? null : basicModule.dateCreated;
  }

  // ---------------------------------------------------------------------------------//
  public LocalDate getDateModified ()
  // ---------------------------------------------------------------------------------//
  {
    return basicModule == null ? null : basicModule.dateModified;
  }

  // ---------------------------------------------------------------------------------//
  public String getTime ()
  // ---------------------------------------------------------------------------------//
  {
    return basicModule == null ? "" : basicModule.time;
  }

  // ---------------------------------------------------------------------------------//
  public String getVersion ()
  // ---------------------------------------------------------------------------------//
  {
    if (basicModule == null || basicModule.vv == 0 & basicModule.mm == 0)
      return "";
    return String.format ("%02d.%02d", basicModule.vv, basicModule.mm);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    String detail =
        basicModule != null ? basicModule.toString () : loadModule.toString ();
    return String.format ("%8s  %s  %8s", name, detail, aliasName);
  }
}
