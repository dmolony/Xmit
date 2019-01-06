package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

import com.bytezone.xmit.*;
import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg.Org;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class OutputPane extends BorderPane
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener
{
  private static final String PREFS_LAST_TAB = "lastTab";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final TabPane tabPane = new TabPane ();

  private final Tab fileTab = new Tab ();
  private final Tab debugTab = new Tab ();
  private final Tab textTab = new Tab ();
  private final Tab hexTab = new Tab ();

  private final TextArea fileText = new TextArea ();
  private final TextArea debugText = new TextArea ();
  private final TextArea textText = new TextArea ();
  private final TextArea hexText = new TextArea ();

  private Reader reader;
  private Dataset dataset;
  private CatalogEntry catalogEntry;
  private boolean showLines;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public OutputPane ()
  {
    tabPane.setSide (Side.BOTTOM);
    tabPane.setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    tabPane.setTabMinWidth (100);

    tabPane.getSelectionModel ().selectedItemProperty ()
        .addListener ( (ov, oldTab, newTab) -> tabSelected (ov, oldTab, newTab));

    addText (fileTab, fileText, "Control");
    addText (debugTab, debugText, "Debug");
    addText (textTab, textText, "Output");
    addText (hexTab, hexText, "Hex");

    setCenter (tabPane);

    restore ();
  }

  // ---------------------------------------------------------------------------------//
  // addText
  // ---------------------------------------------------------------------------------//

  private void addText (Tab tab, TextArea text, String title)
  {
    tab.setContent (text);
    tab.setText (title);
    text.setFont (Font.font ("Monospaced", 13));
    text.setEditable (false);
    text.setWrapText (false);
  }

  // ---------------------------------------------------------------------------------//
  // tabSelected
  // ---------------------------------------------------------------------------------//

  private void tabSelected (ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab)
  {
    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  // updateCurrentTab
  // ---------------------------------------------------------------------------------//

  private void updateCurrentTab ()
  {
    Tab selectedTab = tabPane.getSelectionModel ().getSelectedItem ();

    if (selectedTab == debugTab)
      updateDebugTab ();
    else if (selectedTab == textTab)
      updateTextTab ();
    else if (selectedTab == fileTab)
      updateFileTab ();
    else if (selectedTab == hexTab)
      updateHexTab ();
  }

  // ---------------------------------------------------------------------------------//
  // updateFileTab
  // ---------------------------------------------------------------------------------//

  private void updateFileTab ()
  {
    if (reader == null)
      fileText.clear ();
    else
    {
      StringBuilder text = new StringBuilder ();
      for (ControlRecord controlRecord : reader.getControlRecords ())
      {
        text.append (controlRecord.toString ());
        text.append ("\n");
      }
      text.deleteCharAt (text.length () - 1);

      if (dataset.getOrg () == Org.PDS)
      {
        text.append ("COPYR1\n");
        text.append (((PdsDataset) dataset).getCopyR1 ());
        text.append ("\n\n");
        text.append ("COPYR2\n");
        text.append (((PdsDataset) dataset).getCopyR2 ());
        text.append ("\n\n");

        text.append (String.format ("%s Catalog Blocks:%n", reader.getName ()));
        text.append (
            "   --name-- ---id--- -ttr-- versn    ss -created--  -modified-  hh mm ");
        text.append ("Size1 Size2       -------- user ---------\n");
        for (CatalogEntry catalogEntry : ((PdsDataset) dataset).getMembers ())
        {
          text.append (catalogEntry.debugLine ());
          text.append ("\n");
        }
        text.append (((PdsDataset) dataset).getBlockListing2 ());
      }

      //      text.append ("\nData Blocks:\n");
      //      for (BlockPointerList bpl : reader.getDataBlockPointerLists ())
      //        text.append (String.format ("%s%n", bpl.getList ()));

      text.deleteCharAt (text.length () - 1);
      fileText.setText (text.toString ());
    }
  }

  // ---------------------------------------------------------------------------------//
  // updateDebugTab
  // ---------------------------------------------------------------------------------//

  private void updateDebugTab ()
  {
    if (reader == null)
      debugText.clear ();
    else if (catalogEntry == null)
      debugText.clear ();
    else
    {
      StringBuilder text = new StringBuilder ();
      text.append (catalogEntry.list ());
      debugText.setText (text.toString ());
    }
  }

  // ---------------------------------------------------------------------------------//
  // updateTextTab
  // ---------------------------------------------------------------------------------//

  private void updateTextTab ()
  {
    if (reader == null)
      textText.clear ();
    else if (dataset.getOrg () == Org.PS)                  // flat file
      textText.setText (((PsDataset) dataset).getLines ());
    else if (catalogEntry != null)                         // PDS
      textText.setText (catalogEntry.getLines (showLines));
  }

  // ---------------------------------------------------------------------------------//
  // updateHexTab
  // ---------------------------------------------------------------------------------//

  private void updateHexTab ()
  {
    if (reader == null)
      hexText.clear ();
    else if (dataset.getOrg () == Org.PS)                  // flat file
      hexText.setText (Utility.getHexDump (((PsDataset) dataset).getRawBuffer ()));
    else if (catalogEntry != null)                         // PDS
      hexText.setText (Utility.getHexDump (catalogEntry.getDataBuffer ()));
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  private void restore ()
  {
    tabPane.getSelectionModel ().select (prefs.getInt (PREFS_LAST_TAB, 0));
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.putInt (PREFS_LAST_TAB, tabPane.getSelectionModel ().getSelectedIndex ());
  }

  // ---------------------------------------------------------------------------------//
  // setTabVisible
  // ---------------------------------------------------------------------------------//

  void setTabVisible (boolean fileVisible, boolean debugVisible, boolean hexVisible)
  {
    tabPane.getTabs ().clear ();
    if (fileVisible)
      tabPane.getTabs ().add (fileTab);
    if (debugVisible)
      tabPane.getTabs ().add (debugTab);
    if (hexVisible)
      tabPane.getTabs ().add (hexTab);
    tabPane.getTabs ().add (textTab);
  }

  // ---------------------------------------------------------------------------------//
  // treeItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemSelected (Reader reader, Dataset dataset)
  {
    this.reader = reader;
    this.dataset = dataset;

    catalogEntry = null;
    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  // tableItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  {
    this.catalogEntry = catalogEntry;
    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  // showLinesSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void showLinesSelected (boolean showLines)
  {
    this.showLines = showLines;
    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  // selectCodePage
  // ---------------------------------------------------------------------------------//

  public void selectCodePage ()
  {
    updateCurrentTab ();
  }
}
