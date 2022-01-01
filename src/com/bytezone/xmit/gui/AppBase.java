package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

// -----------------------------------------------------------------------------------//
public abstract class AppBase extends Application
// -----------------------------------------------------------------------------------//
{
  protected static final String PREFS_WINDOW_LOCATION = "WindowLocation";
  protected final Preferences prefs = getPreferences ();

  protected Stage primaryStage;
  protected final MenuBar menuBar = new MenuBar ();
  protected final BorderPane mainPane = new BorderPane ();
  protected final WindowStatus windowStatus = getWindowStatus ();
  protected Scene scene;                                    // not sure if this is needed

  protected final List<SaveState> saveStateList = new ArrayList<> ();

  protected boolean debug = false;

  abstract Parent createContent ();

  abstract Preferences getPreferences ();

  abstract WindowStatus getWindowStatus ();

  // ---------------------------------------------------------------------------------//
  @Override
  public void start (Stage primaryStage) throws Exception
  // ---------------------------------------------------------------------------------//
  {
    this.primaryStage = primaryStage;
    checkParameters ();

    final String os = System.getProperty ("os.name");
    if (os != null && os.startsWith ("Mac"))
      menuBar.setUseSystemMenuBar (true);
    mainPane.setTop (menuBar);

    scene = new Scene (createContent ());
    primaryStage.setScene (scene);

    primaryStage.setOnCloseRequest (e -> exit ());

    restore ();

    primaryStage.show ();
  }

  // ---------------------------------------------------------------------------------//
  protected void restore ()
  // ---------------------------------------------------------------------------------//
  {
    for (SaveState saveState : saveStateList)
      saveState.restore (prefs);

    windowStatus.restore (prefs);

    if (windowStatus.width <= 0 || windowStatus.height <= 22 || windowStatus.x < 0
        || windowStatus.y < 0)
      setWindow ();
    else
      setWindow (windowStatus.width, windowStatus.height, windowStatus.x, windowStatus.y);
  }

  // ---------------------------------------------------------------------------------//
  protected void setWindow ()
  // ---------------------------------------------------------------------------------//
  {
    primaryStage.setWidth (1000);
    primaryStage.setHeight (600);
    primaryStage.centerOnScreen ();
  }

  // ---------------------------------------------------------------------------------//
  protected void setWindow (double width, double height, double x, double y)
  // ---------------------------------------------------------------------------------//
  {
    primaryStage.setWidth (width);
    primaryStage.setHeight (height);
    primaryStage.setX (x);
    primaryStage.setY (y);
  }

  // ---------------------------------------------------------------------------------//
  protected void exit ()
  // ---------------------------------------------------------------------------------//
  {
    windowStatus.setLocation (primaryStage);
    //    ((XmitWindowStatus) windowStatus).setDividers (splitPane);

    windowStatus.save (prefs);

    for (SaveState saveState : saveStateList)
      saveState.save (prefs);

    Platform.exit ();
  }

  // ---------------------------------------------------------------------------------//
  protected void checkParameters ()
  // ---------------------------------------------------------------------------------//
  {
    for (String s : getParameters ().getUnnamed ())
    {
      System.out.printf ("Parameter: %s%n", s);
      if ("-debug".equals (s))
        debug = true;
      else if ("-reset".equals (s))
        try
        {
          prefs.clear ();
          System.out.println ("Preferences reset");
        }
        catch (BackingStoreException e1)
        {
          System.out.println ("Preferences NOT reset");
          e1.printStackTrace ();
        }
      else
        System.out.printf ("Unknown parameter: %s%n", s);
    }
  }
}
