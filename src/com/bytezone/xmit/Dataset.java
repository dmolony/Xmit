package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.Dsorg.Org;

public class Dataset
{
  final List<BlockPointerList> blockPointerLists = new ArrayList<> ();
  int lrecl;
  Org org;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  Dataset (Org org, int lrecl)
  {
    this.lrecl = lrecl;
    this.org = org;
  }

  // ---------------------------------------------------------------------------------//
  // add
  // ---------------------------------------------------------------------------------//

  void add (BlockPointerList blockPointerList)
  {
    blockPointerLists.add (blockPointerList);
    //    System.out.printf ("  adding to %s %d%n", org, lrecl);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("Dataset: %s %d", org, lrecl);
  }
}
