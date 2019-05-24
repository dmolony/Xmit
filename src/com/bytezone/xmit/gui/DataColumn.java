package com.bytezone.xmit.gui;

import java.time.LocalDate;
import java.util.prefs.Preferences;

import com.bytezone.xmit.Utility.FileType;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.util.Callback;

// -----------------------------------------------------------------------------------//
abstract class DataColumn<T> implements Comparable<DataColumn<?>>
// -----------------------------------------------------------------------------------//
{
  private static final String PREFS_SEQUENCE = "Sequence-";
  private static final String PREFS_WIDTH = "Width-";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private static final int PIXELS_PER_CHAR = 11;
  private static int seq = 0;
  static Font font;

  private final int sequence;
  private final String columnHeading;
  private final String propertyName;
  private final int widthInCharacters;
  private final DisplayType displayType;
  String alignment;

  private final double savedWidth;
  private final int savedSequence;

  TableColumn<CatalogEntryItem, T> column;

  enum DisplayType
  {
    ALL, BASIC, LOAD
  }

  // ---------------------------------------------------------------------------------//
  public DataColumn (String heading, String name, int width, String alignment,
      DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    this.sequence = seq++;
    this.columnHeading = heading;
    this.propertyName = name;
    this.widthInCharacters = width;
    this.displayType = displayType;
    this.alignment = "-fx-alignment: " + alignment + ";";

    savedWidth = prefs.getDouble (PREFS_WIDTH + columnHeading,
        widthInCharacters * PIXELS_PER_CHAR);
    savedSequence = prefs.getInt (PREFS_SEQUENCE + columnHeading, sequence);
  }

  // ---------------------------------------------------------------------------------//
  void save (int sequence)
  // ---------------------------------------------------------------------------------//
  {
    prefs.putInt (PREFS_SEQUENCE + columnHeading, sequence);
    prefs.putDouble (PREFS_WIDTH + columnHeading, column.getWidth ());
  }

  // ---------------------------------------------------------------------------------//
  TableColumn<CatalogEntryItem, T> createColumn ()
  // ---------------------------------------------------------------------------------//
  {
    column = new TableColumn<> (columnHeading);
    column.setCellFactory (createCallback ());
    column.setCellValueFactory (new PropertyValueFactory<> (propertyName));
    column.setPrefWidth (savedWidth);
    column.setMinWidth (widthInCharacters * PIXELS_PER_CHAR);
    column.setUserData (this);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  boolean matches (DisplayType displayType)
  // ---------------------------------------------------------------------------------//
  {
    return this.displayType == DisplayType.ALL || this.displayType == displayType;
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
        propertyName, widthInCharacters, displayType);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int compareTo (DataColumn<?> o)
  // ---------------------------------------------------------------------------------//
  {
    return this.savedSequence - o.savedSequence;
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
    super (heading, name, width, alignment, displayType);
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
    super (heading, name, width, alignment, displayType);
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
    super (heading, name, width, "CENTER", displayType);
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
    super (heading, name, width, alignment, displayType);
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
