package com.bytezone.xmit.gui;

import java.time.LocalDate;

import com.bytezone.xmit.BasicModule;
import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.LoadModule;
import com.bytezone.xmit.Utility.FileType;

import javafx.beans.property.*;

// -----------------------------------------------------------------------------------//
public class CatalogEntryItem                   // must be public
// -----------------------------------------------------------------------------------//
{
  private final CatalogEntry catalogEntry;

  private StringProperty memberName;
  private StringProperty userName;
  private StringProperty aliasName;

  // basic module
  private StringProperty version;
  private StringProperty time;
  private ObjectProperty<FileType> type;
  private IntegerProperty size;
  private IntegerProperty bytes;
  private IntegerProperty init;
  private ObjectProperty<LocalDate> dateCreated;
  private ObjectProperty<LocalDate> dateModified;

  // load module
  private IntegerProperty epa;
  private IntegerProperty storage;
  private IntegerProperty aMode;
  private IntegerProperty rMode;
  private IntegerProperty ssi;
  private StringProperty apf;
  private StringProperty attr;

  // ---------------------------------------------------------------------------------//
  CatalogEntryItem (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    this.catalogEntry = catalogEntry;

    setMemberName (catalogEntry.getMemberName ());
    setAliasName (catalogEntry.getAliasName ());
    setBytes (catalogEntry.getDataLength ());
    setType (catalogEntry.getFileType ());

    switch (catalogEntry.getModuleType ())
    {
      case BASIC:
        BasicModule module = (BasicModule) catalogEntry;
        setUserName (module.getUserName ());
        setSize (module.getSize ());
        setInit (module.getInit ());
        setDateCreated (module.getDateCreated ());
        setDateModified (module.getDateModified ());
        setTime (module.getTime ());
        setVersion (module.getVersion ());
        break;

      case LOAD:
        LoadModule loadModule = (LoadModule) catalogEntry;
        setEpa (loadModule.getEpa ());
        setStorage (loadModule.getStorage ());
        setAMode (loadModule.getAMode ());
        setRMode (loadModule.getRMode ());
        setSsi ((int) loadModule.getSsi ());
        setApf (loadModule.isApf () ? "apf" : "");
        setAttr (String.format ("%2s %2s %2s %2s",    //
            loadModule.isReentrant () ? "RN" : "",        //
            loadModule.isReusable () ? "RU" : "",         //
            loadModule.isOverlay () ? "OV" : "",          //
            loadModule.isTest () ? "TS" : ""));
        break;
    }
  }

  // ---------------------------------------------------------------------------------//
  CatalogEntry getCatalogEntry ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogEntry;
  }

  // ---------------------------------------------------------------------------------//
  private void setMemberName (String value)
  // ---------------------------------------------------------------------------------//
  {
    memberNameProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final String getMemberName ()
  // ---------------------------------------------------------------------------------//
  {
    return memberNameProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private StringProperty memberNameProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (memberName == null)
      memberName = new SimpleStringProperty ();
    return memberName;
  }

  // ---------------------------------------------------------------------------------//
  private void setUserName (String value)
  // ---------------------------------------------------------------------------------//
  {
    userNameProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final String getUserName ()
  // ---------------------------------------------------------------------------------//
  {
    return userNameProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private StringProperty userNameProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (userName == null)
      userName = new SimpleStringProperty ();
    return userName;
  }

  // ---------------------------------------------------------------------------------//
  private void setAliasName (String value)
  // ---------------------------------------------------------------------------------//
  {
    aliasNameProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final String getAliasName ()
  // ---------------------------------------------------------------------------------//
  {
    return aliasNameProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private StringProperty aliasNameProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (aliasName == null)
      aliasName = new SimpleStringProperty ();
    return aliasName;
  }

  // ---------------------------------------------------------------------------------//
  private void setSize (int value)
  // ---------------------------------------------------------------------------------//
  {
    sizeProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final int getSize ()
  // ---------------------------------------------------------------------------------//
  {
    return sizeProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private IntegerProperty sizeProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (size == null)
      size = new SimpleIntegerProperty ();
    return size;
  }

  // ---------------------------------------------------------------------------------//
  private void setBytes (int value)
  // ---------------------------------------------------------------------------------//
  {
    bytesProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final int getBytes ()
  // ---------------------------------------------------------------------------------//
  {
    return bytesProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private IntegerProperty bytesProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (bytes == null)
      bytes = new SimpleIntegerProperty ();
    return bytes;
  }

  // ---------------------------------------------------------------------------------//
  private void setInit (int value)
  // ---------------------------------------------------------------------------------//
  {
    initProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final int getInit ()
  // ---------------------------------------------------------------------------------//
  {
    return initProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private IntegerProperty initProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (init == null)
      init = new SimpleIntegerProperty ();
    return init;
  }

  // ---------------------------------------------------------------------------------//
  private void setDateCreated (LocalDate value)
  // ---------------------------------------------------------------------------------//
  {
    dateCreatedProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public LocalDate getDateCreated ()
  // ---------------------------------------------------------------------------------//
  {
    return dateCreatedProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private ObjectProperty<LocalDate> dateCreatedProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (dateCreated == null)
      dateCreated = new SimpleObjectProperty<> ();
    return dateCreated;
  }

  // ---------------------------------------------------------------------------------//
  private void setDateModified (LocalDate value)
  // ---------------------------------------------------------------------------------//
  {
    dateModifiedProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public LocalDate getDateModified ()
  // ---------------------------------------------------------------------------------//
  {
    return dateModifiedProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private ObjectProperty<LocalDate> dateModifiedProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (dateModified == null)
      dateModified = new SimpleObjectProperty<> ();
    return dateModified;
  }

  // ---------------------------------------------------------------------------------//
  private void setTime (String value)
  // ---------------------------------------------------------------------------------//
  {
    timeProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final String getTime ()
  // ---------------------------------------------------------------------------------//
  {
    return timeProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private StringProperty timeProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (time == null)
      time = new SimpleStringProperty ();
    return time;
  }

  // ---------------------------------------------------------------------------------//
  private void setType (FileType value)
  // ---------------------------------------------------------------------------------//
  {
    typeProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final FileType getType ()
  // ---------------------------------------------------------------------------------//
  {
    return typeProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private ObjectProperty<FileType> typeProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (type == null)
      type = new SimpleObjectProperty<> ();
    return type;
  }

  // ---------------------------------------------------------------------------------//
  private void setVersion (String value)
  // ---------------------------------------------------------------------------------//
  {
    versionProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final String getVersion ()
  // ---------------------------------------------------------------------------------//
  {
    return versionProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private StringProperty versionProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (version == null)
      version = new SimpleStringProperty ();
    return version;
  }

  // ---------------------------------------------------------------------------------//
  private void setStorage (int value)
  // ---------------------------------------------------------------------------------//
  {
    storageProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final int getStorage ()
  // ---------------------------------------------------------------------------------//
  {
    return storageProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private IntegerProperty storageProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (storage == null)
      storage = new SimpleIntegerProperty ();
    return storage;
  }

  // ---------------------------------------------------------------------------------//
  private void setEpa (int value)
  // ---------------------------------------------------------------------------------//
  {
    epaProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final int getEpa ()
  // ---------------------------------------------------------------------------------//
  {
    return epaProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private IntegerProperty epaProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (epa == null)
      epa = new SimpleIntegerProperty ();
    return epa;
  }

  // ---------------------------------------------------------------------------------//
  private void setAMode (int value)
  // ---------------------------------------------------------------------------------//
  {
    aModeProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final int getAMode ()
  // ---------------------------------------------------------------------------------//
  {
    return aModeProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private IntegerProperty aModeProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (aMode == null)
      aMode = new SimpleIntegerProperty ();
    return aMode;
  }

  // ---------------------------------------------------------------------------------//
  private void setRMode (int value)
  // ---------------------------------------------------------------------------------//
  {
    rModeProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final int getRMode ()
  // ---------------------------------------------------------------------------------//
  {
    return rModeProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private IntegerProperty rModeProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (rMode == null)
      rMode = new SimpleIntegerProperty ();
    return rMode;
  }

  // ---------------------------------------------------------------------------------//
  private void setSsi (int value)
  // ---------------------------------------------------------------------------------//
  {
    ssiProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final int getSsi ()
  // ---------------------------------------------------------------------------------//
  {
    return ssiProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private IntegerProperty ssiProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (ssi == null)
      ssi = new SimpleIntegerProperty ();
    return ssi;
  }

  // ---------------------------------------------------------------------------------//
  private void setApf (String value)
  // ---------------------------------------------------------------------------------//
  {
    apfProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final String getApf ()
  // ---------------------------------------------------------------------------------//
  {
    return apfProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private StringProperty apfProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (apf == null)
      apf = new SimpleStringProperty ();
    return apf;
  }

  // ---------------------------------------------------------------------------------//
  private void setAttr (String value)
  // ---------------------------------------------------------------------------------//
  {
    attrProperty ().set (value);
  }

  // ---------------------------------------------------------------------------------//
  public final String getAttr ()
  // ---------------------------------------------------------------------------------//
  {
    return attrProperty ().get ();
  }

  // ---------------------------------------------------------------------------------//
  private StringProperty attrProperty ()
  // ---------------------------------------------------------------------------------//
  {
    if (attr == null)
      attr = new SimpleStringProperty ();
    return attr;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogEntry.toString ();
  }
}
