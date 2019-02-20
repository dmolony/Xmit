package com.bytezone.xmit.gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.Reader;
import com.bytezone.xmit.Utility.FileType;
import com.bytezone.xmit.textunit.Dsorg.Org;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.util.Callback;

// ---------------------------------------------------------------------------------//
class XmitTable extends TableView<CatalogEntryItem>
    implements TreeItemSelectionListener, FontChangeListener
// ---------------------------------------------------------------------------------//
{
  private static final String PREFS_LAST_MEMBER_INDEX = "LastMemberIndex";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final List<TableItemSelectionListener> listeners = new ArrayList<> ();
  private final ObservableList<CatalogEntryItem> items =
      FXCollections.observableArrayList ();

  private Dataset dataset;
  private final Map<Reader, String> selectedMembers = new HashMap<> ();
  private Font font;

  private TableColumn<CatalogEntryItem, String> idColumn;
  private TableColumn<CatalogEntryItem, String> timeColumn;
  private TableColumn<CatalogEntryItem, String> versionColumn;
  private TableColumn<CatalogEntryItem, Number> sizeColumn;
  private TableColumn<CatalogEntryItem, Number> initColumn;
  private TableColumn<CatalogEntryItem, LocalDate> createdColumn;
  private TableColumn<CatalogEntryItem, LocalDate> modifiedColumn;
  private TableColumn<CatalogEntryItem, FileType> typeColumn;

  private TableColumn<CatalogEntryItem, Number> epaColumn;
  private TableColumn<CatalogEntryItem, Number> storageColumn;
  private TableColumn<CatalogEntryItem, Number> aModeColumn;
  private TableColumn<CatalogEntryItem, Number> rModeColumn;
  private TableColumn<CatalogEntryItem, String> apfColumn;
  private TableColumn<CatalogEntryItem, String> attrColumn;

  private int currentVisible = 0;

  // ---------------------------------------------------------------------------------//
  XmitTable ()
  // ---------------------------------------------------------------------------------//
  {
    setItems (items);

    // common
    addString ("Member", "MemberName", 100, "CENTER-LEFT");
    addNumber ("Bytes", "Bytes", 90);

    // basic module
    idColumn = addString ("Id", "UserName", 100, "CENTER-LEFT");
    sizeColumn = addNumber ("Size", "Size", 70);
    initColumn = addNumber ("Init", "Init", 70);
    createdColumn = addLocalDate ("Created", "DateCreated", 100);
    modifiedColumn = addLocalDate ("Modified", "DateModified", 100);
    timeColumn = addString ("Time", "Time", 90, "CENTER");
    typeColumn = addFileType ("Type", "Type", 50, "CENTER");
    versionColumn = addString ("ver.mod", "Version", 70, "CENTER");

    // load module
    epaColumn = addNumber ("EPA", "epa", 70);
    storageColumn = addNumber ("Storage", "storage", 70);
    apfColumn = addString ("APF", "apf", 50, "CENTER");
    aModeColumn = addNumber ("amode", "aMode", 50);
    rModeColumn = addNumber ("rmode", "rMode", 50);
    attrColumn = addString ("Attributes", "attr", 100, "CENTER-LEFT");

    // common
    addString ("Alias", "AliasName", 100, "CENTER-LEFT");

    getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, catalogEntryItem) ->
        {
          if (catalogEntryItem == null)
            return;

          CatalogEntry catalogEntry = catalogEntryItem.getCatalogEntry ();
          selectedMembers.put (dataset.getReader (), catalogEntry.getMemberName ());
          for (TableItemSelectionListener listener : listeners)
            listener.tableItemSelected (catalogEntry);
        });
  }

  // ---------------------------------------------------------------------------------//
  void setVisibleColumns (int type)
  // ---------------------------------------------------------------------------------//
  {
    if (currentVisible == type)
      return;

    currentVisible = type;

    switch (type)
    {
      case 1:
        idColumn.setVisible (true);
        sizeColumn.setVisible (true);
        initColumn.setVisible (true);
        createdColumn.setVisible (true);
        modifiedColumn.setVisible (true);
        timeColumn.setVisible (true);
        typeColumn.setVisible (true);
        versionColumn.setVisible (true);

        epaColumn.setVisible (false);
        storageColumn.setVisible (false);
        aModeColumn.setVisible (false);
        rModeColumn.setVisible (false);
        apfColumn.setVisible (false);
        attrColumn.setVisible (false);
        break;

      case 2:
        idColumn.setVisible (false);
        sizeColumn.setVisible (false);
        initColumn.setVisible (false);
        createdColumn.setVisible (false);
        modifiedColumn.setVisible (false);
        timeColumn.setVisible (false);
        typeColumn.setVisible (false);
        versionColumn.setVisible (false);

        epaColumn.setVisible (true);
        storageColumn.setVisible (true);
        aModeColumn.setVisible (true);
        rModeColumn.setVisible (true);
        apfColumn.setVisible (true);
        attrColumn.setVisible (true);
        break;
    }
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, Number> addNumber (String heading, String name, int width)
  // ---------------------------------------------------------------------------------//
  {
    TableColumn<CatalogEntryItem, Number> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (numberCellFactory ());
    column.setMinWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, String> addString (String heading, String name, int width,
      String alignment)
  // ---------------------------------------------------------------------------------//
  {
    TableColumn<CatalogEntryItem, String> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (stringCellFactory (alignment));
    column.setPrefWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, LocalDate> addLocalDate (String heading, String name,
      int width)
  // ---------------------------------------------------------------------------------//
  {
    TableColumn<CatalogEntryItem, LocalDate> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (localDateCellFactory ());
    column.setPrefWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, FileType> addFileType (String heading, String name,
      int width, String alignment)
  // ---------------------------------------------------------------------------------//
  {
    TableColumn<CatalogEntryItem, FileType> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (fileTypeCellFactory (alignment));
    column.setPrefWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  Callback<TableColumn<CatalogEntryItem, Number>, TableCell<CatalogEntryItem, Number>>
      numberCellFactory ()
  // ---------------------------------------------------------------------------------//
  {
    return new Callback<TableColumn<CatalogEntryItem, Number>, TableCell<CatalogEntryItem, Number>> ()
    {
      @Override
      public TableCell<CatalogEntryItem, Number>
          call (TableColumn<CatalogEntryItem, Number> param)
      {
        TableCell<CatalogEntryItem, Number> cell = new TableCell<> ()
        {
          @Override
          public void updateItem (final Number item, boolean empty)
          {
            super.updateItem (item, empty);
            setStyle ("-fx-alignment: CENTER-RIGHT;");
            if (item == null || empty)
              setText (null);
            else
            {
              setText (String.format ("%,d", item));
              setFont (font);
            }
          }
        };
        return cell;
      }
    };
  }

  // ---------------------------------------------------------------------------------//
  Callback<TableColumn<CatalogEntryItem, String>, TableCell<CatalogEntryItem, String>>
      stringCellFactory (String alignment)
  // ---------------------------------------------------------------------------------//
  {
    return new Callback<TableColumn<CatalogEntryItem, String>, TableCell<CatalogEntryItem, String>> ()
    {
      @Override
      public TableCell<CatalogEntryItem, String>
          call (TableColumn<CatalogEntryItem, String> param)
      {
        TableCell<CatalogEntryItem, String> cell = new TableCell<> ()
        {
          @Override
          public void updateItem (final String item, boolean empty)
          {
            super.updateItem (item, empty);
            setStyle ("-fx-alignment: " + alignment + ";");
            if (item == null || empty)
              setText (null);
            else
            {
              setText (item);
              setFont (font);
            }
          }
        };
        return cell;
      }
    };
  }

  // ---------------------------------------------------------------------------------//
  Callback<TableColumn<CatalogEntryItem, LocalDate>, TableCell<CatalogEntryItem, LocalDate>>
      localDateCellFactory ()
  // ---------------------------------------------------------------------------------//
  {
    return new Callback<TableColumn<CatalogEntryItem, LocalDate>, TableCell<CatalogEntryItem, LocalDate>> ()
    {
      @Override
      public TableCell<CatalogEntryItem, LocalDate>
          call (TableColumn<CatalogEntryItem, LocalDate> param)
      {
        TableCell<CatalogEntryItem, LocalDate> cell =
            new TableCell<CatalogEntryItem, LocalDate> ()
            {
              @Override
              public void updateItem (final LocalDate item, boolean empty)
              {
                super.updateItem (item, empty);
                setStyle ("-fx-alignment: CENTER;");
                if (item == null || empty)
                  setText (null);
                else
                {
                  setText (String.format ("%s", item));
                  setFont (font);
                }
              }
            };
        return cell;
      }
    };
  }

  // ---------------------------------------------------------------------------------//
  Callback<TableColumn<CatalogEntryItem, FileType>, TableCell<CatalogEntryItem, FileType>>
      fileTypeCellFactory (String alignment)
  // ---------------------------------------------------------------------------------//
  {
    return new Callback<TableColumn<CatalogEntryItem, FileType>, TableCell<CatalogEntryItem, FileType>> ()
    {
      @Override
      public TableCell<CatalogEntryItem, FileType>
          call (TableColumn<CatalogEntryItem, FileType> param)
      {
        TableCell<CatalogEntryItem, FileType> cell =
            new TableCell<CatalogEntryItem, FileType> ()
            {
              @Override
              public void updateItem (final FileType item, boolean empty)
              {
                super.updateItem (item, empty);
                setStyle ("-fx-alignment: " + alignment + ";");
                if (item == null || empty)
                  setText (null);
                else
                {
                  setText (item == FileType.BIN ? "" : String.format ("%s", item));
                  setFont (font);
                }
              }
            };
        return cell;
      }
    };
  }

  // ---------------------------------------------------------------------------------//
  void exit ()
  // ---------------------------------------------------------------------------------//
  {
    prefs.putInt (PREFS_LAST_MEMBER_INDEX, getSelectionModel ().getSelectedIndex ());
  }

  // ---------------------------------------------------------------------------------//
  void restore ()
  // ---------------------------------------------------------------------------------//
  {
    int index = prefs.getInt (PREFS_LAST_MEMBER_INDEX, 0);
    select (index);
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
    if (dataset != null && dataset.getDisposition ().getOrg () == Org.PDS)
    {
      for (CatalogEntry catalogEntry : ((PdsDataset) dataset).getCatalogEntries ())
        items.add (new CatalogEntryItem (catalogEntry));

      select (selectedMembers.containsKey (dataset.getReader ())
          ? memberIndex (selectedMembers.get (dataset.getReader ())) : 0);

      CatalogEntry ce = ((PdsDataset) dataset).getCatalogEntries ().get (0);
      setVisibleColumns (ce.isBasic () ? 1 : 2);
    }
  }

  // ---------------------------------------------------------------------------------//
  private int memberIndex (String memberName)
  // ---------------------------------------------------------------------------------//
  {
    int index = 0;
    for (CatalogEntry catalogEntry : ((PdsDataset) dataset).getCatalogEntries ())
    {
      if (memberName.equals (catalogEntry.getMemberName ()))
        return index;
      ++index;
    }
    return 0;
  }

  // ---------------------------------------------------------------------------------//
  private void select (int index)
  // ---------------------------------------------------------------------------------//
  {
    getFocusModel ().focus (index);
    getSelectionModel ().select (index);
    scrollTo (index);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    this.font = font;
    refresh ();
  }
}
