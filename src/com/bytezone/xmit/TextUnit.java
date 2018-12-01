package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.common.Utility;

class TextUnit
{
  static int[] keys =
      { 0x0000, 0x0030, 0x1022, 0x0001, 0x000C, 0x0002, 0x003C, 0x8028, 0x1027, 0x0022,
        0x1026, 0x102D, 0x1011, 0x1024, 0x1012, 0x1023, 0x1021, 0x0042, 0x1020, 0x8018,
        0x0003, 0x102F, 0x102A, 0x0049, 0x000B, 0x102C, 0x0028, 0x1001, 0x1025, 0x1002,
        0x8012, 0x1029, 0x1028 };
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
  List<Data> dataList = new ArrayList<> ();
  int length;

  public TextUnit (byte[] buffer, int ptr)
  {
    int key = Reader.getWord (buffer, ptr);
    int number = Reader.getWord (buffer, ptr + 2);

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

  static void dump ()
  {
    for (int i = 0; i < keys.length; i++)
      System.out.printf ("%04X  %-8s  %s%n", keys[i], mnemonics[i], descriptions[i]);
  }

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

  class Data
  {
    int length;
    byte[] data;
    String text;
    boolean printable = true;

    Data (byte[] buffer, int ptr)
    {
      length = Reader.getWord (buffer, ptr);
      data = new byte[length];
      System.arraycopy (buffer, ptr + 2, data, 0, length);

      for (byte b : data)
        if ((b & 0xFF) <= 0x3F)
        {
          printable = false;
          break;
        }
      text = printable ? Reader.getString (data, 0, length) : "";
    }

    @Override
    public String toString ()
    {
      return String.format ("%04X %s : %s", length, Utility.getHex (data, 0, data.length),
          text);
    }
  }
}
