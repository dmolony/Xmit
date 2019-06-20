package com.bytezone.xmit.gui;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;

// -----------------------------------------------------------------------------------//
class ScrollBarState
// -----------------------------------------------------------------------------------//
{
  private ScrollBar scrollBar;
  private final TextArea textArea;
  private final Orientation orientation;

  private double min;
  private double max;
  private double value;
  private double blockIncrement;
  private double unitIncrement;

  // not used
  // ---------------------------------------------------------------------------------//
  private ScrollBarState (TextArea textArea, Orientation orientation)
  // ---------------------------------------------------------------------------------//
  {
    this.textArea = textArea;
    this.orientation = orientation;
  }

  // ---------------------------------------------------------------------------------//
  private void check ()
  // ---------------------------------------------------------------------------------//
  {
    for (Node node : textArea.lookupAll (".scroll-bar"))
    {
      if (node instanceof ScrollBar
          && ((ScrollBar) node).getOrientation ().equals (orientation))
      {
        scrollBar = (ScrollBar) node;
        break;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  public void save ()
  // ---------------------------------------------------------------------------------//
  {
    if (scrollBar == null)
      check ();

    if (scrollBar != null)
    {
      this.min = scrollBar.getMin ();
      this.max = scrollBar.getMax ();
      this.value = scrollBar.getValue ();
      this.blockIncrement = scrollBar.getBlockIncrement ();
      this.unitIncrement = scrollBar.getUnitIncrement ();
    }
  }

  // ---------------------------------------------------------------------------------//
  public void restore ()
  // ---------------------------------------------------------------------------------//
  {
    if (scrollBar != null)
    {
      scrollBar.setMin (min);
      scrollBar.setMax (max);
      scrollBar.setValue (value);
      scrollBar.setUnitIncrement (unitIncrement);
      scrollBar.setBlockIncrement (blockIncrement);
    }
  }
}
