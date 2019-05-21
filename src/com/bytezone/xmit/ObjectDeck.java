package com.bytezone.xmit;

// -----------------------------------------------------------------------------------//
class ObjectDeck
// -----------------------------------------------------------------------------------//
{
  ObjectDeck (byte[] buffer, int lrecl)
  {
    int ptr = 0;
    while (ptr < buffer.length)
    {
      assert buffer[ptr] == 0x02;
      String recordType = Utility.getString (buffer, ptr + 1, 3);
      String deckId = Utility.getString (buffer, ptr + 72, 8);
      //      System.out.printf ("%s  %s%n", recordType, deckId);

      switch (recordType)
      {
        case "ESD":
          int fieldCount = Utility.getTwoBytes (buffer, ptr + 10) >>> 4;
          int something = Utility.getTwoBytes (buffer, ptr + 14);
          int p = ptr + 16;
          System.out.printf ("%s %s %02X%n", recordType, deckId, something);
          for (int i = 0; i < fieldCount; i++)
          {
            String name = Utility.getString (buffer, p, 8);
            int esdCode = buffer[p + 8] & 0xFF;
            int address = (int) Utility.getValue (buffer, p + 9, 3);
            int flag = buffer[p + 12] & 0xFF;
            int length = (int) Utility.getValue (buffer, p + 13, 3);
            System.out.printf ("        %s  %02X  %06X  %02X  %06X%n", name, esdCode,
                address, flag, length);
            p += 16;
          }
          break;

        case "TXT":
          int address = (int) Utility.getValue (buffer, ptr + 5, 3);
          int byteCount = (int) Utility.getValue (buffer, ptr + 10, 2);
          int esdid = Utility.getTwoBytes (buffer, ptr + 14);
          String text = Utility.getString (buffer, ptr + 16, byteCount);
          System.out.printf ("%s %s %06X %02X %02X %s%n", recordType, deckId, address,
              byteCount, esdid, text);
          break;

        case "RLD":
          fieldCount = Utility.getTwoBytes (buffer, ptr + 10);
          System.out.printf ("%s %s %04X %n", recordType, deckId, fieldCount);
          p = ptr + 16;
          while (fieldCount > 0)
          {
            int rel = Utility.getTwoBytes (buffer, p);
            int pos = Utility.getTwoBytes (buffer, p + 2);
            if (fieldCount > 4)
            {
              int flag = buffer[p + 4] & 0xFF;
              address = (int) Utility.getValue (buffer, p + 5, 3);
              System.out.printf ("             %04X %04X %02X %06X%n", rel, pos, flag,
                  address);
              fieldCount -= 8;
              p += 8;
            }
            else
            {
              System.out.printf ("             %04X %04X%n", rel, pos);
              fieldCount -= 4;
              p += 4;
            }
          }
          break;

        case "END":
          int entryAddress = (int) Utility.getValue (buffer, ptr + 5, 3);
          int entryPoint = Utility.getTwoBytes (buffer, ptr + 14);
          String name = Utility.getString (buffer, ptr + 16, 8);
          int idr = buffer[ptr + 32] & 0xFF;
          int idr2 = idr == 0x40 ? 0 : idr == 0xF1 ? 1 : 2;
          text = Utility.getString (buffer, ptr + 33, 19);
          System.out.printf ("%s %s %06X %04X %-8s %d %s %n", recordType, deckId,
              entryAddress, entryPoint, name, idr2, text);
          break;

        case "SYM":
          System.out.printf ("%s %s %n", recordType, deckId);
          break;

        default:
          System.out.println ("Unknown record type: " + recordType);
      }
      ptr += lrecl;
    }
  }
}
