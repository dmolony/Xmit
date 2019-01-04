package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg;
import com.bytezone.xmit.textunit.Dsorg.Org;
import com.bytezone.xmit.textunit.Recfm;
import com.bytezone.xmit.textunit.TextUnit;
import com.bytezone.xmit.textunit.TextUnitNumber;

public abstract class Dataset
{
  Reader reader;
  ControlRecord inmr02;

  int lrecl;
  Org dsorg;
  int recfm;

  final List<BlockPointerList> blockPointerLists = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  Dataset (Reader reader, ControlRecord inmr02)
  {
    this.reader = reader;
    this.inmr02 = inmr02;

    this.lrecl =
        (int) ((TextUnitNumber) inmr02.getTextUnit (TextUnit.INMLRECL)).getNumber ();
    this.dsorg = ((Dsorg) inmr02.getTextUnit (TextUnit.INMDSORG)).type;
    this.recfm = (int) ((Recfm) inmr02.getTextUnit (TextUnit.INMRECFM)).getNumber ();
  }

  abstract void process ();

  // ---------------------------------------------------------------------------------//
  // add
  // ---------------------------------------------------------------------------------//

  void addBlockPointerList (BlockPointerList blockPointerList)
  {
    blockPointerLists.add (blockPointerList);
  }

  // ---------------------------------------------------------------------------------//
  // getBlockPointerList
  // ---------------------------------------------------------------------------------//

  public BlockPointerList getBlockPointerList (int index)
  {
    return blockPointerLists.get (index);
  }

  // ---------------------------------------------------------------------------------//
  // getOrg
  // ---------------------------------------------------------------------------------//

  public Org getOrg ()
  {
    return dsorg;
  }

  // ---------------------------------------------------------------------------------//
  // getControlRecord
  // ---------------------------------------------------------------------------------//

  public ControlRecord getControlRecord ()
  {
    return inmr02;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%-20s %-3s %,6d  %04X", reader.getFileName (), dsorg, lrecl,
        recfm);
  }
}
