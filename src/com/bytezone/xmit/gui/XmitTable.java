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
import com.bytezone.xmit.textunit.Dsorg.Org;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class XmitTable extends TableView<CatalogEntryItem>
    implements TreeItemSelectionListener
{
  private static final String PREFS_LAST_MEMBER_INDEX = "LastMemberIndex";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final List<TableItemSelectionListener> listeners = new ArrayList<> ();
  final ObservableList<CatalogEntryItem> items = FXCollections.observableArrayList ();

  private Reader reader;
  private Dataset dataset;
  private final Map<Reader, String> selectedMembers = new HashMap<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  XmitTable ()
  {
    setStyle ("-fx-font-size: 13; -fx-font-family: monospaced");
    setItems (items);

    addString ("Member", "MemberName", 100, "CENTER-LEFT");
    addString ("User", "UserName", 100, "CENTER-LEFT");
    addNumber ("Size", "Size", 70);
    addLocalDate ("Created", "DateCreated", 100);
    addLocalDate ("Modified", "DateModified", 100);
    addString ("Time", "Time", 90, "CENTER");
    addString ("Version", "Version", 80, "CENTER");
    addString ("Alias", "AliasName", 100, "CENTER-LEFT");

    getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) ->
        {
          if (newSelection == null)
            return;

          selectedMembers.put (reader, newSelection.catalogEntry.getMemberName ());
          for (TableItemSelectionListener listener : listeners)
            listener.tableItemSelected (newSelection.catalogEntry);
        });
  }

  // ---------------------------------------------------------------------------------//
  // addNumber
  // ---------------------------------------------------------------------------------//

  TableColumn<CatalogEntryItem, Number> addNumber (String heading, String name, int width)
  {
    TableColumn<CatalogEntryItem, Number> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (numberCellFactory ());
    column.setMinWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  // addString
  // ---------------------------------------------------------------------------------//

  TableColumn<CatalogEntryItem, String> addString (String heading, String name, int width,
      String alignment)
  {
    TableColumn<CatalogEntryItem, String> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (stringCellFactory (alignment));
    column.setPrefWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  // addLocalDate
  // ---------------------------------------------------------------------------------//

  TableColumn<CatalogEntryItem, LocalDate> addLocalDate (String heading, String name,
      int width)
  {
    TableColumn<CatalogEntryItem, LocalDate> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (localDateCellFactory ());
    column.setPrefWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  // numberCellFactory
  // ---------------------------------------------------------------------------------//

  Callback<TableColumn<CatalogEntryItem, Number>, TableCell<CatalogEntryItem, Number>>
      numberCellFactory ()
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
              setText (String.format ("%,d", item));
          }
        };
        return cell;
      }
    };
  }

  // ---------------------------------------------------------------------------------//
  // stringCellFactory
  // ---------------------------------------------------------------------------------//

  Callback<TableColumn<CatalogEntryItem, String>, TableCell<CatalogEntryItem, String>>
      stringCellFactory (String alignment)
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
              setText (item);
          }
        };
        return cell;
      }
    };
  }

  // ---------------------------------------------------------------------------------//
  // localDateCellFactory
  // ---------------------------------------------------------------------------------//

  Callback<TableColumn<CatalogEntryItem, LocalDate>, TableCell<CatalogEntryItem, LocalDate>>
      localDateCellFactory ()
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
                  setText (String.format ("%s", item));
              }
            };
        return cell;
      }
    };
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.putInt (PREFS_LAST_MEMBER_INDEX, getSelectionModel ().getSelectedIndex ());
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  void restore ()
  {
    int index = prefs.getInt (PREFS_LAST_MEMBER_INDEX, 0);
    select (index);
  }

  // ---------------------------------------------------------------------------------//
  // addListener
  // ---------------------------------------------------------------------------------//

  void addListener (TableItemSelectionListener listener)
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  // removeListener
  // ---------------------------------------------------------------------------------//

  void removeListener (TableItemSelectionListener listener)
  {
    listeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  // treeItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemSelected (Reader reader, Dataset dataset)
  {
    this.reader = reader;
    this.dataset = dataset;

    items.clear ();
    if (dataset != null && dataset.getOrg () == Org.PDS)
    {
      for (CatalogEntry catalogEntry : ((PdsDataset) dataset).getMembers ())
        items.add (new CatalogEntryItem (catalogEntry));

      select (selectedMembers.containsKey (reader)
          ? memberIndex (selectedMembers.get (reader)) : 0);
    }
  }

  // ---------------------------------------------------------------------------------//
  // memberIndex
  // ---------------------------------------------------------------------------------//

  private int memberIndex (String memberName)
  {
    int index = 0;
    for (CatalogEntry catalogEntry : ((PdsDataset) dataset).getMembers ())
    {
      if (memberName.equals (catalogEntry.getMemberName ()))
        return index;
      ++index;
    }
    return 0;
  }

  // ---------------------------------------------------------------------------------//
  // select
  // ---------------------------------------------------------------------------------//

  private void select (int index)
  {
    getFocusModel ().focus (index);
    getSelectionModel ().select (index);
    scrollTo (index);
  }
}
