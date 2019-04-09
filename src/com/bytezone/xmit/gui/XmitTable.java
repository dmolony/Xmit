package com.bytezone.xmit.gui;

import java.util.*;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Font;

// ---------------------------------------------------------------------------------//
class XmitTable extends TableView<CatalogEntryItem> implements TreeItemSelectionListener,
    FontChangeListener, SaveState, FilterChangeListener
// ---------------------------------------------------------------------------------//
{
  private static final String PREFS_LAST_MEMBER_NAME = "LastMemberName";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final List<FilterActionListener> filterListeners = new ArrayList<> ();
  private final List<TableItemSelectionListener> selectionListeners = new ArrayList<> ();
  private final ObservableList<CatalogEntryItem> items =
      FXCollections.observableArrayList ();
  private final FilteredList<CatalogEntryItem> filteredList = new FilteredList<> (items);

  private Dataset dataset;
  private final Map<Dataset, String> selectedMembers = new HashMap<> ();

  private DisplayType currentDisplayType = null;
  private final List<DataColumn<?>> dataColumns = new ArrayList<> ();
  private String filter = "";

  // ---------------------------------------------------------------------------------//
  XmitTable ()
  // ---------------------------------------------------------------------------------//
  {
    //    SortedList<CatalogEntryItem> sortedList = new SortedList<> (filteredList);  // items
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

  // these should be handled by the selection model
  // ---------------------------------------------------------------------------------//
  private void selected (CatalogEntryItem catalogEntryItem)
  // ---------------------------------------------------------------------------------//
  {
    if (catalogEntryItem == null)
      for (TableItemSelectionListener listener : selectionListeners)
        listener.tableItemSelected (null);
    else
    {
      CatalogEntry catalogEntry = catalogEntryItem.getCatalogEntry ();
      selectedMembers.put (dataset, catalogEntry.getMemberName ());

      for (TableItemSelectionListener listener : selectionListeners)
        listener.tableItemSelected (catalogEntry);
    }
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
  public void save ()
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

  // this will go
  // ---------------------------------------------------------------------------------//
  void addListener (TableItemSelectionListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!selectionListeners.contains (listener))
      selectionListeners.add (listener);
  }

  // this will go
  // ---------------------------------------------------------------------------------//
  void removeListener (TableItemSelectionListener listener)
  // ---------------------------------------------------------------------------------//
  {
    selectionListeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  void addFilterListener (FilterActionListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!filterListeners.contains (listener))
      filterListeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  void removeFilterListener (FilterActionListener listener)
  // ---------------------------------------------------------------------------------//
  {
    filterListeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void treeItemSelected (Dataset dataset, String name)
  // ---------------------------------------------------------------------------------//
  {
    this.dataset = dataset;

    if (dataset != null && dataset.isPds ())
    {
      buildList ();
      setVisibleColumns (((PdsDataset) dataset).getModuleType ());
    }
    else
    {
      items.clear ();
      setPlaceholder (new Label ("Not a Partitioned Dataset"));
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFilter (String filter, boolean fullFilter)
  // ---------------------------------------------------------------------------------//
  {
    this.filter = filter;

    if (dataset != null && dataset.isPds ())
      buildList ();
  }

  // ---------------------------------------------------------------------------------//
  private void buildList ()
  // ---------------------------------------------------------------------------------//
  {
    items.clear ();

    int max = ((PdsDataset) dataset).size ();
    for (FilterActionListener filterActionListener : filterListeners)
      filterActionListener.filtering (0, max, false);

    for (CatalogEntry catalogEntry : ((PdsDataset) dataset).getCatalogEntries (filter))
      items.add (new CatalogEntryItem (catalogEntry));

    select (selectedMembers.containsKey (dataset) ? find (selectedMembers.get (dataset))
        : null);

    // setEmptyTableMessage
    setPlaceholder (new Label (filter.isEmpty () ? "No members to display"
        : String.format ("No members contain '%s'", filter)));

    for (FilterActionListener filterActionListener : filterListeners)
      filterActionListener.filtering (items.size (), max, true);
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
  //  private void setEmptyTableMessage ()
  //  // ---------------------------------------------------------------------------------//
  //  {
  //    if (dataset != null && dataset.isPds ())
  //      setPlaceholder (new Label (filter.isEmpty () ? "No members to display"
  //          : String.format ("No members contain '%s'", filter)));
  //    else
  //      setPlaceholder (new Label ("Not a Partitioned Dataset"));
  //  }

  // ---------------------------------------------------------------------------------//
  //  private CatalogEntryItem getSelectedItem ()
  //  // ---------------------------------------------------------------------------------//
  //  {
  //    return getSelectionModel ().getSelectedItem ();
  //  }

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
