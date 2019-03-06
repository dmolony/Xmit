package com.bytezone.xmit;

import java.time.LocalDate;
import java.util.Optional;

// ---------------------------------------------------------------------------------//
public class BasicModule extends CatalogEntry
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

  // ---------------------------------------------------------------------------------//
  BasicModule (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (ModuleType.BASIC, buffer);

    assert numTtr == 0;

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

      int zero = buffer[14] & 0xFF;

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
  public static String getDebugHeader ()
  // ---------------------------------------------------------------------------------//
  {
    return CatalogEntry.getDebugHeader () + "-- id -- vv mm 00 ss -created--  -modified-"
        + "  hh mm size1 size2 size3 -------- user ---------";
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getDebugLine ()
  // ---------------------------------------------------------------------------------//
  {
    if (directoryData == null)
      return "";

    return String.format ("%23s %-8s %s", super.toString (), userName,
        Utility.getHexValues (directoryData, 12, directoryData.length - 12));
  }

  // ---------------------------------------------------------------------------------//
  public String getUserName ()
  // ---------------------------------------------------------------------------------//
  {
    return userName;
  }

  // ---------------------------------------------------------------------------------//
  public int getSize ()
  // ---------------------------------------------------------------------------------//
  {
    return size;
  }

  // ---------------------------------------------------------------------------------//
  public int getInit ()
  // ---------------------------------------------------------------------------------//
  {
    return init;
  }

  // ---------------------------------------------------------------------------------//
  public LocalDate getDateCreated ()
  // ---------------------------------------------------------------------------------//
  {
    return dateCreated;
  }

  // ---------------------------------------------------------------------------------//
  public LocalDate getDateModified ()
  // ---------------------------------------------------------------------------------//
  {
    return dateModified;
  }

  // ---------------------------------------------------------------------------------//
  public String getTime ()
  // ---------------------------------------------------------------------------------//
  {
    return time;
  }

  // ---------------------------------------------------------------------------------//
  public String getVersion ()
  // ---------------------------------------------------------------------------------//
  {
    if (vv == 0 || mm == 0)
      return "";
    return String.format ("%02d.%02d", vv, mm);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    String date1Text = dateCreated == null ? ""
        : String.format ("%td %<tb %<tY", dateCreated).replace (".", "");
    return String.format ("%s  %-8s  %-8s  %,6d  %s  %s", usesAlias ? "A" : ".", name,
        userName, size, date1Text, time);
  }
}
