package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

class TextUnit
{
  static final int INMBLKSZ = 0x0030;
  static final int INMCREAT = 0x1022;
  static final int INMDDNAM = 0x0001;
  static final int INMDIR = 0x000C;
  static final int INMDSNAM = 0x0002;
  static final int INMDSORG = 0x003C;
  static final int INMEATTR = 0x8028;
  static final int INMERRCD = 0x1027;
  static final int INMEXPDT = 0x0022;

  static final int INMFACK = 0x1026;
  static final int INMFFM = 0x102D;
  static final int INMFNODE = 0x1011;
  static final int INMFTIME = 0x1024;
  static final int INMFUID = 0x1012;
  static final int INMFVERS = 0x1023;
  static final int INMLCHG = 0x1021;
  static final int INMLRECL = 0x0042;
  static final int INMLREF = 0x1020;

  static final int INMLSIZE = 0x8018;
  static final int INMMEMBR = 0x0003;
  static final int INMNUMF = 0x102F;
  static final int INMRECCT = 0x102A;
  static final int INMRECFM = 0x0049;
  static final int INMSECND = 0x000B;
  static final int INMSIZE = 0x102C;
  static final int INMTERM = 0x0028;
  static final int INMTNODE = 0x1001;

  static final int INMTTIME = 0x1025;
  static final int INMTUID = 0x1002;
  static final int INMTYPE = 0x8012;
  static final int INMUSERP = 0x1029;
  static final int INMUTILN = 0x1028;

  static int[] keys = { 0x0000, INMBLKSZ, INMCREAT, INMDDNAM, INMDIR, INMDSNAM, INMDSORG,
                        INMEATTR, INMERRCD, INMEXPDT, INMFACK, INMFFM, INMFNODE, INMFTIME,
                        INMFUID, INMFVERS, INMLCHG, INMLRECL, INMLREF, INMLSIZE, INMMEMBR,
                        INMNUMF, INMRECCT, INMRECFM, INMSECND, INMSIZE, INMTERM, INMTNODE,
                        INMTTIME, INMTUID, INMTYPE, INMUSERP, INMUTILN };
  static String[] mnemonics =
      { "NONE", "INMBLKSZ", "INMCREAT", "INMDDNAM", "INMDIR", "INMDSNAM", "INMDSORG",
        "INMEATTR", "INMERRCD", "INMEXPDT", "INMFACK", "INMFFM", "INMFNODE", "INMFTIME",
        "INMFUID", "INMFVERS", "INMLCHG", "INMLRECL", "INMLREF", "INMLSIZE", "INMMEMBR",
        "INMNUMF", "INMRECCT", "INMRECFM", "INMSECND", "INMSIZE", "INMTERM", "INMTNODE",
        "INMTTIME", "INMTUID", "INMTYPE", "INMUSERP", "INMUTILN" };
  static String[] descriptions =
      { "None", "Block size", "Creation date", "DDNAME for the file",
        "Number of directory blocks", "Name of the file", "File organization",
        "Extended attribute status", "RECEIVE command error code", "Expiration date",
        "Originator requested notification", "Filemode number",
        "Origin node name or node number", "Origin timestamp", "Origin user ID",
        "Origin version number of the data format", "Date last changed",
        "Logical record length", "Date last referenced", "Data set size in megabytes",
        "Member name list", "Number of files transmitted", "Transmitted record count",
        "Record format", "Secondary space quantity", "File size in bytes",
        "Data transmitted as a message", "Target node name or node number",
        "Destination timestamp", "Target user ID", "Data set type",
        "User parameter string", "Name of utility program" };

  int keyId;
  List<Data> dataList;
  int length;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public TextUnit (byte[] buffer, int ptr)
  {
    int key = Reader.getWord (buffer, ptr);
    int number = Reader.getWord (buffer, ptr + 2);
    dataList = new ArrayList<> (number);

    ptr += 4;
    for (int i = 0; i < number; i++)
    {
      Data data = new Data (buffer, ptr);
      dataList.add (data);
      ptr += data.length + 2;
      length += data.length + 2;
    }

    for (int i = 1; i < keys.length; i++)
      if (key == keys[i])
      {
        keyId = i;
        break;
      }

    if (keyId == 0)
      System.out.printf ("Unknown key: %04X%n", key);
  }

  // ---------------------------------------------------------------------------------//
  // dump
  // ---------------------------------------------------------------------------------//

  static void dump ()
  {
    for (int i = 0; i < keys.length; i++)
      System.out.printf ("%04X  %-8s  %s%n", keys[i], mnemonics[i], descriptions[i]);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("%-8s  %04X", mnemonics[keyId], dataList.size ()));

    for (Data data : dataList)
      text.append (String.format ("  %s", data) + ",");

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}
