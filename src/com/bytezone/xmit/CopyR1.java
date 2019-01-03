package com.bytezone.xmit;

public class CopyR1
{
  private static final byte[] header = { (byte) 0xCA, 0x6D, 0x0F };
  private final byte[] buffer;

  private final byte unloadFlags;
  private final int blksize;
  private final int reclen;
  private final byte recfm;
  private final byte keylen;
  private final byte optcd;
  private final byte msfg;
  private final int containerBlksize;
  private final int headerRecords;
  private final byte zero;
  private final int date;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CopyR1 (byte[] buffer)
  {
    this.buffer = buffer;

    unloadFlags = buffer[0];
    assert Utility.matches (header, buffer, 1);
    blksize = Utility.getTwoBytes (buffer, 6);
    reclen = Utility.getTwoBytes (buffer, 8);
    recfm = buffer[10];
    keylen = buffer[11];
    optcd = buffer[12];
    msfg = buffer[13];
    containerBlksize = Utility.getTwoBytes (buffer, 14);
    headerRecords = Utility.getTwoBytes (buffer, 36);
    zero = buffer[38];
    date = (int) Utility.getValue (buffer, 39, 3);

    System.out.println (this);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    String flagsText = (unloadFlags & 0x01) != 0 ? "PDSE" : "PDS";

    text.append (
        String.format ("Unld flags ...... %02X    %s%n", unloadFlags, flagsText));
    text.append (String.format ("Block size ...... %04X  %<,7d%n", blksize));
    text.append (String.format ("Record length.... %04X  %<,7d%n", reclen));
    text.append (String.format ("Record format ... %02X%n", recfm));
    text.append (String.format ("Key length ...... %02X%n", keylen));
    text.append (String.format ("Opt code ........ %02X%n", optcd));
    text.append (String.format ("Msfg ............ %02X%n", msfg));
    text.append (String.format ("C Block size .... %04X  %<,7d%n", containerBlksize));
    text.append (
        String.format ("Dev type ........ %s%n", Utility.getHexValues (buffer, 16, 20)));
    text.append (String.format ("Header records .. %04X  %<,7d%n", headerRecords));
    text.append (String.format ("Zero ............ %02X%n", zero));
    text.append (String.format ("Date ............ %06X  %<,7d%n", date));

    return text.toString ();
  }
}
