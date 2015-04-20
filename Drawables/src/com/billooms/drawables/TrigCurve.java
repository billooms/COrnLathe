package com.billooms.drawables;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

/**
 * A quarter of a sine/cosine curve defined by 3 points.
 *
 * The first and last points define the start/end of the curve, and the second
 * point defines the curvature (sine or cosine section). Note that the second
 * point need not be on the curve. More than 3 points will be ignored.
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
public class TrigCurve extends PtDefinedLine {

  private final static int DRAW_PTS = 10;   // draw 10 points (only when painting)
  private final static double HALFPI = Math.PI / 2.0;

  /** Possible curvatures, plus straight line option (2 points) */
  private static enum SegStyle {

    /** Positive sine */
    SINE,
    /** Negative sine */
    NSINE,
    /** Positive cosine */
    COS,
    /** Negative cosine */
    NCOS,
    /** Straight line (2 points) */
    STRAIGHT,
    /** Single point */
    SINGLE,
    /** None */
    NONE
  }

  /**
   * A quarter sine/cosine curve.
   *
   * @param c Color line color
   * @param s Stroke line stroke
   */
  public TrigCurve(Color c, BasicStroke s) {
    super(c, s);
  }

  @Override
  public void paint(Graphics2D g2d) {
    super.paint(g2d);	  // paints the points
    if (isVisible()) {
      if (ptList.size() <= 1) {		// no line for only one point
        return;
      }

      GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, DRAW_PTS);
      polyline.moveTo(getPt(0).getX(), getPt(0).getY());
      double dx = getLastPt().getX() - getPt(0).getX();
      if (dx == 0.0) {
        return;	  // nothing to draw
      }
      for (int i = 1; i < DRAW_PTS; i++) {
        double x = getPt(0).getX() + dx * i / (DRAW_PTS - 1);
        polyline.lineTo(x, getYforX(x));
      }
      g2d.draw(polyline);
    }
  }

  @Override
  protected void update() {
    // do nothing
  }

  @Override
  public double getYforX(double x) {
    double y;
    Pt pt0, pt1, pt2;
    switch (getSegStyle()) {
      default:
      case NONE:
        y = 0.0;
        break;
      case SINGLE:
        y = getPt(0).getY();
        break;
      case STRAIGHT:
        pt0 = getPt(0);
        pt1 = getPt(1);
        y = pt0.getY() + (pt1.getY() - pt0.getY()) * (x - pt0.getX()) / (pt1.getX() - pt0.getX());
        break;
      case SINE:
        pt0 = getPt(0);
        pt2 = getPt(2);
        y = pt0.getY() + (pt2.getY() - pt0.getY()) * Math.sin(HALFPI * (x - pt0.getX()) / (pt2.getX() - pt0.getX()));
        break;
      case COS:
        pt0 = getPt(0);
        pt2 = getPt(2);
        y = pt2.getY() + (pt0.getY() - pt2.getY()) * Math.cos(HALFPI * (x - pt0.getX()) / (pt2.getX() - pt0.getX()));
        break;
      case NSINE:
        pt0 = getPt(0);
        pt2 = getPt(2);
        y = pt0.getY() - (pt0.getY() - pt2.getY()) * Math.sin(HALFPI * (x - pt0.getX()) / (pt2.getX() - pt0.getX()));
        break;
      case NCOS:
        pt0 = getPt(0);
        pt2 = getPt(2);
        y = pt2.getY() - (pt2.getY() - pt0.getY()) * Math.cos(HALFPI * (x - pt0.getX()) / (pt2.getX() - pt0.getX()));
        break;
    }
    return y;
  }

  /**
   * Get the style based on the slope between the points.
   */
  private SegStyle getSegStyle() {
    if (getSize() == 0) {
      return SegStyle.NONE;
    } else if (getSize() == 1) {
      return SegStyle.SINGLE;
    } else if (getSize() == 2) {
      return SegStyle.STRAIGHT;
    } else {
      if (slope(0, 2) >= 0) {		// positive overall slope
        if (slope(0, 1) > slope(1, 2)) {
          return SegStyle.SINE;
        } else {
          return SegStyle.NCOS;
        }
      } else {
        if (slope(0, 1) > slope(1, 2)) {
          return SegStyle.COS;
        } else {
          return SegStyle.NSINE;
        }
      }
    }
  }

  /**
   * Get the slope between points a and b.
   *
   * Note: no checks are made to see if there are 3 points
   *
   * @return slope
   */
  private double slope(int a, int b) {
    Pt pta = getPt(a);
    Pt ptb = getPt(b);
    return (ptb.getY() - pta.getY()) / (ptb.getX() - pta.getX());
  }

  @Override
  public double getXforY(double y) {
    throw new UnsupportedOperationException("TrigCurve.getXforY Not supported yet.");
  }

}
