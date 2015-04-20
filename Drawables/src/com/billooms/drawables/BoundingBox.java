package com.billooms.drawables;

import java.awt.geom.Point2D;

/**
 * Bounding box of something.
 *
 * Instances of this object are immutable.
 *
 * This isn't really a Drawable, but this is a convenient place to put it as
 * it's usually used in conjunction with Drawables.
 *
 * @author Bill Ooms Copyright (c) 2014 Studio of Bill Ooms, all rights reserved
 */
public class BoundingBox {

  /** Lower left corner. */
  public final Point2D.Double min;
  /** Upper right corner. */
  public final Point2D.Double max;

  /**
   * Construct a bounding box with values of zero.
   */
  public BoundingBox() {
    min = new Point2D.Double();
    max = new Point2D.Double();
  }

  /**
   * Create a bounding box with the given dimensions.
   *
   * @param xmin minimum x
   * @param ymin minimum y
   * @param xmax maximum x
   * @param ymax maximum y
   */
  public BoundingBox(double xmin, double ymin, double xmax, double ymax) {
    min = new Point2D.Double(xmin, ymin);
    max = new Point2D.Double(xmax, ymax);
  }

  /**
   * Create a bounding box which is encompasses both the given bounding boxes.
   *
   * @param b1 BoundingBox
   * @param b2 BoundingBox
   */
  public BoundingBox(BoundingBox b1, BoundingBox b2) {
    min = new Point2D.Double(Math.min(b1.min.x, b2.min.x), Math.min(b1.min.y, b2.min.y));
    max = new Point2D.Double(Math.max(b1.max.x, b2.max.x), Math.max(b1.max.y, b2.max.y));
  }

  /**
   * Get the width of the bounding box.
   *
   * @return width
   */
  public double getWidth() {
    return Math.abs(max.x - min.x);
  }

  /**
   * Get the height of the bounding box.
   *
   * @return height
   */
  public double getHeight() {
    return Math.abs(max.y - min.y);
  }

  /**
   * String representation of this object.
   *
   * @return string representation
   */
  @Override
  public String toString() {
    return "min:" + min.toString() + " max:" + max.toString();
  }
}
