package com.bytezone.xmit.gui;

import java.time.LocalDate;

import com.bytezone.xmit.Utility.FileType;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.util.Callback;

// ---------------------------------------------------------------------------------//
class DataColumn
// ---------------------------------------------------------------------------------//
{
  private static final int PIXELS_PER_CHAR = 11;
  static Font font;

  String heading;
  String name;
  int width;
  DisplayType displayType;

  TableColumn<CatalogEntryItem, ?> column;

  enum DisplayType
  {
    All, Basic, Load
  }

  // ---------------------------------------------------------------------------------//
  public DataColumn (String heading, String name, int width, DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    this.heading = heading;
    this.name = name;
    this.width = width;
    this.displayType = displayType;
  }

  // ---------------------------------------------------------------------------------//
  void setWidth (TableColumn<CatalogEntryItem, ?> column, int width)
  // ---------------------------------------------------------------------------------//
  {
    int columnWidth = width * PIXELS_PER_CHAR;
    column.setPrefWidth (columnWidth);
    column.setMinWidth (columnWidth);
  }
}

// ---------------------------------------------------------------------------------//
class StringColumn extends DataColumn
// ---------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public StringColumn (String heading, String name, int width, String alignment,
      DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    super (heading, name, width, displayType);
    column = addString (heading, name, width, alignment);
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, String> addString (String heading, String name, int width,
      String alignment)
  // ---------------------------------------------------------------------------------//
  {
    TableColumn<CatalogEntryItem, String> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (stringCellFactory (alignment));
    setWidth (column, width);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  Callback<TableColumn<CatalogEntryItem, String>, TableCell<CatalogEntryItem, String>>
      stringCellFactory (String alignment)
  // ---------------------------------------------------------------------------------//
  {
    return new Callback<TableColumn<CatalogEntryItem, String>, //
        TableCell<CatalogEntryItem, String>> ()
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
}

//---------------------------------------------------------------------------------//
class NumberColumn extends DataColumn
//---------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public NumberColumn (String heading, String name, int width, String mask,
      String alignment, DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    super (heading, name, width, displayType);
    column = addNumber (heading, name, width, mask, alignment);
  }

  // ---------------------------------------------------------------------------------//
  public NumberColumn (String heading, String name, int width, DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    super (heading, name, width, displayType);
    column = addNumber (heading, name, width, "%,d", "CENTER-RIGHT");
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, Number> addNumber (String heading, String name, int width,
      String mask, String alignment)
  // ---------------------------------------------------------------------------------//
  {
    TableColumn<CatalogEntryItem, Number> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (numberCellFactory (mask, alignment));
    setWidth (column, width);
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
}

class LocalDateColumn extends DataColumn
{
  // ---------------------------------------------------------------------------------//
  public LocalDateColumn (String heading, String name, int width, DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    super (heading, name, width, displayType);
    column = addLocalDate (heading, name, width);
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, LocalDate> addLocalDate (String heading, String name,
      int width)
  // ---------------------------------------------------------------------------------//
  {
    TableColumn<CatalogEntryItem, LocalDate> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (localDateCellFactory ());
    setWidth (column, width);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  Callback<TableColumn<CatalogEntryItem, LocalDate>, TableCell<CatalogEntryItem, LocalDate>>
      localDateCellFactory ()
  // ---------------------------------------------------------------------------------//
  {
    return new Callback<TableColumn<CatalogEntryItem, LocalDate>,         //
        TableCell<CatalogEntryItem, LocalDate>> ()
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
}

class FileTypeColumn extends DataColumn
{
  // ---------------------------------------------------------------------------------//
  public FileTypeColumn (String heading, String name, int width, String alignment,
      DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    super (heading, name, width, displayType);
    column = addFileType (heading, name, width, alignment);
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, FileType> addFileType (String heading, String name,
      int width, String alignment)
  // ---------------------------------------------------------------------------------//
  {
    TableColumn<CatalogEntryItem, FileType> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (fileTypeCellFactory (alignment));
    setWidth (column, width);
    return column;
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
}
