package com.bytezone.xmit.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.Preferences;

import com.bytezone.xmit.Utility;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;

// ------------------------------------------------------------------------------------ //
class OutputPane extends HeaderTabPane
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener,
    FontChangeListener, OutputWriter, SaveState, FilterChangeListener
//------------------------------------------------------------------------------------- //
{
  private static final String PREFS_LAST_TAB = "lastTab";

  final HeadersTab headersTab = new HeadersTab ("Headers", KeyCode.H);
  final BlocksTab blocksTab = new BlocksTab ("Blocks", KeyCode.B);
  final HexTab hexTab = new HexTab ("Hex", KeyCode.X);
  final OutputTab outputTab = new OutputTab ("Output", KeyCode.O);

  final OutputHeaderBar outputHeaderBar = new OutputHeaderBar ();

  //----------------------------------------------------------------------------------- //
  OutputPane ()
  //----------------------------------------------------------------------------------- //
  {
    xmitTabs.add (headersTab);
    xmitTabs.add (blocksTab);
    xmitTabs.add (hexTab);
    xmitTabs.add (outputTab);

    setTop (outputHeaderBar);
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void restore (Preferences prefs)
  //----------------------------------------------------------------------------------- //
  {
    tabPane.getSelectionModel ().select (prefs.getInt (PREFS_LAST_TAB, 0));
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void save (Preferences prefs)
  //----------------------------------------------------------------------------------- //
  {
    prefs.putInt (PREFS_LAST_TAB, tabPane.getSelectionModel ().getSelectedIndex ());
  }

  //----------------------------------------------------------------------------------- //
  void setTabVisible (boolean headersVisible, boolean blocksVisible, boolean hexVisible)
  //----------------------------------------------------------------------------------- //
  {
    tabPane.getTabs ().clear ();

    if (headersVisible)
      tabPane.getTabs ().add (headersTab);
    if (blocksVisible)
      tabPane.getTabs ().add (blocksTab);
    if (hexVisible)
      tabPane.getTabs ().add (hexTab);

    tabPane.getTabs ().add (outputTab);         // always visible
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void treeItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
    clearText ();
    updateCurrentTab ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void tableItemSelected (DatasetStatus datasetStatus)
  //----------------------------------------------------------------------------------- //
  {
    clearText ();
    updateCurrentTab ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void showLinesSelected (LineDisplayStatus lineDisplayStatus)
  //----------------------------------------------------------------------------------- //
  {
    clearText ();
    updateCurrentTab ();
  }

  //----------------------------------------------------------------------------------- //
  public void selectCodePage ()
  //----------------------------------------------------------------------------------- //
  {
    clearText ();
    updateCurrentTab ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void setFilter (FilterStatus filterStatus)
  //----------------------------------------------------------------------------------- //
  {
    clearText ();
    updateCurrentTab ();
  }

  //----------------------------------------------------------------------------------- //
  @Override
  public void write (File file)
  //----------------------------------------------------------------------------------- //
  {
    if (file == null)
      return;

    try (BufferedWriter output = new BufferedWriter (new FileWriter (file)))
    {
      for (String line : outputTab.getLines (0))
        output.write (line + "\n");
      Utility.showAlert (AlertType.INFORMATION, "Success",
          "File Saved: " + file.getName ());
    }
    catch (IOException e)
    {
      Utility.showAlert (AlertType.ERROR, "Error", "File Error: " + e.getMessage ());
    }
  }
}
