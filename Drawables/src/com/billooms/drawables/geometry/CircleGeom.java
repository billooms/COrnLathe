package com.billooms.drawables.geometry;

import java.awt.geom.Point2D;

/**
 * A circle defined by a center point and a radius.
 *
 * @author Bill Ooms. Copyright 2011 Studio of Bill Ooms. All rights reserved.
 */
public class CircleGeom {

  /** Center point. */
  public Point2D.Double p;
  /** Radius. */
  public double r;

  /**
   * Define a circle by a center point and a radius.
   *
   * @param p Point2D.Double center
   * @param r radius
   */
  public CircleGeom(Point2D.Double p, double r) {
    this.p = p;
    this.r = r;
  }

  /**
   * Determine if the given point is inside the circle.
   *
   * @param pt given point
   * @return true = inside (not on the circle or outside)
   */
  public boolean isInside(Point2D.Double pt) {
    return Math.hypot(p.x - pt.x, p.y - pt.y) < r;
  }
}
