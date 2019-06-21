package com.bytezone.xmit.textunit;

// -----------------------------------------------------------------------------------//
public class Member extends TextUnit
// -----------------------------------------------------------------------------------//
{
  public final String datasetName;

  // ---------------------------------------------------------------------------------//
  public Member (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    super (buffer, ptr);

    StringBuilder text = new StringBuilder ();
    for (int i = 0; i < dataList.size (); i++)
    {
      text.append (dataList.get (i).text);
      text.append (", ");
    }
    text.deleteCharAt (text.length () - 1);
    text.deleteCharAt (text.length () - 1);
    datasetName = text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%04X  %-8s  %s", keys[keyId], mnemonics[keyId], datasetName);
  }
}
