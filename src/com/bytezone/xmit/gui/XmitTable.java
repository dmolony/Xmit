package com.bytezone.xmit.gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.CatalogEntry.ModuleType;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.Utility.FileType;

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
  private static final int CHAR_8 = 75;
  private static final int CHAR_10 = 100;
  private static final String PREFS_LAST_MEMBER_INDEX = "LastMemberIndex";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final List<TableItemSelectionListener> listeners = new ArrayList<> ();
  private final ObservableList<CatalogEntryItem> items =
      FXCollections.observableArrayList ();

  private Dataset dataset;
  private final Map<Dataset, String> selectedMembers = new HashMap<> ();
  private Font font;

  private ModuleType currentVisibleType = null;

  private final List<TableColumn<CatalogEntryItem, ?>> basicColumns;
  private final List<TableColumn<CatalogEntryItem, ?>> loadColumns;

  // ---------------------------------------------------------------------------------//
  XmitTable ()
  // ---------------------------------------------------------------------------------//
  {
    setItems (items);

    // common
    addString ("Member", "MemberName", CHAR_8, "CENTER-LEFT");
    addNumber ("Bytes", "Bytes", 90);

    basicColumns = Arrays.asList (                                //
        addString ("Id", "UserName", CHAR_8, "CENTER-LEFT"),      //
        addNumber ("Size", "Size", 70),                           //
        addNumber ("Init", "Init", 70),                           //
        addLocalDate ("Created", "DateCreated", CHAR_10),         //
        addLocalDate ("Modified", "DateModified", CHAR_10),       //
        addString ("Time", "Time", CHAR_8, "CENTER"),             //
        addFileType ("Type", "Type", 50, "CENTER"),               //
        addString ("ver.mod", "Version", 70, "CENTER"));

    loadColumns = Arrays.asList (                                 //
        addNumber ("Storage", "storage", 70, "%06X", "CENTER"),   //
        addNumber ("Entry", "epa", 70, "%06X", "CENTER"),         //
        addString ("APF", "apf", 40, "CENTER"),                   //
        addNumber ("amode", "aMode", 30),                         //
        addNumber ("rmode", "rMode", 30),                         //
        addNumber ("ssi", "ssi", CHAR_8, "%08X", "CENTER"),       //
        addString ("Attributes", "attr", 100, "CENTER-LEFT"));

    // common
    addString ("Alias", "AliasName", CHAR_8, "CENTER-LEFT");

    getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, catalogEntryItem) ->
        {
          if (catalogEntryItem == null)
            return;

          CatalogEntry catalogEntry = catalogEntryItem.getCatalogEntry ();
          selectedMembers.put (dataset, catalogEntry.getMemberName ());
          for (TableItemSelectionListener listener : listeners)
            listener.tableItemSelected (catalogEntry);
        });
  }

  // ---------------------------------------------------------------------------------//
  void setVisibleColumns (ModuleType type)
  // ---------------------------------------------------------------------------------//
  {
    if (currentVisibleType == type)
      return;

    currentVisibleType = type;

    for (var column : basicColumns)
      column.setVisible (type == ModuleType.BASIC);

    for (var column : loadColumns)
      column.setVisible (type == ModuleType.LOAD);
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, Number> addNumber (String heading, String name, int width)
  // ---------------------------------------------------------------------------------//
  {
    return addNumber (heading, name, width, "%,d", "CENTER-RIGHT");
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, Number> addNumber (String heading, String name, int width,
      String mask, String alignment)
  // ---------------------------------------------------------------------------------//
  {
    TableColumn<CatalogEntryItem, Number> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (numberCellFactory (mask, alignment));
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
      numberCellFactory (String mask, String alignment)
  // ---------------------------------------------------------------------------------//
  {
    return new Callback<TableColumn<CatalogEntryItem, Number>, //
        TableCell<CatalogEntryItem, Number>> ()
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
            setStyle ("-fx-alignment: " + alignment + ";");
            if (item == null || empty)
              setText (null);
            else
            {
              if (item.intValue () == 0)
                setText ("");
              else
                setText (String.format (mask, item));
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
    return new Callback<TableColumn<CatalogEntryItem, FileType>,          //
        TableCell<CatalogEntryItem, FileType>> ()
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
    if (dataset != null && dataset.isPds ())
    {
      PdsDataset pdsDataset = (PdsDataset) dataset;

      for (CatalogEntry catalogEntry : pdsDataset)
        items.add (new CatalogEntryItem (catalogEntry));

      select (selectedMembers.containsKey (dataset)
          ? pdsDataset.memberIndex (selectedMembers.get (dataset)) : 0);

      setVisibleColumns (pdsDataset.getModuleType ());
    }
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
