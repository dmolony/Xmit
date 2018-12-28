package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

public abstract class Dataset
{
  final List<BlockPointerList> blockPointerLists = new ArrayList<> ();
  final List<String> lines = new ArrayList<> ();
  int lrecl;

  abstract byte[] getDataBuffer ();
}
