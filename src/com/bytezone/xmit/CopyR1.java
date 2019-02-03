package com.bytezone.xmit;

import java.time.LocalDate;

// https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.1.0/com.ibm.zos.v2r1.idau100/u1322.htm

// ---------------------------------------------------------------------------------//
class CopyR1
//---------------------------------------------------------------------------------//
{
  private static final byte[] header = { (byte) 0xCA, 0x6D, 0x0F };
  private static String[] recfmTypes = { "?", "V", "F", "U" };
  private final byte[] buffer;

  private final byte unloadFlags;
  private final int blksize;
  private final int reclen;
  private final byte recfm;
  private final byte keylen;
  private final byte optcd;
  private final byte smsfg;
  private final int containerBlksize;
  private final int headerRecords;
  private final byte zero;
  private final LocalDate date;
  private final int year;
  private final int julian;
  private final int scext;
  private final int scalo;
  private final int lstar;
  private final int trbal;
  private final int zero2;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  CopyR1 (byte[] buffer)
  {
    this.buffer = buffer;

    unloadFlags = buffer[0];
    assert Utility.matches (header, buffer, 1);
    blksize = Utility.getTwoBytes (buffer, 6);
    reclen = Utility.getTwoBytes (buffer, 8);
    recfm = buffer[10];
    keylen = buffer[11];
    optcd = buffer[12];
    smsfg = buffer[13];
    containerBlksize = Utility.getTwoBytes (buffer, 14);
    headerRecords = Utility.getTwoBytes (buffer, 36);
    zero = buffer[38];
    year = buffer[39] & 0xFF;
    julian = Utility.getTwoBytes (buffer, 40);
    date = year > 0 && julian > 0 ? LocalDate.ofYearDay (year + 1900, julian) : null;
    scext = (int) Utility.getValue (buffer, 42, 3);
    scalo = (int) Utility.getFourBytes (buffer, 45);
    lstar = (int) Utility.getValue (buffer, 49, 3);

    if (buffer.length > 52)       // FILE776.XMI/XMCLOAD
    {
      trbal = Utility.getTwoBytes (buffer, 52);
      zero2 = Utility.getTwoBytes (buffer, 54);
    }
    else
    {
      trbal = 0;
      zero2 = 0;
    }
  }

  // ---------------------------------------------------------------------------------//
  // getRecfm
  // ---------------------------------------------------------------------------------//

  String getRecfm ()
  {
    String recfmText = recfmTypes[(recfm & 0xC0) >>> 6];
    if ((recfm & 0x10) != 0)
      recfmText += "B";
    int carriageControl = (recfm & 0x06) >> 1;
    if (carriageControl == 1 || carriageControl == 2)
      recfmText += "A";
    return recfmText;
  }

  // ---------------------------------------------------------------------------------//
  // isPdse
  // ---------------------------------------------------------------------------------//

  boolean isPdse ()
  {
    return (unloadFlags & 0x01) != 0;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    String flagsText = isPdse () ? "PDSE" : "PDS";

    text.append (
        String.format ("Unld flags ...... %02X    %s%n", unloadFlags, flagsText));
    text.append (String.format ("Block size ...... %04X  %<,7d%n", blksize));
    text.append (String.format ("Record length.... %04X  %<,7d%n", reclen));
    text.append (String.format ("Record format ... %02X    %s%n", recfm, getRecfm ()));
    text.append (String.format ("Key length ...... %02X%n", keylen));
    text.append (String.format ("OPTCD ........... %02X%n", optcd));
    text.append (String.format ("SMSFG ........... %02X%n", smsfg));
    text.append (String.format ("C Block size .... %04X  %<,7d%n", containerBlksize));
    text.append (
        String.format ("Dev type ........ %s%n", Utility.getHexValues (buffer, 16, 20)));
    text.append (String.format ("Header records .. %04X  %<,7d%n", headerRecords));
    text.append (String.format ("Zero ............ %02X%n", zero));
    text.append (String.format ("Date ............ %s%n", date == null ? "" : date));
    text.append (String.format ("Scnd space ...... %06X  %n", scext));
    text.append (String.format ("Scnd alloc ...... %08X  %n", scalo));
    text.append (String.format ("Last trk used ... %06X  %n", lstar));
    text.append (String.format ("Last trk bal .... %04X  %<,7d%n", trbal));
    text.append (String.format ("Zero ............ %02X", zero2));

    return text.toString ();
  }
}
