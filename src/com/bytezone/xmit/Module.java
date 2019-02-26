package com.bytezone.xmit;

// ---------------------------------------------------------------------------------//
public abstract class Module
// ---------------------------------------------------------------------------------//
{
  String aliasName = "";
  final boolean usesAlias;
  final int numTtr;
  final int hw;
  final String name;
  final int ttr;

  final byte[] directoryData;

  // ---------------------------------------------------------------------------------//
  public Module (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    directoryData = buffer;

    name = Utility.getString (buffer, 0, 8).trim ();
    ttr = (int) Utility.getValue (buffer, 8, 3);    // TTR of first block

    usesAlias = (buffer[11] & 0x80) != 0;     // name in the first field is an alias
    numTtr = (buffer[11] & 0x60) >>> 5;       // number of TTRs in user data
    hw = buffer[11] & 0x1F;                   // half words of user data
  }

  public abstract String getDebugLine ();

  // ---------------------------------------------------------------------------------//
  public String getAliasName ()
  // ---------------------------------------------------------------------------------//
  {
    return aliasName;
  }
}
