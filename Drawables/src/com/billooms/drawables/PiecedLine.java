package com.billooms.drawables;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

/**
 * A pieced line defined by a number of Points.
 *
 * Line segments are straight lines between the points. The color of the points
 * is defined by each of the points.
 *
 * @author Bill Ooms. Copyright 2014 Studio of Bill Ooms. All rights reserved.
 */
public class PiecedLine extends PtDefinedLine {

  /**
   * A pieced line defined by a number of Points.
   *
   * @param c Color line color
   * @param s Stroke line stroke
   */
  public PiecedLine(Color c, BasicStroke s) {
    super(c, s);
  }

  /**
   * Paint the object.
   *
   * @param g2d Graphics2D
   */
  @Override
  public void paint(Graphics2D g2d) {
    super.paint(g2d);	  // paints Points, set line color and stroke
    if (isVisible()) {
      if (ptList.size() == 1) {	// no line for only one point
        return;
      }
      Pt p, pm1;
      for (int i = 1; i < ptList.size(); i++) {
        pm1 = ptList.get(i - 1);
        p = ptList.get(i);
        g2d.draw(new Line2D.Double(pm1.getX(), pm1.getY(), p.getX(), p.getY()));
      }
    }
  }

  @Override
  protected void update() {
    // do nothing
  }

  /**
   * Get the x-value for a given y-value by interpolation.
   *
   * Note: this assumes the points are in sorted-by-Y order.
   *
   * @param y y-value
   * @return x-value (or zero if there are no points).
   */
  @Override
  public double getXforY(double y) {
    if (ptList.isEmpty()) {
      return 0.0;
    }
    if (y <= ptList.get(0).getY()) {		// less than first point
      return ptList.get(0).getX();
    }
    if (y >= getLastPt().getY()) {		// more than last point
      return getLastPt().getX();
    }
    double xmin, xmax, ymin, ymax;
    for (int i = 1; i < ptList.size(); i++) {   // point is somewhere between
      xmin = ptList.get(i - 1).getX();
      xmax = ptList.get(i).getX();
      ymin = ptList.get(i - 1).getY();
      ymax = ptList.get(i).getY();
      if ((y >= ymin) && (y < ymax)) {
        return (y - ymin) / (ymax - ymin) * (xmax - xmin) + xmin;
      }
    }
    return 0.0;		// should never get here
  }

  /**
   * Get the y-value for a given x-value by interpolation.
   *
   * Note: this assumes the points are in sorted-by-X order.
   *
   * @param x x-value
   * @return y-value (or zero if there are no points).
   */
  @Override
  public double getYforX(double x) {
    if (ptList.isEmpty()) {
      return 0.0;
    }
    if (x <= ptList.get(0).getX()) {		// less than first point
      return ptList.get(0).getY();
    }
    if (x >= getLastPt().getX()) {		// more than last point
      return getLastPt().getY();
    }
    double xmin, xmax, ymin, ymax;
    for (int i = 1; i < ptList.size(); i++) {    // point is somewhere between
      xmin = ptList.get(i - 1).getX();
      xmax = ptList.get(i).getX();
      ymin = ptList.get(i - 1).getY();
      ymax = ptList.get(i).getY();
      if ((x >= xmin) && (x < xmax)) {
        return (x - xmin) / (xmax - xmin) * (ymax - ymin) + ymin;
      }
    }
    return 0.0;		// should never get here
  }

}
