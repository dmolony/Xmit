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
import javafx.scene.text.Font;

public class OutputPane extends DefaultPane
    implements TreeItemSelectionListener, TableItemSelectionListener, ShowLinesListener
{
  private static final String PREFS_LAST_TAB = "lastTab";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final TabPane tabPane = new TabPane ();

  private final Tab controlTab = new Tab ();
  private final Tab debugTab = new Tab ();
  private final Tab hexTab = new Tab ();
  private final Tab outputTab = new Tab ();

  private final TextArea controlText = new TextArea ();
  private final TextArea debugText = new TextArea ();
  private final TextArea hexText = new TextArea ();
  private final TextArea outputText = new TextArea ();

  private final Label lblMemberName = new Label ();
  private final Label lblDisposition = new Label ();

  private Reader reader;
  private Dataset dataset;
  private CatalogEntry catalogEntry;
  private Member member;
  private boolean showLines;
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

    addText (controlTab, controlText, "Control");
    addText (debugTab, debugText, "Debug");
    addText (hexTab, hexText, "Hex");
    addText (outputTab, outputText, "Output");

    HBox hbox = getHBox (lblMemberName, lblDisposition);

    setCenter (tabPane);
    setTop (hbox);

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

    if (selectedTab == controlTab)
      updateControlTab ();
    else if (selectedTab == debugTab)
      updateDebugTab ();
    else if (selectedTab == hexTab)
      updateHexTab ();
    else if (selectedTab == outputTab)
      updateOutputTab ();
  }

  // ---------------------------------------------------------------------------------//
  // updateControlTab
  // ---------------------------------------------------------------------------------//

  private void updateControlTab ()
  {
    if (reader == null)
      controlText.clear ();
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
        //        for (CatalogEntry catalogEntry : ((PdsDataset) dataset).getMembers ())
        for (Member member : (PdsDataset) dataset)
        {
          text.append (member.getCatalogEntry ().debugLine ());
          text.append ("\n");
        }
        //        text.append (((PdsDataset) dataset).getBlockListing ());
      }

      text.deleteCharAt (text.length () - 1);
      controlText.setText (text.toString ());
    }
  }

  // ---------------------------------------------------------------------------------//
  // updateDebugTab
  // ---------------------------------------------------------------------------------//

  private void updateDebugTab ()
  {
    if (reader == null)
      debugText.clear ();
    else if (disposition.getOrg () == Org.PS)                        // flat file
    {
      debugText.setText (dataset.listSegments ());
    }
    else if (catalogEntry != null)
      debugText.setText (catalogEntry.getMember ().toString ());
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
      //      if (disposition.getOrg () == Org.PS)                        // flat file
      //        buffer = ((PsDataset) dataset).getRawBuffer ();
      //      else if (catalogEntry != null)                          // PDS
      //        buffer = catalogEntry.getMember ().getDataBuffer ();
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
    if (reader == null || member == null)
      outputText.clear ();
    //    else if (disposition.getOrg () == Org.PS)                  // flat file
    //      outputText.setText (((PsDataset) dataset).getLines ());
    //    else if (catalogEntry != null)                         // PDS
    //      outputText.setText (catalogEntry.getMember ().getLines (showLines));
    else
      outputText.setText (member.getLines (showLines));
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
      tabPane.getTabs ().add (controlTab);
    if (debugVisible)
      tabPane.getTabs ().add (debugTab);
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

    catalogEntry = null;
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
    this.catalogEntry = catalogEntry;
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
