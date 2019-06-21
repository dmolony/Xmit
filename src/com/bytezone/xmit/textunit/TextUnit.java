package com.bytezone.xmit.textunit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.Utility;

// -----------------------------------------------------------------------------------//
public class TextUnit
// -----------------------------------------------------------------------------------//
{
  public static final int INMBLKSZ = 0x0030;
  public static final int INMCREAT = 0x1022;
  public static final int INMDDNAM = 0x0001;
  public static final int INMDIR = 0x000C;          // no of directory blocks
  public static final int INMDSNAM = 0x0002;
  public static final int INMDSORG = 0x003C;
  public static final int INMEATTR = 0x8028;        // Extended attribute status
  public static final int INMERRCD = 0x1027;
  public static final int INMEXPDT = 0x0022;

  public static final int INMFACK = 0x1026;
  public static final int INMFFM = 0x102D;
  public static final int INMFNODE = 0x1011;
  public static final int INMFTIME = 0x1024;
  public static final int INMFUID = 0x1012;
  public static final int INMFVERS = 0x1023;
  public static final int INMLCHG = 0x1021;
  public static final int INMLRECL = 0x0042;
  public static final int INMLREF = 0x1020;

  public static final int INMLSIZE = 0x8018;        // size of the file in MB
  public static final int INMMEMBR = 0x0003;        // Member name list
  public static final int INMNUMF = 0x102F;
  public static final int INMRECCT = 0x102A;
  public static final int INMRECFM = 0x0049;
  public static final int INMSECND = 0x000B;
  public static final int INMSIZE = 0x102C;         // size of the file in bytes
  public static final int INMTERM = 0x0028;
  public static final int INMTNODE = 0x1001;

  public static final int INMTTIME = 0x1025;
  public static final int INMTUID = 0x1002;
  public static final int INMTYPE = 0x8012;         // dataset type
  public static final int INMUSERP = 0x1029;
  public static final int INMUTILN = 0x1028;

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

  // INMTYPE
  // X'80' Data library
  // X'40' Program library
  // X'04' Extended format sequential data set
  // X'01' Large format sequential data set

  // ---------------------------------------------------------------------------------//
  public TextUnit (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    int key = Utility.getTwoBytes (buffer, ptr);
    int number = Utility.getTwoBytes (buffer, ptr + 2);
    dataList = new ArrayList<> (number);
    //    System.out.println (Utility.getHexDump (buffer, ptr, 20));

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
      System.out.printf ("Unknown key:: %04X%n", key);
  }

  String getString ()
  {
    return "";
  }

  long getNumber ()
  {
    return -1;
  }

  // ---------------------------------------------------------------------------------//
  public boolean matches (int keyId)
  // ---------------------------------------------------------------------------------//
  {
    return keys[this.keyId] == keyId;
  }

  // ---------------------------------------------------------------------------------//
  private boolean findKey (int keyId)
  // ---------------------------------------------------------------------------------//
  {
    for (int key : keys)
      if (key == keyId)
        return true;
    return false;
  }

  // ---------------------------------------------------------------------------------//
  public int length ()
  // ---------------------------------------------------------------------------------//
  {
    return length;
  }

  // ---------------------------------------------------------------------------------//
  static void dump ()
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < keys.length; i++)
      System.out.printf ("%04X  %-8s  %s%n", keys[i], mnemonics[i], descriptions[i]);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("%04X  %-8s  %04X", keys[keyId], mnemonics[keyId],
        dataList.size ()));

    for (Data data : dataList)
      text.append (String.format ("  %s", data) + ",");

    if (dataList.size () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}
