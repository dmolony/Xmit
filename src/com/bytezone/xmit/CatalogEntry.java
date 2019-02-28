package com.bytezone.xmit;

import com.bytezone.xmit.Utility.FileType;

// ---------------------------------------------------------------------------------//
public abstract class CatalogEntry
//---------------------------------------------------------------------------------//
{
  private PdsMember member;            // contains DataBlocks
  String aliasName = "";
  final boolean usesAlias;
  final int numTtr;
  final int hw;
  final String name;
  final int ttr;

  final byte[] directoryData;
  final ModuleType moduleType;

  // not used
  private byte[] ttl = new byte[5];
  private CopyR1 copyR1;
  private CopyR2 copyR2;

  public enum ModuleType
  {
    BASIC, LOAD
  }

  // https://www.ibm.com/support/knowledgecenter/SSLTBW_2.3.0/
  //           com.ibm.zos.v2r3.ieab200/destow.htm
  // https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/
  //           com.ibm.zos.v2r3.idad400/pdsd.htm#pdsd__fg43

  // ---------------------------------------------------------------------------------//
  static CatalogEntry instanceOf (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    int numTtr = (buffer[ptr + 11] & 0x60) >>> 5;     // number of TTRs in user data
    int hw = buffer[ptr + 11] & 0x1F;                 // half words of user data

    byte[] directoryData = new byte[12 + hw * 2];
    System.arraycopy (buffer, ptr, directoryData, 0, directoryData.length);

    return numTtr == 0 ?                              //
        new BasicModule (directoryData)               // 
        : new LoadModule (directoryData);             // 
  }

  // ---------------------------------------------------------------------------------//
  CatalogEntry (ModuleType moduleType, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    this.moduleType = moduleType;
    directoryData = buffer;

    name = Utility.getString (buffer, 0, 8).trim ();
    ttr = (int) Utility.getValue (buffer, 8, 3);            // TTR of first block

    usesAlias = (buffer[11] & 0x80) != 0;     // name in the first field is an alias
    numTtr = (buffer[11] & 0x60) >>> 5;       // number of TTRs in user data
    hw = buffer[11] & 0x1F;                   // half words of user data
  }

  // ---------------------------------------------------------------------------------//
  public abstract String getDebugLine ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  public ModuleType getModuleType ()
  // ---------------------------------------------------------------------------------//
  {
    return moduleType;
  }

  // ---------------------------------------------------------------------------------//
  int getEntryLength ()
  // ---------------------------------------------------------------------------------//
  {
    return directoryData.length;
  }

  // ---------------------------------------------------------------------------------//
  public int getDataLength ()
  // ---------------------------------------------------------------------------------//
  {
    return member.dataLength;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isAlias ()
  // ---------------------------------------------------------------------------------//
  {
    return usesAlias;
  }

  // ---------------------------------------------------------------------------------//
  int getTtr ()
  // ---------------------------------------------------------------------------------//
  {
    return ttr;
  }

  // ---------------------------------------------------------------------------------//
  public FileType getFileType ()
  // ---------------------------------------------------------------------------------//
  {
    return member.getFileType ();
  }

  // ---------------------------------------------------------------------------------//
  public String getMemberName ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }

  // ---------------------------------------------------------------------------------//
  public String getAliasName ()
  // ---------------------------------------------------------------------------------//
  {
    return aliasName;
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

    int index = ((ttr & 0x00FF00) >>> 8) / mult;

    long lo = Utility.getFourBytes (copyR2.buffer, index * 16 + 22);
    long hi = Utility.getFourBytes (copyR2.buffer, index * 16 + 26);
    //      System.out.printf ("%02X  Range: %08X : %08X%n", index, lo, hi);

    byte[] temp = convert (index, mult);

    long val = Utility.getValue (temp, 0, 4);     // ignore last byte
    System.out.printf ("%02X  %06X -> %s ", index, ttr, Utility.getHexValues (temp));

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

    int xxxx = (ttr & 0xFFFF00) >>> 8;
    int yyyy = Utility.getTwoBytes (copyR2.buffer, index * 16 + 22);
    int zzzz = Utility.getTwoBytes (copyR2.buffer, index * 16 + 24);

    int dddd = (xxxx + zzzz) / mult + yyyy - index;

    tt[0] = (byte) ((dddd & 0xFF00) >>> 8);
    tt[1] = (byte) ((dddd & 0x00FF) >>> 0);
    tt[2] = 0;
    tt[3] = (byte) ((xxxx + zzzz) % mult);
    tt[4] = (byte) (ttr & 0x0000FF);

    return tt;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%02X %-8s %06X ", directoryData[11], name, ttr);
  }
}
