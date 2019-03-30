package com.bytezone.xmit.gui;

import java.util.List;
import java.util.regex.Pattern;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Disposition;
import com.bytezone.xmit.Utility;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

// ---------------------------------------------------------------------------------//
public class JclManager implements TableItemSelectionListener
//---------------------------------------------------------------------------------//
{
  private static Pattern includePattern =
      Pattern.compile ("^//\\s+JCLLIB\\s+ORDER=\\((" + Utility.validName + ")\\)$");
  private static Pattern memberPattern =
      Pattern.compile ("INCLUDE\\s+MEMBER=(" + Utility.validPart + ")");
  private static Pattern dsnPattern = Pattern
      .compile ("DSN=(" + Utility.validName + ")\\((" + Utility.validPart + ")\\)");

  private Stage stage;
  private final TextFlow textFlow = new TextFlow ();
  //  Text text1 = new Text ("Hello ");
  //  Text text2 = new Text ("Bold");
  //  Text text3 = new Text (" World");
  private CatalogEntry catalogEntry;
  private final Font font = Font.font ("Monaco", FontWeight.NORMAL, 12);
  private final Color baseColor = Color.GREEN;

  //---------------------------------------------------------------------------------//
  void showWindow ()
  //---------------------------------------------------------------------------------//
  {
    if (stage == null)
    {
      stage = new Stage ();
      stage.setTitle ("JCL Viewer");

      BorderPane borderPane = new BorderPane ();

      Button btnOk = getButton ("OK");
      Button btnCancel = getButton ("Cancel");
      btnOk.setOnAction (e -> apply ());
      btnCancel.setOnAction (e -> cancel ());

      HBox controlBox = new HBox (10);
      controlBox.setPrefHeight (20);
      controlBox.setPadding (new Insets (6, 10, 6, 10));
      controlBox.setAlignment (Pos.CENTER_LEFT);
      Region filler = new Region ();
      HBox.setHgrow (filler, Priority.ALWAYS);
      controlBox.getChildren ().addAll (filler, btnCancel, btnOk);

      textFlow.setLineSpacing (1);
      textFlow.setBorder (null);
      ScrollPane sp = new ScrollPane (textFlow);
      sp.setPadding (new Insets (10));

      borderPane.setBottom (controlBox);
      borderPane.setCenter (sp);

      stage.setScene (new Scene (borderPane, 600, 800));

      btnOk.setDefaultButton (true);
      btnCancel.setCancelButton (true);
    }

    setText ();
    stage.show ();
    stage.setAlwaysOnTop (true);
  }

  // ---------------------------------------------------------------------------------//
  private void setText ()
  // ---------------------------------------------------------------------------------//
  {
    textFlow.getChildren ().clear ();

    if (catalogEntry == null)
      return;

    Disposition disposition = catalogEntry.getMember ().getDisposition ();
    if (disposition.getLrecl () != 80 || !disposition.getRecfm ().equals ("FB"))
      return;

    List<String> lines = catalogEntry.getMember ().getLines ();
    if (!isJCL (lines))
      return;

    for (String line : lines)
    {
      if (line.length () > 72)
        line = line.substring (0, 72);            // remove line numbers

      if (line.startsWith ("//*"))                // line comment
        addTextNewLine (line, Color.BLUE);
      else
      {
        int pos = line.indexOf ("DSN=");          // found a dataset
        if (pos > 0)
        {
          pos += 4;
          addText (line.substring (0, pos), baseColor);

          int pos2 = line.indexOf (',', pos);
          if (pos2 < 0)
            pos2 = line.indexOf (' ', pos);

          if (pos2 > 0)
          {
            addText (line.substring (pos, pos2), Color.RED);
            addTextNewLine (line.substring (pos2), baseColor);
          }
          else
            addTextNewLine (line.substring (pos), Color.RED);
        }
        else                                      // no dataset
          addTextNewLine (line, baseColor);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private void addTextNewLine (String line, Color color)
  // ---------------------------------------------------------------------------------//
  {
    addText (line + "\n", color);
  }

  // ---------------------------------------------------------------------------------//
  private void addText (String line, Color color)
  // ---------------------------------------------------------------------------------//
  {
    Text text = new Text (line);
    text.setFill (color);
    text.setFont (font);
    textFlow.getChildren ().add (text);
  }

  // ---------------------------------------------------------------------------------//
  private void apply ()
  // ---------------------------------------------------------------------------------//
  {
    stage.hide ();
  }

  // ---------------------------------------------------------------------------------//
  private void cancel ()
  // ---------------------------------------------------------------------------------//
  {
    stage.hide ();
  }

  // ---------------------------------------------------------------------------------//
  private Button getButton (String text)
  // ---------------------------------------------------------------------------------//
  {
    Button button = new Button (text);
    button.setMinWidth (100);
    return button;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void tableItemSelected (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    this.catalogEntry = catalogEntry;
    if (stage != null && stage.isShowing ())
      setText ();
  }

  //----------------------------------------------------------------------------------- //
  private boolean isJCL (List<String> lines)
  //----------------------------------------------------------------------------------- //
  {
    return Utility.jobCardPattern.matcher (getFirstNonComment (lines)).find ();
  }

  //----------------------------------------------------------------------------------- //
  private String getFirstNonComment (List<String> lines)
  //----------------------------------------------------------------------------------- //
  {
    for (String line : lines)
      if (!line.startsWith ("//*"))
        return line;
    return "";
  }
}
