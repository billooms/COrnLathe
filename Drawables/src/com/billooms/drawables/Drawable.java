package com.billooms.drawables;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Abstract interface defining things that are drawable.
 *
 * All Drawable objects have color and visible flag and may utilize a stroke.
 *
 * @author Bill Ooms. Copyright 2014 Studio of Bill Ooms. All rights reserved.
 */
public interface Drawable {

  /** Solid line stroke. */
  public final static BasicStroke SOLID_LINE = new BasicStroke(1.0f);
  /** Light dotted stroke. */
  public final static BasicStroke LIGHT_DOT = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, new float[]{1, 5}, 0);

  /**
   * Get the color of the object.
   *
   * @return color
   */
  Color getColor();

  /**
   * Set the color of the object.
   *
   * @param c new color
   */
  void setColor(Color c);

  /**
   * Determine if the object is currently visible.
   *
   * @return true=visible; false=invisible
   */
  boolean isVisible();

  /**
   * Set the visibility of the object.
   *
   * @param v true=visible; false=not drawn
   */
  void setVisible(boolean v);

  /**
   * Get the stroke for this shape.
   *
   * @return stroke
   */
  default BasicStroke getStroke() {
    return SOLID_LINE;
  }

  /**
   * Set the stroke for the shape.
   *
   * @param s new stroke
   */
  default void setStroke(BasicStroke s) {
    // do nothing -- user must over-ride
  }

  /**
   * Paint the object.
   *
   * @param g2d Graphics2D g
   */
  void paint(Graphics2D g2d);
}
