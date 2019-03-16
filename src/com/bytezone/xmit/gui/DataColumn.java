package com.bytezone.xmit.gui;

import java.time.LocalDate;

import com.bytezone.xmit.Utility.FileType;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.util.Callback;

// ---------------------------------------------------------------------------------//
abstract class DataColumn<T>
// ---------------------------------------------------------------------------------//
{
  private static final int PIXELS_PER_CHAR = 11;
  private static int seq = 0;
  static Font font;

  int sequence;
  String columnHeading;
  String propertyName;
  int width;
  DisplayType displayType;
  String alignment;

  TableColumn<CatalogEntryItem, T> column;

  enum DisplayType
  {
    ALL, BASIC, LOAD
  }

  // ---------------------------------------------------------------------------------//
  public DataColumn (String heading, String name, int width, DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    this.sequence = seq++;
    this.columnHeading = heading;
    this.propertyName = name;
    this.width = width;
    this.displayType = displayType;
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, T> createColumn ()
  // ---------------------------------------------------------------------------------//
  {
    column = new TableColumn<> (columnHeading);
    column.setCellFactory (createCallback ());
    column.setCellValueFactory (new PropertyValueFactory<> (propertyName));
    int columnWidth = width * PIXELS_PER_CHAR;
    column.setPrefWidth (columnWidth);
    column.setMinWidth (columnWidth);
    column.setUserData (this);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  abstract Callback<TableColumn<CatalogEntryItem, T>, TableCell<CatalogEntryItem, T>>
      createCallback ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%2d  %-15s %-15s %4d  %s", sequence, columnHeading,
        propertyName, width, displayType);
  }
}

// ---------------------------------------------------------------------------------//
class StringColumn extends DataColumn<String>
// ---------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public StringColumn (String heading, String name, int width, String alignment,
      DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    super (heading, name, width, displayType);
    this.alignment = "-fx-alignment: " + alignment + ";";
  }

  // ---------------------------------------------------------------------------------//
  @Override
  Callback<TableColumn<CatalogEntryItem, String>, TableCell<CatalogEntryItem, String>>
      createCallback ()
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
            setStyle (alignment);
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
class NumberColumn extends DataColumn<Number>
//---------------------------------------------------------------------------------//
{
  final String mask;

  // ---------------------------------------------------------------------------------//
  public NumberColumn (String heading, String name, int width, String mask,
      String alignment, DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    super (heading, name, width, displayType);
    this.alignment = "-fx-alignment: " + alignment + ";";
    this.mask = mask;
  }

  // ---------------------------------------------------------------------------------//
  public NumberColumn (String heading, String name, int width, DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    this (heading, name, width, "%,d", "CENTER-RIGHT", displayType);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  Callback<TableColumn<CatalogEntryItem, Number>, TableCell<CatalogEntryItem, Number>>
      createCallback ()
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
            setStyle (alignment);
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

// ---------------------------------------------------------------------------------//
class LocalDateColumn extends DataColumn<LocalDate>
// ---------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public LocalDateColumn (String heading, String name, int width, DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    super (heading, name, width, displayType);
    this.alignment = "-fx-alignment: CENTER;";
  }

  // ---------------------------------------------------------------------------------//
  @Override
  Callback<TableColumn<CatalogEntryItem, LocalDate>, TableCell<CatalogEntryItem, LocalDate>>
      createCallback ()
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
                setStyle (alignment);
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

// ---------------------------------------------------------------------------------//
class FileTypeColumn extends DataColumn<FileType>
// ---------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public FileTypeColumn (String heading, String name, int width, String alignment,
      DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    super (heading, name, width, displayType);
    this.alignment = "-fx-alignment: " + alignment + ";";
  }

  // ---------------------------------------------------------------------------------//
  @Override
  Callback<TableColumn<CatalogEntryItem, FileType>, TableCell<CatalogEntryItem, FileType>>
      createCallback ()
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
                setStyle (alignment);
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
