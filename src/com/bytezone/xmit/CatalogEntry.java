package com.bytezone.xmit;

import com.bytezone.xmit.Utility.FileType;

// -----------------------------------------------------------------------------------//
public abstract class CatalogEntry
// -----------------------------------------------------------------------------------//
{
  private PdsMember member;             // contains DataBlocks
  private String aliasName = "";        // member we are an alias of

  private final boolean isAlias;
  private final int numTtr;
  final int halfWords;
  private final String name;
  private final int ttr;

  final byte[] directoryData;
  final ModuleType moduleType;

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

    isAlias = (buffer[11] & 0x80) != 0;       // name in the first field is an alias
    numTtr = (buffer[11] & 0x60) >>> 5;       // number of TTRs in user data
    halfWords = buffer[11] & 0x1F;            // half words of user data
  }

  // ---------------------------------------------------------------------------------//
  public Disposition getDisposition ()
  // ---------------------------------------------------------------------------------//
  {
    return member.getDisposition ();
  }

  // ---------------------------------------------------------------------------------//
  public abstract String getDebugLine ();
  // ---------------------------------------------------------------------------------//

  public static String getDebugHeader ()
  {
    return "- - -- --name-- -ttr--  ";
  }

  // ---------------------------------------------------------------------------------//
  public ModuleType getModuleType ()
  // ---------------------------------------------------------------------------------//
  {
    return moduleType;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isBasicModule ()
  // ---------------------------------------------------------------------------------//
  {
    return moduleType == ModuleType.BASIC;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isLoadModule ()
  // ---------------------------------------------------------------------------------//
  {
    return moduleType == ModuleType.LOAD;
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
    return member.getDataLength ();
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
  public boolean isAlias ()
  // ---------------------------------------------------------------------------------//
  {
    return isAlias;
  }

  // ---------------------------------------------------------------------------------//
  public String getAliasName ()
  // ---------------------------------------------------------------------------------//
  {
    return aliasName;
  }

  // ---------------------------------------------------------------------------------//
  void setAliasName (String aliasName)
  // ---------------------------------------------------------------------------------//
  {
    assert isAlias ();
    if (!this.aliasName.isEmpty ())
      System.out.printf ("%s - Alias not empty: %s -> %s%n", name, this.aliasName,
          aliasName);
    this.aliasName = aliasName;
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
  public boolean contains (String key)
  // ---------------------------------------------------------------------------------//
  {
    return member.contains (key);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%1.1s %d %02X %-8s %06X ", isAlias ? "A" : ".", numTtr,
        halfWords, name, ttr);
  }
}
