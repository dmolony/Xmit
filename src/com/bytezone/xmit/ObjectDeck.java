package com.bytezone.xmit;

public class ObjectDeck
{
  public ObjectDeck (byte[] buffer, int lrecl)
  {
    int ptr = 0;
    while (ptr < buffer.length)
    {
      assert buffer[ptr] == 0x02;
      String recordType = Utility.getString (buffer, ptr + 1, 3);
      String deckId = Utility.getString (buffer, ptr + 72, 8);
      System.out.printf ("%s  %s%n", recordType, deckId);

      switch (recordType)
      {
        case "ESD":
          int fieldCount = Utility.getTwoBytes (buffer, ptr + 10) >>> 4;
          int something = Utility.getTwoBytes (buffer, ptr + 14);
          int p = ptr + 16;
          for (int i = 0; i < fieldCount; i++)
          {
            String name = Utility.getString (buffer, p, 8);
            int esdCode = buffer[p + 8] & 0xFF;
            int address = (int) Utility.getValue (buffer, p + 9, 3);
            int flag = buffer[p + 12] & 0xFF;
            int length = (int) Utility.getValue (buffer, p + 13, 3);
            System.out.printf ("%s  %02X  %06X  %02X  %06X%n", name, esdCode, address,
                flag, length);
            p += 16;
          }
          break;

        case "TXT":
          int address = (int) Utility.getValue (buffer, ptr + 5, 3);
          int byteCount = (int) Utility.getValue (buffer, ptr + 10, 2);
          int esdid = Utility.getTwoBytes (buffer, ptr + 14);
          String text = Utility.getString (buffer, ptr + 16, byteCount);
          System.out.println (text);
          break;
        case "RLD":
          break;
        case "END":
          break;
        case "SYM":
          break;
        default:
          System.out.println ("Unknown record type: " + recordType);
      }
      ptr += lrecl;
    }
  }
}
