package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.CatalogEntry.ModuleType;
import com.bytezone.xmit.Filter;
import com.bytezone.xmit.Filter.FilterMode;
import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.gui.DataColumn.DisplayType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
class XmitTable extends TableView<CatalogEntryItem>                                 //
    implements SaveState, TreeItemSelectionListener, FontChangeListener,
    FilterChangeListener
// ---------------------------------------------------------------------------------//
{
  private static final String PREFS_LAST_MEMBER_NAME = "LastMemberName";

  private final List<FilterActionListener> filterListeners = new ArrayList<> ();
  private final List<TableItemSelectionListener> selectionListeners = new ArrayList<> ();
  private final ObservableList<CatalogEntryItem> items =
      FXCollections.observableArrayList ();

  private DatasetStatus datasetStatus;
  private FilterStatus filterStatus;

  private DisplayType currentDisplayType = null;
  private final List<DataColumn<?>> dataColumns = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  XmitTable ()
  // ---------------------------------------------------------------------------------//
  {
    SortedList<CatalogEntryItem> sortedList = new SortedList<> (items);
    sortedList.comparatorProperty ().bind (this.comparatorProperty ());
    setItems (sortedList);

    dataColumns.addAll (Arrays.asList (
        new StringColumn ("Member", "MemberName", 8, "CENTER-LEFT", DisplayType.ALL),
        new NumberColumn ("Bytes", "Bytes", 9, DisplayType.ALL),
        new StringColumn ("Id", "UserName", 8, "CENTER-LEFT", DisplayType.BASIC),
        new NumberColumn ("Size", "Size", 7, DisplayType.BASIC),
        new NumberColumn ("Init", "Init", 7, DisplayType.BASIC),
        new LocalDateColumn ("Created", "DateCreated", 10, DisplayType.BASIC),
        new LocalDateColumn ("Modified", "DateModified", 10, DisplayType.BASIC),
        new StringColumn ("Time", "Time", 8, "CENTER", DisplayType.BASIC),
        new FileTypeColumn ("Type", "Type", 5, "CENTER", DisplayType.BASIC),
        new StringColumn ("ver.mod", "Version", 7, "CENTER", DisplayType.BASIC),
        new NumberColumn ("Storage", "storage", 7, "%06X", "CENTER", DisplayType.LOAD),
        new NumberColumn ("Entry", "epa", 7, "%06X", "CENTER", DisplayType.LOAD),
        new StringColumn ("APF", "apf", 4, "CENTER", DisplayType.LOAD),
        new NumberColumn ("amode", "aMode", 5, DisplayType.LOAD),
        new NumberColumn ("rmode", "rMode", 5, DisplayType.LOAD),
        new NumberColumn ("ssi", "ssi", 8, "%08X", "CENTER", DisplayType.LOAD),
        new StringColumn ("Attributes", "attr", 10, "CENTER", DisplayType.LOAD),
        new StringColumn ("Alias", "AliasName", 8, "CENTER-LEFT", DisplayType.ALL)));

    Collections.sort (dataColumns);                   // sort into saved sequence

    for (DataColumn<?> dataColumn : dataColumns)
      getColumns ().add (dataColumn.createColumn ());

    getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSel, newSel) -> selected (newSel));
  }

  // ---------------------------------------------------------------------------------//
  private void selected (CatalogEntryItem catalogEntryItem)
  // ---------------------------------------------------------------------------------//
  {
    if (catalogEntryItem == null)
      datasetStatus.catalogEntrySelected (null);
    else
      datasetStatus.catalogEntrySelected (catalogEntryItem.getCatalogEntry ());

    for (TableItemSelectionListener listener : selectionListeners)
      listener.tableItemSelected (datasetStatus);
  }

  // ---------------------------------------------------------------------------------//
  void addListener (TableItemSelectionListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!selectionListeners.contains (listener))
      selectionListeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  private void setVisibleColumns (ModuleType moduleType)
  // ---------------------------------------------------------------------------------//
  {
    DisplayType displayType =
        moduleType == ModuleType.BASIC ? DisplayType.BASIC : DisplayType.LOAD;
    if (currentDisplayType != displayType)
    {
      currentDisplayType = displayType;
      for (DataColumn<?> dataColumn : dataColumns)
        dataColumn.column.setVisible (dataColumn.matches (displayType));
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void save (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    CatalogEntryItem catalogEntryItem = getSelectionModel ().getSelectedItem ();
    String name = catalogEntryItem == null ? "" : catalogEntryItem.getMemberName ();
    prefs.put (PREFS_LAST_MEMBER_NAME, name);

    int seq = 0;
    for (TableColumn<CatalogEntryItem, ?> column : getColumns ())
      ((DataColumn<?>) column.getUserData ()).save (seq++);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    String name = prefs.get (PREFS_LAST_MEMBER_NAME, "");
    selectCatalogEntryItem (find (name));
  }

  // ---------------------------------------------------------------------------------//
  private CatalogEntryItem find (String name)
  // ---------------------------------------------------------------------------------//
  {
    for (CatalogEntryItem catalogEntryItem : items)
      if (name.equals (catalogEntryItem.getMemberName ()))
        return catalogEntryItem;
    return null;
  }

  // ---------------------------------------------------------------------------------//
  void addFilterListener (FilterActionListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!filterListeners.contains (listener))
      filterListeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (DatasetStatus datasetStatus)
  // ---------------------------------------------------------------------------------//
  {
    this.datasetStatus = datasetStatus;

    if (datasetStatus.isPds ())
    {
      setVisibleColumns (((PdsDataset) datasetStatus.getDataset ()).getModuleType ());
      buildList (datasetStatus.previousSelection ());
    }
    else
    {
      items.clear ();
      setPlaceholder (new Label ("Not a Partitioned Dataset"));
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (FilterStatus filterStatus)
  // ---------------------------------------------------------------------------------//
  {
    Objects.requireNonNull (filterStatus);
    if (filterStatus.matches (this.filterStatus))
      return;

    this.filterStatus = filterStatus;

    if (datasetStatus != null && datasetStatus.getDataset () != null
        && datasetStatus.isPds ())
    {
      CatalogEntryItem selectedItem = getSelectionModel ().getSelectedItem ();
      String selectedName = selectedItem == null ? "" : selectedItem.getMemberName ();
      buildList (selectedName);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void buildList (String selectedName)
  // ---------------------------------------------------------------------------------//
  {
    // setEmptyTableMessage
    if (filterStatus.filterValue.isEmpty ())
      setPlaceholder (new Label (String.format ("No members to display")));
    else
      setPlaceholder (new Label (
          String.format ("No members contain '%s'", filterStatus.filterValue)));

    // create filter
    Filter filter =
        ((PdsDataset) datasetStatus.getDataset ()).getFilter (filterStatus.filterValue);
    FilterMode filterMode =
        filterStatus.filterValue.isEmpty () || !filterStatus.filterActive ? FilterMode.OFF
            : filterStatus.filterReverse ? FilterMode.REVERSED : FilterMode.ON;

    // build items based on filter value
    items.clear ();
    for (CatalogEntry catalogEntry : filter.getCatalogEntries (filterMode))
      items.add (new CatalogEntryItem (catalogEntry));

    // notify filter listeners
    for (FilterActionListener listener : filterListeners)
      listener.filtering (items.size (),
          ((PdsDataset) datasetStatus.getDataset ()).size (), true);

    // select a member
    selectCatalogEntryItem (findItem (selectedName));
  }

  // ---------------------------------------------------------------------------------//
  private CatalogEntryItem findItem (String name)
  // ---------------------------------------------------------------------------------//
  {
    if (!name.isEmpty ())
      for (CatalogEntryItem catalogEntryItem : items)
        if (catalogEntryItem.getMemberName ().equals (name))
          return catalogEntryItem;
    return null;
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
  private void selectCatalogEntryItem (CatalogEntryItem catalogEntryItem)
  // ---------------------------------------------------------------------------------//
  {
    if (items.size () == 0)
      return;

    if (catalogEntryItem == null)
      getSelectionModel ().select (0);                      // select by index
    else
      getSelectionModel ().select (catalogEntryItem);       // select by item

    scrollTo (getSelectionModel ().getSelectedIndex ());
  }
}
