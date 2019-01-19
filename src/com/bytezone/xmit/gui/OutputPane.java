package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

import com.bytezone.xmit.*;
import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg.Org;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

public class OutputPane extends DefaultPane
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener
{
  private static final String PREFS_LAST_TAB = "lastTab";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final TabPane tabPane = new TabPane ();

  private final Tab headersTab = new Tab ();
  private final Tab blocksTab = new Tab ();
  private final Tab hexTab = new Tab ();
  private final Tab outputTab = new Tab ();

  private final TextArea headersText = new TextArea ();
  private final TextArea blocksText = new TextArea ();
  private final TextArea hexText = new TextArea ();
  private final TextArea outputText = new TextArea ();

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

    addText (headersTab, headersText, "Headers");
    addText (blocksTab, blocksText, "Blocks");
    addText (hexTab, hexText, "Hex");
    addText (outputTab, outputText, "Output");

    HBox hbox = getHBox (lblMemberName, lblDisposition);

    setCenter (tabPane);
    setTop (hbox);

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

    if (selectedTab == headersTab)
      updateHeadersTab ();
    else if (selectedTab == blocksTab)
      updateBlocksTab ();
    else if (selectedTab == hexTab)
      updateHexTab ();
    else if (selectedTab == outputTab)
      updateOutputTab ();
  }

  // ---------------------------------------------------------------------------------//
  // updateHeadersTab
  // ---------------------------------------------------------------------------------//

  private void updateHeadersTab ()
  {
    if (reader == null)
      headersText.clear ();
    else
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
      headersText.setText (text.toString ());
    }
  }

  // ---------------------------------------------------------------------------------//
  // updateBlocksTab
  // ---------------------------------------------------------------------------------//

  private void updateBlocksTab ()
  {
    if (member == null)
      blocksText.clear ();
    else
      blocksText.setText (member.toString ());
  }

  // ---------------------------------------------------------------------------------//
  // updateHexTab
  // ---------------------------------------------------------------------------------//

  private void updateHexTab ()
  {
    if (member == null)
      hexText.clear ();
    else
    {
      byte[] buffer = member.getDataBuffer ();

      if (buffer != null)
      {
        int max = Math.min (0x20000, buffer.length);
        hexText.setText (Utility.getHexDump (buffer, 0, max));
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // updateTextTab
  // ---------------------------------------------------------------------------------//

  private void updateOutputTab ()
  {
    if (member == null)
      outputText.clear ();
    else
      outputText.setText (member.getLines (showLines, truncateLines));
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

  void setTabVisible (boolean controlVisible, boolean debugVisible, boolean hexVisible)
  {
    tabPane.getTabs ().clear ();

    if (controlVisible)
      tabPane.getTabs ().add (headersTab);
    if (debugVisible)
      tabPane.getTabs ().add (blocksTab);
    if (hexVisible)
      tabPane.getTabs ().add (hexTab);

    tabPane.getTabs ().add (outputTab);         // always visible
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
        lblMemberName.setText (member.getName ());
      }
    }

    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  // tableItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  {
    this.member = catalogEntry.getMember ();

    if (catalogEntry.isAlias ())
      lblMemberName.setText (
          catalogEntry.getMemberName ().trim () + " -> " + catalogEntry.getAliasName ());
    else
      lblMemberName.setText (catalogEntry.getMemberName ());

    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  // showLinesSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void showLinesSelected (boolean showLines, boolean truncateLines)
  {
    this.showLines = showLines;
    this.truncateLines = truncateLines;
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
