package com.bytezone.xmit.gui;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Reader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
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
  private final Map<Reader, String> selectedMembers = new HashMap<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  XmitTable ()
  {
    setStyle ("-fx-font-size: 13; -fx-font-family: monospaced");
    setItems (items);

    addString ("Member", "MemberName", 100)
        .setCellFactory (stringCellFactory ("CENTER-LEFT"));
    addString ("User", "UserName", 100)
        .setCellFactory (stringCellFactory ("CENTER-LEFT"));
    addString ("Alias", "AliasName", 100)
        .setCellFactory (stringCellFactory ("CENTER-LEFT"));
    addNumber ("Size", "Size", 70).setCellFactory (numberCellFactory ());
    addLocalDate ("Created", "DateCreated", 100).setCellFactory (localDateCellFactory ());
    addLocalDate ("Modified", "DateModified", 100)
        .setCellFactory (localDateCellFactory ());
    addString ("Time", "Time", 80).setCellFactory (stringCellFactory ("CENTER"));
    addString ("Version", "Version", 80).setCellFactory (stringCellFactory ("CENTER"));

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
    column.setMinWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  // addString
  // ---------------------------------------------------------------------------------//

  TableColumn<CatalogEntryItem, String> addString (String heading, String name, int width)
  {
    TableColumn<CatalogEntryItem, String> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
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
  public void treeItemSelected (Reader reader)
  {
    this.reader = reader;

    items.clear ();
    if (reader != null)
    {
      for (CatalogEntry catalogEntry : reader.getCatalogEntries ())
        items.add (new CatalogEntryItem (catalogEntry));

      select (selectedMembers.containsKey (reader)
          ? memberIndex (selectedMembers.get (reader)) : 0);
    }
  }

  // ---------------------------------------------------------------------------------//
  // treeItemExpanded
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemExpanded (TreeItem<File> treeItem)
  {
  }

  // ---------------------------------------------------------------------------------//
  // memberIndex
  // ---------------------------------------------------------------------------------//

  private int memberIndex (String memberName)
  {
    int index = 0;
    for (CatalogEntry catalogEntry : reader.getCatalogEntries ())
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
