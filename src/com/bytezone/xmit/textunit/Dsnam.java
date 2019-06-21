package com.bytezone.xmit.textunit;

// -----------------------------------------------------------------------------------//
public class Dsnam extends TextUnitString
// -----------------------------------------------------------------------------------//
{
  public final String datasetName;

  // ---------------------------------------------------------------------------------//
  public Dsnam (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    super (buffer, ptr);

    StringBuilder text = new StringBuilder ();
    for (int i = 0; i < dataList.size (); i++)
    {
      text.append (dataList.get (i).text);
      text.append (".");
    }
    text.deleteCharAt (text.length () - 1);
    datasetName = text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getString ()
  // ---------------------------------------------------------------------------------//
  {
    return datasetName;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%04X  %-8s  %s", keys[keyId], mnemonics[keyId], datasetName);
  }
}
