package com.bytezone.xmit.textunit;

public class DatasetType extends TextUnitNumber
{
/*
 * X'80' Data library
 * X'40' Program library
 * X'04' Extended format sequential data set
 * X'01' Large format sequential data set
 */
  public DatasetType (byte[] buffer, int ptr)
  {
    super (buffer, ptr);
  }

}
