package com.bytezone.xmit.gui;

import com.bytezone.xmit.Filter.FilterMode;

// ----------------------------------------------------------------------------------- //
interface FilterChangeListener
//----------------------------------------------------------------------------------- //
{
  public void setFilter (String filter, boolean fullFilter, FilterMode mode);
}
