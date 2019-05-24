package com.bytezone.xmit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.Dsorg.Org;

// https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.1.0/com.ibm.zos.v2r1.idau100/u1322.htm

// -----------------------------------------------------------------------------------//
public class CopyR1
// -----------------------------------------------------------------------------------//
{
  private static final byte[] header = { (byte) 0xCA, 0x6D, 0x0F };
  private static String[] recfmTypes = { "?", "V", "F", "U" };
  private final byte[] buffer;

  private final byte unloadFlags;
  private final int dsorg;
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

  private final boolean managed;
  private final boolean reblockable;
  private final boolean pdse;

  private final Disposition disposition;

  // ---------------------------------------------------------------------------------//
  CopyR1 (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    this.buffer = buffer;

    unloadFlags = buffer[0];
    assert Utility.matches (header, buffer, 1);
    dsorg = Utility.getTwoBytes (buffer, 4);                  // 0x0200 = PDS
    blksize = Utility.getTwoBytes (buffer, 6);
    reclen = Utility.getTwoBytes (buffer, 8);
    recfm = buffer[10];
    keylen = buffer[11];
    optcd = buffer[12];
    smsfg = buffer[13];                                       // includes PDSE flag
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

    managed = (smsfg & 0x80) != 0;
    reblockable = (smsfg & 0x20) != 0;
    pdse = (smsfg & 0x08) != 0;

    disposition = new Disposition (Org.PDS, (recfm & 0xFF) << 8, reclen, blksize);
  }

  // ---------------------------------------------------------------------------------//
  String getRecfm ()
  // ---------------------------------------------------------------------------------//
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
  Disposition getDisposition ()
  // ---------------------------------------------------------------------------------//
  {
    return disposition;
  }

  // ---------------------------------------------------------------------------------//
  boolean isPdse ()
  // ---------------------------------------------------------------------------------//
  {
    return (unloadFlags & 0x01) != 0;
  }

  // ---------------------------------------------------------------------------------//
  public List<String> toLines ()
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = new ArrayList<> ();
    String flagsText = isPdse () ? "PDSE" : "PDS";

    lines.add ("-----------------------------------------------------------");
    lines.add (String.format ("Unld flags ...... %02X     %s", unloadFlags, flagsText));
    lines.add (String.format ("Dsorg ........... %04X", dsorg));
    lines.add (String.format ("Block size ...... %04X  %<,7d", blksize));
    lines.add (String.format ("Record length.... %04X  %<,7d", reclen));
    lines.add (String.format ("Record format ... %02X     %s", recfm, getRecfm ()));
    lines.add (String.format ("Key length ...... %02X", keylen));
    lines.add (String.format ("OPTCD ........... %02X", optcd));
    lines.add (String.format ("SMSFG ........... %02X", smsfg));
    lines.add (String.format ("C Block size .... %04X  %<,7d", containerBlksize));
    lines.add (
        String.format ("Dev type ........ %s", Utility.getHexValues (buffer, 16, 20)));
    lines.add (String.format ("Header records .. %04X  %<,7d", headerRecords));
    lines.add (String.format ("Zero ............ %02X", zero));
    lines.add (String.format ("Date ............ %s", date == null ? "" : date));
    lines.add (String.format ("Scnd space ...... %06X", scext));
    lines.add (String.format ("Scnd alloc ...... %08X", scalo));
    lines.add (String.format ("Last trk used ... %06X", lstar));
    lines.add (String.format ("Last trk bal .... %04X  %<,7d", trbal));
    lines.add (String.format ("Zero ............ %02X", zero2));
    lines.add ("-----------------------------------------------------------");

    return lines;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    for (String line : toLines ())
    {
      text.append (line);
      text.append ("\n");
    }

    return text.toString ();
  }
}
