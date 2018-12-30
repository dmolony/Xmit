package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.Dsorg.Org;

public abstract class Dataset
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

  abstract void process ();

  // ---------------------------------------------------------------------------------//
  // add
  // ---------------------------------------------------------------------------------//

  void add (BlockPointerList blockPointerList)
  {
    blockPointerLists.add (blockPointerList);
    //    System.out.printf ("  adding to %s %d%n", org, lrecl);
  }

  // ---------------------------------------------------------------------------------//
  // getOrg
  // ---------------------------------------------------------------------------------//

  public Org getOrg ()
  {
    return org;
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
