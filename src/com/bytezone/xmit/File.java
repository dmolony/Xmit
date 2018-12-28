package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public class File
{
  int dataLength;
  final List<BlockPointerList> blockPointerLists = new ArrayList<> ();
  final List<String> lines = new ArrayList<> ();
  int lrecl;

  // ---------------------------------------------------------------------------------//
  // addBlockPointers
  // ---------------------------------------------------------------------------------//

  void addBlockPointers (BlockPointerList blockPointerList)
  {
    blockPointerLists.add (blockPointerList);
    dataLength += blockPointerList.getDataLength ();
  }
}
