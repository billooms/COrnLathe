package com.billooms.drawables;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 * A pieced line defined by a number of points and TrigCurve segments.
 *
 * Every 3 points is a quarter sine/cosine arc, even numbered points (starting
 * with 0) are shared, and odd numbered points determine the curvature of each
 * segment. If there is an extra point, it is a simple straight line.
 *
 * @author Bill Ooms. Copyright 2015 Studio of Bill Ooms. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class TrigSegLine extends PtDefinedLine {

  private final ArrayList<TrigCurve> segments = new ArrayList<>();

  /**
   * A line made of arc segments defined by a number of Points.
   *
   * @param c Color line color
   * @param s Stroke line stroke
   */
  public TrigSegLine(Color c, BasicStroke s) {
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
      TrigCurve tc = new TrigCurve(getColor(), getStroke());
      tc.addPt(ptList.get(idx));
      tc.addPt(ptList.get(idx + 1));
      tc.addPt(ptList.get(idx + 2));
      segments.add(tc);
    }
    if (((ptList.size() + 1) % 2) == 1) {   // last segment is only 2 points (which will be straight)
      TrigCurve tc = new TrigCurve(getColor(), getStroke());
      tc.addPt(ptList.get(ptList.size() - 2));
      tc.addPt(ptList.get(ptList.size() - 1));
      segments.add(tc);
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
    if (y <= ptList.get(0).getY()) {	// less than first point
      return ptList.get(0).getX();
    }
    if (y >= getLastPt().getY()) {	// more than last point
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
    if (x <= ptList.get(0).getX()) {	// less than first point
      return ptList.get(0).getY();
    }
    if (x >= getLastPt().getX()) {	// more than last point
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
