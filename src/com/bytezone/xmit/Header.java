package com.bytezone.xmit;

// ---------------------------------------------------------------------------------//
class Header
//---------------------------------------------------------------------------------//
{
  final byte[] buffer;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  Header ()
  {
    this.buffer = new byte[12];
  }

  // ---------------------------------------------------------------------------------//
  // getSize
  // ---------------------------------------------------------------------------------//

  public int getSize ()
  {
    return (int) Utility.getValue (buffer, 9, 3);
  }

  // ---------------------------------------------------------------------------------//
  // isEmpty
  // ---------------------------------------------------------------------------------//

  boolean isEmpty ()
  {
    for (byte b : buffer)
      if (b != 0)
        return false;
    return true;
  }

  // ---------------------------------------------------------------------------------//
  // getTtl
  // ---------------------------------------------------------------------------------//

  long getTtl ()
  {
    return Utility.getValue (buffer, 4, 5);
  }

  // ---------------------------------------------------------------------------------//
  // ttlMatches
  // ---------------------------------------------------------------------------------//

  boolean ttlMatches (byte[] ttl)
  {
    return Utility.matches (ttl, buffer, 4);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return Utility.getHexValues (buffer);
  }
}
