package com.bytezone.xmit.gui;

import java.util.prefs.Preferences;

import com.bytezone.xmit.*;
import com.bytezone.xmit.textunit.ControlRecord;
import com.bytezone.xmit.textunit.Dsorg.Org;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.KeyCode;
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
    if (member != null && headersText.getText ().isEmpty ())
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
    if (member != null && blocksText.getText ().isEmpty ())
      blocksText.setText (member.toString ());
  }

  // ---------------------------------------------------------------------------------//
  // updateHexTab
  // ---------------------------------------------------------------------------------//

  private void updateHexTab ()
  {
    if (member != null && hexText.getText ().isEmpty ())
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
  // updateOutputTab
  // ---------------------------------------------------------------------------------//

  private void updateOutputTab ()
  {
    if (member != null && outputText.getText ().isEmpty ())
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
        updateName ();
      }
    }

    resetTabs ();
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
    resetTabs ();
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

    resetTabs ();
    updateCurrentTab ();
    updateName ();
  }

  // ---------------------------------------------------------------------------------//
  // selectCodePage
  // ---------------------------------------------------------------------------------//

  public void selectCodePage ()
  {
    resetTabs ();
    updateCurrentTab ();
  }

  // ---------------------------------------------------------------------------------//
  // resetTabs
  // ---------------------------------------------------------------------------------//

  private void resetTabs ()
  {
    headersText.clear ();
    blocksText.clear ();
    hexText.clear ();
    outputText.clear ();
  }

  // ---------------------------------------------------------------------------------//
  //
  // ---------------------------------------------------------------------------------//

  private ScrollBar getScrollBar (TabPane tree, Orientation orientation)
  {
    // Get the ScrollBar with the given Orientation using lookupAll
    for (Node n : tree.lookupAll (".scroll-bar"))
    {
      if (n instanceof ScrollBar)
      {
        ScrollBar bar = (ScrollBar) n;

        if (bar.getOrientation ().equals (orientation))
          return bar;
      }
    }
    return null;
  }

  // ---------------------------------------------------------------------------------//
  // keyPressed
  // ---------------------------------------------------------------------------------//

  public void keyPressed (KeyCode keyCode)
  {
    SingleSelectionModel<Tab> model = tabPane.getSelectionModel ();
    switch (keyCode)
    {
      case H:
        model.select (headersTab);
        break;
      case B:
        model.select (blocksTab);
        break;
      case X:
        model.select (hexTab);
        break;
      case O:
        model.select (outputTab);
        break;
      default:
        break;
    }
  }
}
