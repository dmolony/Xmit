package com.bytezone.xmit;

class BlockPointer
{
  int start;
  int length;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  BlockPointer (int start, int length)
  {
    this.start = start;
    this.length = length;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%06X - %04X", start, length);
  }
}