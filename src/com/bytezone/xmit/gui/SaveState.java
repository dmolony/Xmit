package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

interface SaveState
{
  void save (Preferences prefs);

  void restore (Preferences prefs);
}
