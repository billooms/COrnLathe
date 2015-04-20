package com.billooms.drawables.simple;

import com.billooms.drawables.Drawable;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * Abstract drawable for use by other various objects.
 *
 * @author Bill Ooms Copyright 2014 Studio of Bill Ooms. All rights reserved.
 */
public abstract class AbstractDrawable implements Drawable {

  private final static Color DEFAULT_COLOR = Color.RED;	  // You'll want to over-ride this
  private final static int DEFAULT_PTSIZE = 6;

  /** The color of the drawn point. */
  protected Color color = DEFAULT_COLOR;
  /** Flag indicating if the grid is visible or not. */
  protected boolean visible = true;
  /** Flag indicating fill the shape. */
  protected boolean fill = false;
  /** Stroke for the shape. */
  protected BasicStroke stroke = SOLID_LINE;
  /** Point size. */
  protected int ptSize = DEFAULT_PTSIZE;
  /** x-coordinate. */
  protected double x = 0.0;
  /** y-coordinate. */
  protected double y = 0.0;

  /** Construct a new Drawable with defaults. */
  public AbstractDrawable() {
    // use defaults
  }

  /**
   * Construct a new Drawable.
   *
   * @param x x-coordinate
   * @param y y-coordinate
   * @param c color
   * @param fill fill flag
   */
  public AbstractDrawable(double x, double y, Color c, boolean fill) {
    this.x = x;
    this.y = y;
    this.color = c;
    this.fill = fill;
  }

  /**
   * Construct a new Drawable.
   *
   * @param pos position
   * @param c color
   * @param fill fill flag
   */
  public AbstractDrawable(Point2D.Double pos, Color c, boolean fill) {
    this(pos.x, pos.y, c, fill);
  }

  /**
   * Construct a new Drawable.
   *
   * @param pos position
   * @param c color
   * @param s stroke
   */
  public AbstractDrawable(Point2D.Double pos, Color c, BasicStroke s) {
    this.x = pos.x;
    this.y = pos.y;
    this.color = c;
    this.stroke = s;
  }

  /**
   * Get the color of the object.
   *
   * @return color
   */
  @Override
  public Color getColor() {
    return color;
  }

  /**
   * Set the color of the object.
   *
   * @param c new color
   */
  @Override
  public void setColor(Color c) {
    this.color = c;
  }

  /**
   * Determine if the object is currently visible.
   *
   * @return true=visible; false=invisible
   */
  @Override
  public boolean isVisible() {
    return visible;
  }

  /**
   * Set the visibility of the object.
   *
   * @param v true=visible; false=not drawn
   */
  @Override
  public void setVisible(boolean v) {
    this.visible = v;
  }

  /**
   * Is the shape filled?
   *
   * @return true: filled, false: hollow
   */
  public boolean isFilled() {
    return fill;
  }

  /**
   * Set the fill flag.
   *
   * @param fill true: filled, false: hollow
   */
  public void setFill(boolean fill) {
    this.fill = fill;
  }

  /**
   * Get the overall size of a point in pixels.
   *
   * @return size in pixels
   */
  public int getPtSize() {
    return ptSize;
  }

  /**
   * Set the overall size of a point in pixels.
   *
   * @param newSize size in pixels
   */
  public void setPtSize(int newSize) {
    this.ptSize = newSize;
  }

  /**
   * Get the x coordinate.
   *
   * @return x coordinate
   */
  public double getX() {
    return x;
  }

  /**
   * Set the x coordinate.
   *
   * @param x x coordinate
   */
  public void setX(double x) {
    this.x = x;
  }

  /**
   * Get the y coordinate.
   *
   * @return y coordinate
   */
  public double getY() {
    return y;
  }

  /**
   * Set the y coordinate.
   *
   * @param y y coordinate
   */
  public void setY(double y) {
    this.y = y;
  }

  /**
   * Set the position.
   *
   * @param x x coordinate
   * @param y y coordinate
   */
  public void setXY(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Get the stroke for this shape.
   *
   * @return stroke
   */
  @Override
  public BasicStroke getStroke() {
    return stroke;
  }

  /**
   * Set the stroke for the shape.
   *
   * @param s new stroke
   */
  @Override
  public void setStroke(BasicStroke s) {
    this.stroke = s;
  }

  /**
   * Get the X size of the point in inches.
   *
   * @param g2d Graphics2D
   * @return size of the point in inches
   */
  public double getXPixSizeInch(Graphics2D g2d) {
    return (double) ptSize / g2d.getTransform().getScaleX();
  }

  /**
   * Get the Y size of the point in inches.
   *
   * @param g2d Graphics2D
   * @return size of the point in inches
   */
  public double getYPixSizeInch(Graphics2D g2d) {
    return (double) ptSize / g2d.getTransform().getScaleY();
  }
}
