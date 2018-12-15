package com.bytezone.xmit.gui;

import java.io.File;
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
  private final Map<Reader, String> selectedMembers = new HashMap<> ();
  final ObservableList<CatalogEntryItem> items = FXCollections.observableArrayList ();

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
    addNumber ("Size", "Size", 80).setCellFactory (numberCellFactory ());

    getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) ->
        {
          if (newSelection == null)
            return;

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
  // exit
  // ---------------------------------------------------------------------------------//

  public void exit ()
  {
    prefs.putInt (PREFS_LAST_MEMBER_INDEX, getSelectionModel ().getSelectedIndex ());
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  public void restore ()
  {
    int index = prefs.getInt (PREFS_LAST_MEMBER_INDEX, 0);
    scrollTo (index);
    getSelectionModel ().select (index);
    getFocusModel ().focus (index);
  }

  // ---------------------------------------------------------------------------------//
  // addListener
  // ---------------------------------------------------------------------------------//

  public void addListener (TableItemSelectionListener listener)
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  // removeListener
  // ---------------------------------------------------------------------------------//

  public void removeListener (TableItemSelectionListener listener)
  {
    listeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  // treeItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemSelected (Reader reader)
  {
    items.clear ();
    for (CatalogEntry catalogEntry : reader.getCatalogEntries ())
      items.add (new CatalogEntryItem (catalogEntry));

    getSelectionModel ().select (0);
    scrollTo (0);
    getFocusModel ().focus (0);
  }

  // ---------------------------------------------------------------------------------//
  // treeItemExpanded
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemExpanded (TreeItem<File> treeItem)
  {
  }
}
