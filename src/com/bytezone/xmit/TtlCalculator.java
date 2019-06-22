package com.bytezone.xmit;

// -----------------------------------------------------------------------------------//
class TtlCalculator          // not used
// -----------------------------------------------------------------------------------//
{
  private int ttr;

  // not used
  private byte[] ttl = new byte[5];
  private CopyR1 copyR1;
  private CopyR2 copyR2;

  // ---------------------------------------------------------------------------------//
  void setCopyRecords (CopyR1 copyR1, CopyR2 copyR2)
  // ---------------------------------------------------------------------------------//
  {
    this.copyR1 = copyR1;
    this.copyR2 = copyR2;
  }

  // ---------------------------------------------------------------------------------//
  long notUsed ()
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
}
