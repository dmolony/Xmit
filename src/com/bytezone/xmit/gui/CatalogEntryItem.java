package com.bytezone.xmit.gui;

import com.bytezone.xmit.CatalogEntry;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CatalogEntryItem
{
  CatalogEntry catalogEntry;
  private StringProperty memberName;
  private StringProperty userName;
  private IntegerProperty size;

  public CatalogEntryItem (CatalogEntry catalogEntry)
  {
    this.catalogEntry = catalogEntry;
    setMemberName (catalogEntry.getMemberName ());
    setUserName (catalogEntry.getUserName ());
    setSize (catalogEntry.getSize ());
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
}
