package com.bytezone.xmit;

import java.time.LocalDate;
import java.util.Optional;

// ---------------------------------------------------------------------------------//
public class BasicModule
// ---------------------------------------------------------------------------------//
{
  final int size;
  final int init;
  private final int mod;

  final int vv;
  final int mm;

  LocalDate dateCreated;
  LocalDate dateModified;
  String time = "";

  String userName = "";
  String aliasName = "";
  private final boolean usesAlias;
  byte[] directoryData;

  // ---------------------------------------------------------------------------------//
  BasicModule (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    directoryData = buffer;

    usesAlias = (buffer[11] & 0x80) != 0;     // name in the first field is an alias
    int hw = buffer[11] & 0x1F;               // half words of user data

    if (hw == 0)
    {
      vv = 0;
      mm = 0;
      size = 0;
      mod = 0;
      init = 0;
    }
    else
    {
      vv = buffer[12] & 0xFF;
      mm = buffer[13] & 0xFF;

      Optional<LocalDate> opt = Utility.getLocalDate (buffer, 16);
      if (opt.isPresent ())
        dateCreated = opt.get ();

      opt = Utility.getLocalDate (buffer, 20);
      if (opt.isPresent ())
        dateModified = opt.get ();

      time = String.format ("%02X:%02X:%02X", buffer[24], buffer[25], buffer[15]);

      size = Utility.getTwoBytes (buffer, 26);
      init = Utility.getTwoBytes (buffer, 28);
      mod = Utility.getTwoBytes (buffer, 30);
      userName = Utility.getString (buffer, 32, 8).trim ();

      if (usesAlias && directoryData.length > 42)
        aliasName = Utility.getString (buffer, buffer.length - 8, 8);
    }
  }

  // ---------------------------------------------------------------------------------//
  public String debugLineBasic ()
  // ---------------------------------------------------------------------------------//
  {
    if (directoryData == null)
      return "";

    String hex = Utility.getHexValues (directoryData, 12, directoryData.length - 12);
    return hex;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    String date1Text = dateCreated == null ? ""
        : String.format ("%td %<tb %<tY", dateCreated).replace (".", "");
    return String.format ("%8s  %,6d  %s  %s", userName, size, date1Text, time);
  }
}
