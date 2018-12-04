package com.bytezone.xmit.textunit;

public class ImdDsnam extends TextUnit
{
  final String datasetName;

  public ImdDsnam (byte[] buffer, int ptr)
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
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%-8s  %s", mnemonics[keyId], datasetName);
  }
}
