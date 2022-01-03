package com.bytezone.xmit;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.bytezone.appbase.AppBase;

import javafx.scene.control.Alert.AlertType;

// https://en.wikipedia.org/wiki/EBCDIC_code_pages
// https://en.wikipedia.org/wiki/EBCDIC_1047

// -----------------------------------------------------------------------------------//
public class CodePage
// -----------------------------------------------------------------------------------//
{
  private static byte[] values;
  public final int[] ebc2asc = new int[256];
  public final int[] asc2ebc = new int[256];
  final String name;

  static
  {
    values = new byte[256];
    for (int i = 0; i < 256; i++)
      values[i] = (byte) i;
  }

  // ---------------------------------------------------------------------------------//
  public CodePage (String name)
  // ---------------------------------------------------------------------------------//
  {
    if (name.startsWith ("USER"))
    {
      int i = 0;
      for (String s : Utility.getLocalCodePage (name).split (" "))
        ebc2asc[i++] = Integer.parseInt (s, 16);
    }
    else
    {
      if (!Charset.isSupported (name))
      {
        System.out.printf ("Charset %s is not supported%n", name);
        name = "CP037";
      }
      try
      {
        int i = 0;
        for (char c : new String (values, name).toCharArray ())
        {
          //          if (c < 256)
          //            asc2ebc[c] = i;
          ebc2asc[i++] = c;
        }
      }
      catch (UnsupportedEncodingException e)
      {
        AppBase.showAlert (AlertType.ERROR, "Encoding Exception", e.toString ());
      }
    }

    for (int i = 0; i < 256; i++)
    {
      int j = ebc2asc[i];
      if (j < 256)              // CP870 requires this
        asc2ebc[j] = i;
    }
    this.name = name;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ("Name: " + name + "\n");

    for (int i = 0; i < 256; i += 32)
    {
      for (int j = 0; j < 32; j++)
        text.append (String.format ("%02X ", ebc2asc[i + j]));
      text.append ("\n");
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public static void main (String[] args)
  // ---------------------------------------------------------------------------------//
  {
    if (true)
    {
      list ();
    }
    else
    {
      String[] codePageNames = { "CP037", "CP273", "CP285", "CP297", "CP500", "CP1047", "USER1" };
      CodePage[] codePages = new CodePage[codePageNames.length];
      int count = 0;

      System.out.print ("   ");
      for (String codePageName : codePageNames)
      {
        System.out.printf (" %-6.6s", codePageNames[count]);
        codePages[count++] = new CodePage (codePageName);
      }
      System.out.println ();

      for (int i = 0; i < 256; i++)
      {
        if (allSame (codePages, i))
          continue;
        System.out.printf ("%02X:   ", i);
        for (CodePage codePage : codePages)
          System.out.printf ("%02X     ", codePage.ebc2asc[i]);
        if (codePages[5].ebc2asc[i] != codePages[6].ebc2asc[i])
          System.out.print (" **");
        System.out.println ();
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private static boolean allSame (CodePage[] codePages, int i)
  // ---------------------------------------------------------------------------------//
  {
    int base = codePages[0].ebc2asc[i];
    for (int j = 1; j < codePages.length; j++)
      if (codePages[j].ebc2asc[i] != base)
        return false;
    return true;
  }

  // ---------------------------------------------------------------------------------//
  private static boolean noChange (CodePage[] codePages, int i)
  // ---------------------------------------------------------------------------------//
  {
    for (CodePage codePage : codePages)
      if (codePage.ebc2asc[i] != i)
        return false;
    return true;
  }

  // ---------------------------------------------------------------------------------//
  private static void list ()
  // ---------------------------------------------------------------------------------//
  {
    System.out.println ("Canonical name   Display name   Can encode   Aliases");

    for (Charset charset : Charset.availableCharsets ().values ())
    {
      System.out.printf ("%-15s %-15s %-6s", charset.name (), charset.displayName (),
          charset.canEncode ());
      for (String aliasName : charset.aliases ())
        System.out.printf (" %-15s", aliasName);
      System.out.println ();
    }
  }
}
