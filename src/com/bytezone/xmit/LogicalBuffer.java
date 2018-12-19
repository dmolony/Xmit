package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class LogicalBuffer
{
  List<BlockPointerList> blockPointerLists = new ArrayList<> ();
  byte[] buffer;

  void addBlockPointerList (BlockPointerList blockPointerList)
  {
    blockPointerLists.add (blockPointerList);
    if (buffer == null)
      buffer = blockPointerList.blockPointers.get (0).buffer;
  }

  void walk ()
  {
    for (BlockPointerList blockPointerList : blockPointerLists)
    {
      //      System.out.println (blockPointerList);
      int ptr = 0;
      int currentBlockNo = 0;
      int bytesLeft = 0;

      while (currentBlockNo < blockPointerList.blockPointers.size ())
      {
        BlockPointer blockPointer = blockPointerList.blockPointers.get (currentBlockNo++);
        ptr = blockPointer.offset;
        int max = ptr + blockPointer.length;

        System.out.println ("New block pointer");
        System.out.printf ("ptr=%06X  rem=%06X  max=%06X%n", ptr, bytesLeft, max);
        while (ptr < max)
        {
          if (bytesLeft == 0)             // new header
          {
            System.out.println ("New header");
            System.out.println (Utility.getHexDump (buffer, ptr, 12));
            bytesLeft = Utility.getWord (buffer, ptr + 10);     // bytes left to use
            ptr += 12;
            System.out.printf ("ptr=%06X  rem=%06X  max=%06X%n", ptr, bytesLeft, max);
          }

          if (ptr + bytesLeft <= max)
          {
            System.out.println ("Full record");
            System.out.println (Utility.getHexDump (buffer, ptr, bytesLeft));
            ptr += bytesLeft;
            bytesLeft = 0;
            System.out.printf ("ptr=%06X  rem=%06X  max=%06X%n", ptr, bytesLeft, max);
          }
          else
          {
            System.out.println ("Partial record");
            System.out.println (Utility.getHexDump (buffer, ptr, max - ptr));
            bytesLeft -= (max - ptr);
            ptr = max;
            System.out.printf ("ptr=%06X  rem=%06X  max=%06X%n", ptr, bytesLeft, max);
          }

        }
      }
    }
  }
}
