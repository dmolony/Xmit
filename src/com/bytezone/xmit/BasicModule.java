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

  private String userName = "";
  byte[] directoryData;

  // ---------------------------------------------------------------------------------//
  BasicModule ()
  // ---------------------------------------------------------------------------------//
  {
    vv = 0;
    mm = 0;
    size = 0;
    mod = 0;
    init = 0;
  }

  // ---------------------------------------------------------------------------------//
  BasicModule (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    directoryData = buffer;

    vv = buffer[12] & 0xFF;
    mm = buffer[13] & 0xFF;

    Optional<LocalDate> opt = Utility.getLocalDate (buffer, 16);
    if (opt.isPresent ())
      dateCreated = opt.get ();

    opt = Utility.getLocalDate (buffer, 20);
    if (opt.isPresent ())
      dateModified = opt.get ();

    //    int moduleAttrs1 = buffer[ptr] & 0xFF;
    //    int moduleAttrs2 = buffer[ptr] & 0xFF;

    time = String.format ("%02X:%02X:%02X", buffer[24], buffer[25], buffer[15]);

    size = Utility.getTwoBytes (buffer, 26);
    init = Utility.getTwoBytes (buffer, 28);
    mod = Utility.getTwoBytes (buffer, 30);
    userName = Utility.getString (buffer, 32, 8).trim ();
  }

  // ---------------------------------------------------------------------------------//
  public String debugLineBasic ()
  // ---------------------------------------------------------------------------------//
  {
    String hex = "";
    String t1 = "";
    //    byte[] directoryData = catalogEntry.getDirectoryData ();

    int extra = directoryData[11] & 0xFF;      // indicator byte
    if (extra == 0x2E)
      hex =
          Utility.getHexValues (directoryData, 12, 22) + "                              "
              + Utility.getHexValues (directoryData, 34, 6);
    else if (extra == 0x31)
      hex =
          Utility.getHexValues (directoryData, 12, 22) + "                              "
              + Utility.getHexValues (directoryData, 34, 12);
    else
      hex = Utility.getHexValues (directoryData, 12, directoryData.length - 12);

    if (extra == 0xB6)
      t1 = Utility.getString (directoryData, 48, 8);

    return String.format (" %-129s %8s", hex, t1).trim ();
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
