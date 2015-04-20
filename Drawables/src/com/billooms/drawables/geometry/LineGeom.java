package com.billooms.drawables.geometry;

import java.awt.geom.Point2D;

/**
 * A straight line defined by a slope m and y-intercept b.
 *
 * Instances of this class are immutable.
 *
 * @author Bill Ooms. Copyright 2014 Studio of Bill Ooms. All rights reserved.
 */
public class LineGeom {

  /** Slope of the line. */
  private final double m;
  /** y-intercept of the line. */
  private final double b;
  /** Flag for a vertical line. */
  private final boolean vertical;
  /** x-value for a vertical line. */
  private final double x;

  /**
   * Define the line by a point and an angle.
   *
   * @param p point
   * @param ang angle in radians
   */
  public LineGeom(Point2D.Double p, double ang) {
    double a = angleCheck(ang);
    if ((a == Math.PI / 2.0) || (a == 3.0 * Math.PI / 2.0)) {
      this.vertical = true;
      this.x = p.x;
      this.m = Double.MAX_VALUE;
      this.b = Double.NaN;
    } else {
      this.vertical = false;
      this.m = Math.tan(a);
      this.b = p.y - m * p.x;
      this.x = Double.NaN;
    }
  }

  /**
   * Define the line by 2 points.
   *
   * @param p1 1st point
   * @param p2 2nd point
   */
  public LineGeom(Point2D.Double p1, Point2D.Double p2) {	// define the line by 2 points
    if (p1.x == p2.x) {
      this.vertical = true;
      this.x = p1.x;
      this.m = Double.MAX_VALUE;
      this.b = Double.NaN;
    } else {
      this.vertical = false;
      this.m = (p2.y - p1.y) / (p2.x - p1.x);
      this.b = p1.y - m * p1.x;
      this.x = Double.NaN;
    }
  }

  /**
   * Construct a line perpendicular to the given line that goes through the
   * point.
   *
   * @param p point
   * @param line given line
   */
  public LineGeom(Point2D.Double p, LineGeom line) {
    if (line.isVertical()) {	// if given line is vertical
      this.vertical = false;
      this.m = 0;				// then perpendicular is horizontal
      this.b = p.y;
      this.x = Double.NaN;
    } else if (line.m == 0) {			// if given line is horizontal
      this.vertical = true;	// then perpendicular is vertical
      this.x = p.x;
      this.m = Double.MAX_VALUE;
      this.b = Double.NaN;
    } else {
      this.vertical = false;
      this.m = -1.0 / line.m;
      this.b = p.y - m * p.x;
      this.x = Double.NaN;
    }
  }

  /**
   * Check if this line is vertical.
   *
   * @return true=vertical
   */
  public boolean isVertical() {
    return vertical;
  }

  /**
   * Get the slope (m) of the line.
   *
   * @return the slope
   */
  public double getSlope() {
    return m;
  }

  /**
   * Get Y for a given x value.
   *
   * @param x given x value
   * @return y value (will be zero for a vertical line independent of x)
   */
  public double getY(double x) {
    if (vertical) {
      return 0.0;
    }
    return m * x + b;
  }

  /**
   * Get X for a given y value.
   *
   * @param y given y value
   * @return x value (will be zero for a horizontal line independent of y)
   */
  public double getX(double y) {
    if (vertical) {
      return x;
    }
    if (m == 0.0) {
      return 0.0;
    }
    return (y - b) / m;
  }

  /**
   * Find the intersection of this line with the given line.
   *
   * @param line given line
   * @return the point at the intersection (or null if lines are parallel)
   */
  public Point2D.Double intersection(LineGeom line) {
    if (vertical) {
      return new Point2D.Double(x, line.getY(x));			// this line is vertical
    }
    if (line.vertical) {
      return new Point2D.Double(line.x, getY(line.x));		// the given line is vertical
    }
    if ((line.m == m) || (vertical && line.vertical)) {	// no intersection for lines with equal slopes
      return null;
    }
    double xx = (b - line.b) / (line.m - m);
    double yy = m * xx + b;
    return new Point2D.Double(xx, yy);
  }

  /**
   * Make sure angle is in range 0.0 to less than 2PI.
   *
   * @param a angle in radians
   * @return angle in range 0.0 to less than 2PI
   */
  private double angleCheck(double a) {
    while (a < 0.0) {
      a += 2 * Math.PI;
    }
    while (a >= 2 * Math.PI) {
      a -= 2 * Math.PI;
    }
    return a;
  }
}
