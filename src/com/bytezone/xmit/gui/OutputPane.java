package com.bytezone.xmit.gui;

import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.xmit.BlockPointerList;
import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Reader;
import com.bytezone.xmit.textunit.ControlRecord;

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
  private final Tab metaTab = new Tab ();
  private final Tab textTab = new Tab ();
  private final Tab fileTab = new Tab ();
  private final TextArea metaText = new TextArea ();
  private final TextArea textText = new TextArea ();
  private final TextArea fileText = new TextArea ();

  private Reader reader;
  private CatalogEntry catalogEntry;
  private boolean showLines;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public OutputPane ()
  {
    tabPane.getTabs ().addAll (fileTab, metaTab, textTab);
    tabPane.setSide (Side.BOTTOM);
    tabPane.setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    tabPane.setTabMinWidth (100);

    tabPane.getSelectionModel ().selectedItemProperty ()
        .addListener ( (ov, oldTab, newTab) -> tabSelected (ov, oldTab, newTab));

    addText (fileTab, fileText, "Control");
    addText (metaTab, metaText, "Debug");
    addText (textTab, textText, "Output");
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
    if (selectedTab == metaTab)
      updateMetaTab ();
    else if (selectedTab == textTab)
      updateTextTab ();
    else if (selectedTab == fileTab)
      updateFileTab ();
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
      List<ControlRecord> controlRecords = reader.getControlRecords ();
      StringBuilder text = new StringBuilder ();
      for (ControlRecord controlRecord : controlRecords)
      {
        text.append (controlRecord.toString ());
        text.append ("\n");
      }

      text.append ("Catalog Blocks:\n");
      for (CatalogEntry catalogEntry : reader.getCatalogEntries ())
      {
        text.append (catalogEntry.debugLine ());
        text.append ("\n");
      }

      text.append ("\nData Blocks:\n");
      for (BlockPointerList bpl : reader.getDataBlockPointerLists ())
        text.append (String.format ("%s%n", bpl.getList ()));

      text.deleteCharAt (text.length () - 1);
      fileText.setText (text.toString ());
    }
  }

  // ---------------------------------------------------------------------------------//
  // updateMetaTab
  // ---------------------------------------------------------------------------------//

  private void updateMetaTab ()
  {
    if (reader == null)
      metaText.clear ();
    else if (catalogEntry == null)
      metaText.clear ();
    else
    {
      StringBuilder text = new StringBuilder ();
      //      if (catalogEntry.isXmit ())
      //        text.append ("XMIT file\n\n");
      text.append (catalogEntry.list ());
      metaText.setText (text.toString ());
    }
  }

  // ---------------------------------------------------------------------------------//
  // updateTextTab
  // ---------------------------------------------------------------------------------//

  private void updateTextTab ()
  {
    if (reader == null)
      textText.clear ();
    else if (catalogEntry == null)                  // flat file
      textText.setText (reader.getLines ());
    else                                            // PDS
      textText.setText (catalogEntry.getText (showLines));
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
  //
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemSelected (Reader reader)
  {
    this.reader = reader;
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
}
