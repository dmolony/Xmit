package com.bytezone.xmit;

class BlockPointer
{
  final byte[] buffer;
  final int offset;
  final int length;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  BlockPointer (byte[] buffer, int offset, int length)
  {
    this.buffer = buffer;
    this.offset = offset;
    this.length = length;
  }

  String toHex ()
  {
    return Utility.toHex (buffer, offset, length);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%06X - %04X  %<,7d", offset, length);
  }
}