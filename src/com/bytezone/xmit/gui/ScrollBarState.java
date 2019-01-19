package com.bytezone.xmit.gui;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;

public class ScrollBarState
{
  private final TextArea textArea;
  private final Orientation orientation;
  private ScrollBar scrollBar;

  private double min;
  private double max;
  private double value;
  private double blockIncrement;
  private double unitIncrement;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public ScrollBarState (TextArea textArea, Orientation orientation)
  {
    this.textArea = textArea;
    this.orientation = orientation;
  }

  // ---------------------------------------------------------------------------------//
  // reset
  // ---------------------------------------------------------------------------------//

  public void reset ()
  {
    scrollBar = null;
  }

  // ---------------------------------------------------------------------------------//
  // save
  // ---------------------------------------------------------------------------------//

  public void save ()
  {
    if (scrollBar == null)
    {
      setScrollBar ();
      if (scrollBar == null)
        return;
    }

    this.min = scrollBar.getMin ();
    this.max = scrollBar.getMax ();
    this.value = scrollBar.getValue ();
    this.blockIncrement = scrollBar.getBlockIncrement ();
    this.unitIncrement = scrollBar.getUnitIncrement ();
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  public void restore ()
  {
    if (scrollBar == null)
      return;

    scrollBar.setMin (min);
    scrollBar.setMax (max);
    scrollBar.setValue (value);
    scrollBar.setUnitIncrement (unitIncrement);
    scrollBar.setBlockIncrement (blockIncrement);
  }

  // ---------------------------------------------------------------------------------//
  // getScrollBar
  // ---------------------------------------------------------------------------------//

  private void setScrollBar ()
  {
    for (Node node : textArea.lookupAll (".scroll-bar"))
      if (node instanceof ScrollBar
          && ((ScrollBar) node).getOrientation ().equals (orientation))
      {
        scrollBar = (ScrollBar) node;
        return;
      }
  }
}
