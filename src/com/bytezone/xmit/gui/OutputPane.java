package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

import com.bytezone.xmit.*;
import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg.Org;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;

public class OutputPane extends DefaultPane
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener
{
  private static final String PREFS_LAST_TAB = "lastTab";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final TabPane tabPane = new TabPane ();

  private final XmitTab[] tabs = new XmitTab[4];
  private final int HEADERS = 0;
  private final int BLOCKS = 1;
  private final int HEX = 2;
  private final int OUTPUT = 3;

  private final ScrollBarState[] scrollBarStates =
      { new ScrollBarState (headersText, Orientation.VERTICAL),
        new ScrollBarState (blocksText, Orientation.VERTICAL),
        new ScrollBarState (hexText, Orientation.VERTICAL),
        new ScrollBarState (outputText, Orientation.VERTICAL) };

  private final Label lblMemberName = new Label ();
  private final Label lblDisposition = new Label ();

  private Reader reader;
  private Dataset dataset;
  private NamedData member;

  private boolean showLines;
  private boolean truncateLines;
  private Disposition disposition;

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

    tabs[HEADERS] = createTab ("Headers", KeyCode.H);
    tabs[BLOCKS] = createTab ("Blocks", KeyCode.B);
    tabs[HEX] = createTab ("Hex", KeyCode.X);
    tabs[OUTPUT] = createTab ("Output", KeyCode.O);

    setCenter (tabPane);
    setTop (getHBox (lblMemberName, lblDisposition));

    restore ();
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

    if (selectedTab == tabs[HEADERS].tab)
      updateHeadersTab ();
    else if (selectedTab == tabs[BLOCKS].tab)
      updateBlocksTab ();
    else if (selectedTab == tabs[HEX].tab)
      updateHexTab ();
    else if (selectedTab == tabs[OUTPUT].tab)
      updateOutputTab ();
    else
      System.out.println ("Unknown Tab:" + selectedTab);
  }

  // ---------------------------------------------------------------------------------//
  // updateHeadersTab
  // ---------------------------------------------------------------------------------//

  private void updateHeadersTab ()
  {
    if (member != null && tabs[HEADERS].isTextEmpty ())
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
  }

  // ---------------------------------------------------------------------------------//
  // updateBlocksTab
  // ---------------------------------------------------------------------------------//

  private void updateBlocksTab ()
  {
    if (member != null && tabs[BLOCKS].isTextEmpty ())
      tabs[BLOCKS].setText (member.toString ());
  }

  // ---------------------------------------------------------------------------------//
  // updateHexTab
  // ---------------------------------------------------------------------------------//

  private void updateHexTab ()
  {
    if (member != null && tabs[HEX].isTextEmpty ())
    {
      byte[] buffer = member.getDataBuffer ();

      if (buffer != null)
      {
        int max = Math.min (0x20000, buffer.length);
        tabs[HEX].setText (Utility.getHexDump (buffer, 0, max));
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // updateOutputTab
  // ---------------------------------------------------------------------------------//

  private void updateOutputTab ()
  {
    if (member != null && tabs[OUTPUT].isTextEmpty ())
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
    updateName ();
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
  // saveScrollBars
  // ---------------------------------------------------------------------------------//

  private void saveScrollBars ()
  {
    for (XmitTab tab : tabs)
      tab.saveScrollBar ();
  }

  // ---------------------------------------------------------------------------------//
  // restoreScrollBars
  // ---------------------------------------------------------------------------------//

  private void restoreScrollBars ()
  {
    for (XmitTab tab : tabs)
      tab.restoreScrollBar ();
  }

  // ---------------------------------------------------------------------------------//
  // clearText
  // ---------------------------------------------------------------------------------//

  private void clearText ()
  {
    for (XmitTab tab : tabs)
      tab.textArea.clear ();
  }

  // ---------------------------------------------------------------------------------//
  // keyPressed
  // ---------------------------------------------------------------------------------//

  public void keyPressed (KeyCode keyCode)
  {
    SingleSelectionModel<Tab> model = tabPane.getSelectionModel ();

    for (XmitTab tab : tabs)
      if (tab.keyCode == keyCode)
      {
        model.select (tab.tab);
        return;
      }
  }
}
