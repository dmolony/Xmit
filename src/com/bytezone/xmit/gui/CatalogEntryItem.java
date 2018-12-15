package com.bytezone.xmit.gui;

import java.time.LocalDate;

import com.bytezone.xmit.CatalogEntry;

import javafx.beans.property.*;

public class CatalogEntryItem
{
  CatalogEntry catalogEntry;
  private StringProperty memberName;
  private StringProperty userName;
  private StringProperty aliasName;
  private StringProperty time;
  private IntegerProperty size;
  private ObjectProperty<LocalDate> date;

  public CatalogEntryItem (CatalogEntry catalogEntry)
  {
    this.catalogEntry = catalogEntry;
    setMemberName (catalogEntry.getMemberName ());
    setUserName (catalogEntry.getUserName ());
    setAliasName (catalogEntry.getAliasName ());
    setSize (catalogEntry.getSize ());
    setDate (catalogEntry.getDate ());
    setTime (catalogEntry.getTime ());
  }

  // ---------------------------------------------------------------------------------//
  // MemberName
  // ---------------------------------------------------------------------------------//

  private void setMemberName (String value)
  {
    memberNameProperty ().set (value);
  }

  public final String getMemberName ()
  {
    return memberNameProperty ().get ();
  }

  private StringProperty memberNameProperty ()
  {
    if (memberName == null)
      memberName = new SimpleStringProperty ();
    return memberName;
  }

  // ---------------------------------------------------------------------------------//
  // UserName
  // ---------------------------------------------------------------------------------//

  private void setUserName (String value)
  {
    userNameProperty ().set (value);
  }

  public final String getUserName ()
  {
    return userNameProperty ().get ();
  }

  private StringProperty userNameProperty ()
  {
    if (userName == null)
      userName = new SimpleStringProperty ();
    return userName;
  }

  // ---------------------------------------------------------------------------------//
  // AliasName
  // ---------------------------------------------------------------------------------//

  private void setAliasName (String value)
  {
    aliasNameProperty ().set (value);
  }

  public final String getAliasName ()
  {
    return aliasNameProperty ().get ();
  }

  private StringProperty aliasNameProperty ()
  {
    if (aliasName == null)
      aliasName = new SimpleStringProperty ();
    return aliasName;
  }

  // ---------------------------------------------------------------------------------//
  // Days
  // ---------------------------------------------------------------------------------//

  private void setSize (int value)
  {
    sizeProperty ().set (value);
  }

  public final int getSize ()
  {
    return sizeProperty ().get ();
  }

  private IntegerProperty sizeProperty ()
  {
    if (size == null)
      size = new SimpleIntegerProperty ();
    return size;
  }

  // ---------------------------------------------------------------------------------//
  // FirstDate
  // ---------------------------------------------------------------------------------//

  private void setDate (LocalDate value)
  {
    dateProperty ().set (value);
  }

  public LocalDate getDate ()
  {
    return dateProperty ().get ();
  }

  private ObjectProperty<LocalDate> dateProperty ()
  {
    if (date == null)
      date = new SimpleObjectProperty<> ();
    return date;
  }

  // ---------------------------------------------------------------------------------//
  // AliasName
  // ---------------------------------------------------------------------------------//

  private void setTime (String value)
  {
    timeProperty ().set (value);
  }

  public final String getTime ()
  {
    return timeProperty ().get ();
  }

  private StringProperty timeProperty ()
  {
    if (time == null)
      time = new SimpleStringProperty ();
    return time;
  }
}
