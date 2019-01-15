package com.bytezone.xmit;

import java.time.LocalDate;
import java.util.Optional;

public class CatalogEntry
{
  final Reader reader;
  private Member member;                                  // contains DataBlocks

  private final String name;
  private String userName = "";
  private String aliasName = "";

  private int size;
  private int init;
  private int mod;

  private int vv;
  private int mm;

  private LocalDate dateCreated;
  private LocalDate dateModified;
  private String time = "";

  private final byte[] directoryData;
  private final int extra;

  private final int blockFrom;
  private byte[] ttl = new byte[5];

  private CopyR1 copyR1;
  private CopyR2 copyR2;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CatalogEntry (Reader reader, byte[] buffer, int ptr)
  {
    name = Utility.getString (buffer, ptr, 8);
    blockFrom = (int) Utility.getValue (buffer, ptr + 8, 3);
    this.reader = reader;

    extra = buffer[ptr + 11] & 0xFF;

    switch (extra)
    {
      case 0x0F:
        basic (buffer, ptr);
        break;

      case 0x14:
        basic (buffer, ptr);
        break;

      case 0x2B:                    //
        break;

      case 0x2C:                    // FILE035 - load module?
        break;

      case 0x2E:                    // FILE035
        break;

      case 0x31:                    // FILE242
        //        dateCreated = Utility.getLocalDate (buffer, ptr + 34);  NFE
        break;

      case 0x36:                    // file242    // 0xB6 alias of itself?
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0x37:                    // FILE135
        String someName = Utility.getString (buffer, ptr + 46, 8);
        break;

      case 0x8F:                    // FILE134/REVHELP
        basic (buffer, ptr);       // alias without the alias' name ??
        break;

      case 0xB1:                    // alias of 0x2C
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0xB3:                    // FILE035
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0xB6:      // file242    // alias of 0x31
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        Optional<LocalDate> opt = Utility.getLocalDate (buffer, ptr + 44);
        if (opt.isPresent ())
          dateCreated = opt.get ();
        break;

      case 0xD3:
        System.out.printf ("%02X  %s  %s%n", extra, reader.getName (), name);
        aliasName = Utility.getString (buffer, ptr + 36, 8);
        break;

      case 0x4E:
        System.out.printf ("%02X  %s  %s%n", extra, reader.getName (), name);
        break;

      case 0xCB:
        System.out.printf ("%02X  %s  %s%n", extra, reader.getName (), name);
        break;

      case 0x4B:
        System.out.printf ("%02X  %s  %s%n", extra, reader.getName (), name);
        break;

      case 0:
        break;

      default:
        System.out.printf ("********************** Unknown extra: %02X in %s%n", extra,
            name);
    }

    int extraLength = 12 + (extra & 0x0F) * 2 + ((extra & 0x10) >> 4) * 32;
    directoryData = new byte[extraLength];
    System.arraycopy (buffer, ptr, directoryData, 0, directoryData.length);
  }

  // ---------------------------------------------------------------------------------//
  // debugLine
  // ---------------------------------------------------------------------------------//

  public String debugLine ()
  {
    String hex = "";
    String t1 = "";

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

    return String
        .format ("%02X %-8s %-8s %06X %-129s %8s %8s", directoryData[11],
            getMemberName (), getUserName (), blockFrom, hex, getAliasName (), t1)
        .trim ();
  }

  // ---------------------------------------------------------------------------------//
  // basic
  // ---------------------------------------------------------------------------------//

  private void basic (byte[] buffer, int offset)
  {
    userName = Utility.getString (buffer, offset + 32, 8);
    size = Utility.getTwoBytes (buffer, offset + 26);
    init = Utility.getTwoBytes (buffer, offset + 28);
    mod = Utility.getTwoBytes (buffer, offset + 30);

    vv = buffer[offset + 12] & 0xFF;
    mm = buffer[offset + 13] & 0xFF;

    Optional<LocalDate> opt = Utility.getLocalDate (buffer, offset + 16);
    if (opt.isPresent ())
      dateCreated = opt.get ();

    opt = Utility.getLocalDate (buffer, offset + 20);
    if (opt.isPresent ())
      dateModified = opt.get ();

    time = String.format ("%02X:%02X:%02X", buffer[offset + 24], buffer[offset + 25],
        buffer[offset + 15]);

    if (false)
    {
      String vvmmText = String.format ("%02d.%02d", vv, mm);
      String date1Text = String.format ("%td %<tb %<tY", dateCreated).replace (".", "");
      String date2Text = String.format ("%td %<tb %<tY", dateModified).replace (".", "");
      System.out.println (String.format ("%-8s  %6d  %6d %4d  %13s  %13s  %s  %5s  %s",
          name, size, init, mod, date1Text, date2Text, time, vvmmText, userName));
    }
  }

  // ---------------------------------------------------------------------------------//
  // setMember
  // ---------------------------------------------------------------------------------//

  void setMember (Member member)
  {
    this.member = member;
  }

  // ---------------------------------------------------------------------------------//
  // getMember
  // ---------------------------------------------------------------------------------//

  public Member getMember ()
  {
    return member;
  }

  // ---------------------------------------------------------------------------------//
  // setCopyRecords
  // ---------------------------------------------------------------------------------//

  void setCopyRecords (CopyR1 copyR1, CopyR2 copyR2)
  {
    //    isPdse = copyR1.isPdse ();
    this.copyR1 = copyR1;
    this.copyR2 = copyR2;
  }

  // ---------------------------------------------------------------------------------//
  // not currently used
  // ---------------------------------------------------------------------------------//

  private long nothing ()
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
      //      break;
    }
    else
    {
      System.out.printf (": Out of range%n");
    }
    //    }

    return Utility.getValue (ttl, 0, 5);
  }

  // ---------------------------------------------------------------------------------//
  // convert
  // ---------------------------------------------------------------------------------//

  private byte[] convert (int index, int mult)
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
  // length
  // ---------------------------------------------------------------------------------//

  int length ()
  {
    return directoryData.length;
  }

  // ---------------------------------------------------------------------------------//
  // getMemberName
  // ---------------------------------------------------------------------------------//

  public String getMemberName ()
  {
    return name;
  }

  // ---------------------------------------------------------------------------------//
  // getUserName
  // ---------------------------------------------------------------------------------//

  public String getUserName ()
  {
    return userName;
  }

  // ---------------------------------------------------------------------------------//
  // isAlias
  // ---------------------------------------------------------------------------------//

  public boolean isAlias ()
  {
    return !aliasName.isEmpty ();
  }

  // ---------------------------------------------------------------------------------//
  // getAliasName
  // ---------------------------------------------------------------------------------//

  public String getAliasName ()
  {
    return aliasName;
  }

  // ---------------------------------------------------------------------------------//
  // getSize
  // ---------------------------------------------------------------------------------//

  public int getSize ()
  {
    return size;
  }

  // ---------------------------------------------------------------------------------//
  // getInit
  // ---------------------------------------------------------------------------------//

  public int getInit ()
  {
    return init;
  }

  // ---------------------------------------------------------------------------------//
  // getDateCreated
  // ---------------------------------------------------------------------------------//

  public LocalDate getDateCreated ()
  {
    return dateCreated;
  }

  // ---------------------------------------------------------------------------------//
  // getDateModified
  // ---------------------------------------------------------------------------------//

  public LocalDate getDateModified ()
  {
    return dateModified;
  }

  // ---------------------------------------------------------------------------------//
  // getTime
  // ---------------------------------------------------------------------------------//

  public String getTime ()
  {
    return time;
  }

  // ---------------------------------------------------------------------------------//
  // getVersion
  // ---------------------------------------------------------------------------------//

  public String getVersion ()
  {
    if (vv == 0 & mm == 0)
      return "";
    return String.format ("%02d.%02d", vv, mm);
  }

  // ---------------------------------------------------------------------------------//
  // getOffset
  // ---------------------------------------------------------------------------------//

  public int getOffset ()
  {
    return blockFrom;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    String date1Text = dateCreated == null ? ""
        : String.format ("%td %<tb %<tY", dateCreated).replace (".", "");
    return String.format ("%8s  %8s  %,6d  %s  %s  %8s", name, userName, size, date1Text,
        time, aliasName);
  }
}
