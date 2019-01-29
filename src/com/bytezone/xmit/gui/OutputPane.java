package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

import com.bytezone.xmit.*;
import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg.Org;

import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.KeyCode;

public class OutputPane extends DefaultTabPane implements TreeItemSelectionListener,
    TableItemSelectionListener, ShowLinesListener, FontChangeListener
{
  private static final String PREFS_LAST_TAB = "lastTab";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final TabPane tabPane = new TabPane ();

  private final int HEADERS = 0;
  private final int BLOCKS = 1;
  private final int HEX = 2;
  private final int OUTPUT = 3;

  private final Label lblMemberName = new Label ();
  private final Label lblDisposition = new Label ();

  private Reader reader;
  private Dataset dataset;
  private DataFile member;

  private boolean showLines;
  private boolean truncateLines;
  private Disposition disposition;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public OutputPane ()
  {
    super (4);

    tabPane.setSide (Side.BOTTOM);
    tabPane.setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    tabPane.setTabMinWidth (100);

    tabPane.getSelectionModel ().selectedItemProperty ()
        .addListener ( (ov, oldTab, newTab) -> updateCurrentTab ());

    tabs[HEADERS] = createTab ("Headers", KeyCode.H, () -> updateHeadersTab ());
    tabs[BLOCKS] = createTab ("Blocks", KeyCode.B, () -> updateBlocksTab ());
    tabs[HEX] = createTab ("Hex", KeyCode.X, () -> updateHexTab ());
    tabs[OUTPUT] = createTab ("Output", KeyCode.O, () -> updateOutputTab ());

    setCenter (tabPane);
    setTop (getHBox (lblMemberName, lblDisposition));

    restore ();
  }

  // ---------------------------------------------------------------------------------//
  // updateCurrentTab
  // ---------------------------------------------------------------------------------//

  private void updateCurrentTab ()
  {
    if (member != null)
    {
      Tab selectedTab = tabPane.getSelectionModel ().getSelectedItem ();
      if (selectedTab != null)
      {
        XmitTab xmitTab = (XmitTab) selectedTab.getUserData ();
        if (xmitTab.isTextEmpty ())
          xmitTab.update ();
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // updateHeadersTab
  // ---------------------------------------------------------------------------------//

  private void updateHeadersTab ()
  {
    StringBuilder text = new StringBuilder ();

    for (ControlRecord controlRecord : reader.getControlRecords ())
    {
      text.append (controlRecord.toString ());
      text.append ("\n");
    }
    text.deleteCharAt (text.length () - 1);

    if (disposition.getOrg () == Org.PDS)
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

      for (PdsMember member : (PdsDataset) dataset)
      {
        if (member.getCatalogEntry () != null)
          text.append (member.getCatalogEntry ().debugLine ());
        else
          text.append ("not found");
        text.append ("\n");
      }
    }

    Utility.removeTrailingNewlines (text);
    tabs[HEADERS].setText (text.toString ());
  }

  // ---------------------------------------------------------------------------------//
  // updateBlocksTab
  // ---------------------------------------------------------------------------------//

  private void updateBlocksTab ()
  {
    tabs[BLOCKS].setText (member.toString ());
  }

  // ---------------------------------------------------------------------------------//
  // updateHexTab
  // ---------------------------------------------------------------------------------//

  private void updateHexTab ()
  {
    byte[] buffer = member.getDataBuffer ();

    if (buffer != null)
    {
      int max = Math.min (0x20000, buffer.length);
      tabs[HEX].setText (Utility.getHexDump (buffer, 0, max));
    }
  }

  // ---------------------------------------------------------------------------------//
  // updateOutputTab
  // ---------------------------------------------------------------------------------//

  private void updateOutputTab ()
  {
    tabs[OUTPUT].setText (member.getLines (showLines, truncateLines));
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

  void setTabVisible (boolean headersVisible, boolean blocksVisible, boolean hexVisible)
  {
    tabPane.getTabs ().clear ();

    if (headersVisible)
      tabPane.getTabs ().add (tabs[HEADERS].tab);
    if (blocksVisible)
      tabPane.getTabs ().add (tabs[BLOCKS].tab);
    if (hexVisible)
      tabPane.getTabs ().add (tabs[HEX].tab);

    tabPane.getTabs ().add (tabs[OUTPUT].tab);         // always visible
  }

  // ---------------------------------------------------------------------------------//
  // treeItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemSelected (Reader reader, Dataset dataset, String name, String path)
  {
    this.reader = reader;
    this.dataset = dataset;

    member = null;

    if (dataset == null)
    {
      lblMemberName.setText ("");
      disposition = null;
      lblDisposition.setText ("");
    }
    else
    {
      disposition = dataset.getDisposition ();
      lblDisposition.setText (disposition.toString ());
      if (disposition.getOrg () == Org.PS)
      {
        member = ((PsDataset) dataset).getMember ();
        updateName ();
      }
    }

    clearText ();
    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  // tableItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  {
    this.member = catalogEntry.getMember ();
    updateName ();
    clearText ();
    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  // updateName
  // ---------------------------------------------------------------------------------//

  private void updateName ()
  {
    if (dataset == null)
      return;

    String indicator = truncateLines ? "<- " : "";

    if (dataset.isPds ())
    {
      CatalogEntry catalogEntry = ((PdsMember) member).getCatalogEntry ();
      if (catalogEntry.isAlias ())
        lblMemberName.setText (indicator + catalogEntry.getMemberName ().trim () + " -> "
            + catalogEntry.getAliasName ());
      else
        lblMemberName.setText (indicator + catalogEntry.getMemberName ());
    }
    else
      lblMemberName.setText (indicator + member.getName ());
  }

  // ---------------------------------------------------------------------------------//
  // showLinesSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void showLinesSelected (boolean showLines, boolean truncateLines)
  {
    this.showLines = showLines;
    this.truncateLines = truncateLines;

    saveScrollBars ();
    clearText ();
    updateCurrentTab ();
    updateName ();              // toggle the '<-' indicator
    restoreScrollBars ();
  }

  // ---------------------------------------------------------------------------------//
  // selectCodePage
  // ---------------------------------------------------------------------------------//

  public void selectCodePage ()
  {
    saveScrollBars ();
    clearText ();
    updateCurrentTab ();
    restoreScrollBars ();
  }

  // ---------------------------------------------------------------------------------//
  // keyPressed
  // ---------------------------------------------------------------------------------//

  public void keyPressed (KeyCode keyCode)
  {
    for (XmitTab tab : tabs)
      if (tab.keyCode == keyCode)
      {
        tabPane.getSelectionModel ().select (tab.tab);
        return;
      }
  }
}
