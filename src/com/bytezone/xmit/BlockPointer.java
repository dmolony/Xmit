package com.bytezone.xmit;

// -----------------------------------------------------------------------------------//
class BlockPointer
// -----------------------------------------------------------------------------------//
{
  final byte[] buffer;
  final int offset;
  final int length;

  // ---------------------------------------------------------------------------------//
  BlockPointer (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    this.buffer = buffer;
    this.offset = offset;
    this.length = length;
  }

  // ---------------------------------------------------------------------------------//
  byte[] getData ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] data = new byte[length];
    System.arraycopy (buffer, offset, data, 0, length);
    return data;
  }

  // ---------------------------------------------------------------------------------//
  byte[] getData (int offset)
  // ---------------------------------------------------------------------------------//
  {
    byte[] data = new byte[length - offset];
    System.arraycopy (buffer, this.offset + offset, data, 0, data.length);
    return data;
  }

  // ---------------------------------------------------------------------------------//
  String getString (int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    return Utility.getString (buffer, this.offset + offset, length);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%06X - %06X:  %s", offset, offset + length - 1,
        Utility.getHexDump (buffer, offset, 12));
  }
}