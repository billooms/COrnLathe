package com.billooms.drawables;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 * A pieced line defined by a number of points and arc segments.
 *
 * Every 3 points is a fitted curve arc, even numbered points (starting with 0)
 * are shared, and odd numbered points determine the curvature of each arc
 * segment. If there is an extra point, it is a simple straight line.
 *
 * @author Bill Ooms. Copyright 2014 Studio of Bill Ooms. All rights reserved.
 */
public class ArcSegLine extends PtDefinedLine /* implements
 * PropertyChangeListener */ {

  private final ArrayList<PtDefinedLine> segments = new ArrayList<>();

  /**
   * A line made of arc segments defined by a number of Points.
   *
   * @param c Color line color
   * @param s Stroke line stroke
   */
  public ArcSegLine(Color c, BasicStroke s) {
    super(c, s);
  }

  /**
   * Paint the object.
   *
   * @param g2d Graphics2D
   */
  @Override
  public void paint(Graphics2D g2d) {
    super.paint(g2d);
    if (isVisible()) {
      if (ptList.size() == 1) {
        return;
      }
      if (segments.isEmpty()) {
        return;
      }
      segments.stream().forEach((seg) -> {
        seg.paint(g2d);
      });
    }
  }

  /**
   * Rebuild the list of segments.
   */
  @Override
  protected void update() {
    segments.clear();
    if (ptList.size() <= 1) {
      return;
    }
    int nArcs = (ptList.size() - 1) / 2;
    for (int i = 0, idx = 0; i < nArcs; i++, idx = idx + 2) {
      FittedCurve arc = new FittedCurve(getColor(), getStroke());
      arc.addPt(ptList.get(idx));
      arc.addPt(ptList.get(idx + 1));
      arc.addPt(ptList.get(idx + 2));
      arc.setPtSpacing(0.02);	  // this is arbitrary to assure the line is drawn
      segments.add(arc);
    }
    if (((ptList.size() + 1) % 2) == 1) {
      PiecedLine line = new PiecedLine(getColor(), getStroke());
      line.addPt(ptList.get(ptList.size() - 2));
      line.addPt(ptList.get(ptList.size() - 1));
      segments.add(line);
    }
  }

  /**
   * Get the x-value for a given y-value by interpolation. Note: this assumes
   * the points are in sorted-by-Y order.
   *
   * @param y y-value
   * @return x-value (or zero if there are no points).
   */
  @Override
  public double getXforY(double y) {
    if (ptList.isEmpty()) {
      return 0.0;
    }
    if (y <= ptList.get(0).getY()) {			// less than first point
      return ptList.get(0).getX();
    }
    if (y >= getLastPt().getY()) {			// more than last point
      return getLastPt().getX();
    }
    for (PtDefinedLine seg : segments) {
      if ((y >= seg.getPt(0).getY()) && (y <= seg.getLastPt().getY())) {
        return seg.getXforY(y);
      }
    }
    return 0.0;		// should never get here
  }

  /**
   * Get the y-value for a given x-value by interpolation. Note: this assumes
   * the points are in sorted-by-X order.
   *
   * @param x x-value
   * @return y-value (or zero if there are no points).
   */
  @Override
  public double getYforX(double x) {
    if (ptList.isEmpty()) {
      return 0.0;
    }
    if (x <= ptList.get(0).getX()) {			// less than first point
      return ptList.get(0).getY();
    }
    if (x >= getLastPt().getX()) {			// more than last point
      return getLastPt().getY();
    }
    for (PtDefinedLine seg : segments) {
      if ((x >= seg.getPt(0).getX()) && (x <= seg.getLastPt().getX())) {
        return seg.getYforX(x);
      }
    }
    return 0.0;		// should never get here
  }

}
