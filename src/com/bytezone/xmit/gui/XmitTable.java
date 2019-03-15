package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.prefs.Preferences;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.CatalogEntry.ModuleType;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.gui.DataColumn.DisplayType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
class XmitTable extends TableView<CatalogEntryItem>
    implements TreeItemSelectionListener, FontChangeListener, SaveState, FilterListener
// ---------------------------------------------------------------------------------//
{
  private static final String PREFS_LAST_MEMBER_NAME = "LastMemberName";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final List<TableItemSelectionListener> listeners = new ArrayList<> ();
  private final ObservableList<CatalogEntryItem> items =
      FXCollections.observableArrayList ();
  private final FilteredList<CatalogEntryItem> filteredList = new FilteredList<> (items);

  private Dataset dataset;
  private final Map<Dataset, String> selectedMembers = new HashMap<> ();

  private DisplayType currentVisibleType = null;
  private final List<DataColumn> dataColumns = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  XmitTable ()
  // ---------------------------------------------------------------------------------//
  {
    SortedList<CatalogEntryItem> sortedList = new SortedList<> (filteredList);
    sortedList.comparatorProperty ().bind (this.comparatorProperty ());
    setItems (sortedList);

    dataColumns.addAll (Arrays.asList (
        new StringColumn ("Member", "MemberName", 8, "CENTER-LEFT", DisplayType.All),
        new NumberColumn ("Bytes", "Bytes", 9, DisplayType.All),
        new StringColumn ("Id", "UserName", 8, "CENTER-LEFT", DisplayType.Basic),
        new NumberColumn ("Size", "Size", 7, DisplayType.Basic),
        new NumberColumn ("Init", "Init", 7, DisplayType.Basic),
        new LocalDateColumn ("Created", "DateCreated", 10, DisplayType.Basic),
        new LocalDateColumn ("Modified", "DateModified", 10, DisplayType.Basic),
        new StringColumn ("Time", "Time", 8, "CENTER", DisplayType.Basic),
        new FileTypeColumn ("Type", "Type", 5, "CENTER", DisplayType.Basic),
        new StringColumn ("ver.mod", "Version", 7, "CENTER", DisplayType.Basic),
        new NumberColumn ("Storage", "storage", 7, "%06X", "CENTER", DisplayType.Load),
        new NumberColumn ("Entry", "epa", 7, "%06X", "CENTER", DisplayType.Load),
        new StringColumn ("APF", "apf", 4, "CENTER", DisplayType.Load),
        new NumberColumn ("amode", "aMode", 5, DisplayType.Load),
        new NumberColumn ("rmode", "rMode", 5, DisplayType.Load),
        new NumberColumn ("ssi", "ssi", 8, "%08X", "CENTER", DisplayType.Load),
        new StringColumn ("Attributes", "attr", 10, "CENTER", DisplayType.Load),
        new StringColumn ("Alias", "AliasName", 8, "CENTER-LEFT", DisplayType.All)));

    for (DataColumn dataColumn : dataColumns)
      getColumns ().add (dataColumn.column);

    getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, catalogEntryItem) ->
        {
          if (catalogEntryItem == null)
            for (TableItemSelectionListener listener : listeners)
              listener.tableItemSelected (null);
          else
          {
            CatalogEntry catalogEntry = catalogEntryItem.getCatalogEntry ();
            selectedMembers.put (dataset, catalogEntry.getMemberName ());

            for (TableItemSelectionListener listener : listeners)
              listener.tableItemSelected (catalogEntry);
          }
        });
  }

  // ---------------------------------------------------------------------------------//
  void setVisibleColumns (DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    if (currentVisibleType == displayType)
      return;

    currentVisibleType = displayType;

    for (DataColumn dataColumn : dataColumns)
      dataColumn.column.setVisible (dataColumn.displayType == DisplayType.All
          || dataColumn.displayType == displayType);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void save ()
  // ---------------------------------------------------------------------------------//
  {
    CatalogEntryItem catalogEntryItem = getSelectionModel ().getSelectedItem ();
    String name = catalogEntryItem == null ? "" : catalogEntryItem.getMemberName ();
    prefs.put (PREFS_LAST_MEMBER_NAME, name);

    //    for (TableColumn<CatalogEntryItem, ?> column : getColumns ())
    //    {
    //      System.out.printf ("%-12s %5.1f  %5.1f  %5.1f%n", column.getText (),
    //          column.getMinWidth (), column.getPrefWidth (), column.getWidth ());
    //    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore ()
  // ---------------------------------------------------------------------------------//
  {
    String name = prefs.get (PREFS_LAST_MEMBER_NAME, "");
    CatalogEntryItem catalogEntryItem = find (name);
    select (catalogEntryItem);
  }

  // ---------------------------------------------------------------------------------//
  private CatalogEntryItem find (String name)
  // ---------------------------------------------------------------------------------//
  {
    for (CatalogEntryItem catalogEntryItem : filteredList)
      if (name.equals (catalogEntryItem.getMemberName ()))
        return catalogEntryItem;
    return null;
  }

  // ---------------------------------------------------------------------------------//
  void addListener (TableItemSelectionListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  void removeListener (TableItemSelectionListener listener)
  // ---------------------------------------------------------------------------------//
  {
    listeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  // ---------------------------------------------------------------------------------//
  {
    this.dataset = dataset;

    items.clear ();
    if (dataset != null && dataset.isPds ())
    {
      PdsDataset pdsDataset = (PdsDataset) dataset;

      for (CatalogEntry catalogEntry : pdsDataset)
        items.add (new CatalogEntryItem (catalogEntry));

      select (selectedMembers.containsKey (dataset) ? find (selectedMembers.get (dataset))
          : null);

      setVisibleColumns (pdsDataset.getModuleType () == ModuleType.BASIC
          ? DisplayType.Basic : DisplayType.Load);
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    DataColumn.font = font;
    refresh ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (String filter)
  // ---------------------------------------------------------------------------------//
  {
    filteredList.setPredicate (new Predicate<CatalogEntryItem> ()
    {
      @Override
      public boolean test (CatalogEntryItem t)
      {
        return filter.isEmpty () ? true : t.getCatalogEntry ().contains (filter);
      }
    });

    // the change may have filtered out the previously selected member
    if (dataset != null && dataset.isPds () && getSelectedItem () == null)
      select (null);
  }

  // ---------------------------------------------------------------------------------//
  private CatalogEntryItem getSelectedItem ()
  // ---------------------------------------------------------------------------------//
  {
    return getSelectionModel ().getSelectedItem ();
  }

  // ---------------------------------------------------------------------------------//
  private void select (CatalogEntryItem catalogEntryItem)
  // ---------------------------------------------------------------------------------//
  {
    if (filteredList.size () == 0)
      return;

    if (catalogEntryItem == null)
      getSelectionModel ().select (0);                      // select by index
    else
      getSelectionModel ().select (catalogEntryItem);       // select by item

    scrollTo (getSelectionModel ().getSelectedIndex ());
  }
}
