package com.billooms.drawables.simple;

import com.billooms.drawables.BoundingBox;
import com.billooms.drawables.vecmath.Vector2d;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * A curve defined by an array of points.
 *
 * Points are ordered, bottom first and top last.
 *
 * In addition to color, visible flag, and stroke, a Curve has an array of 2D
 * points.
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
public class Curve extends AbstractDrawable {

  /** A bit more than 90 degrees (in radians). */
  private final static double FILTER_ANGLE = 1.01 * (Math.PI / 2.0);

  /** Array of points defining the curve. */
  private Point2D.Double[] points;

  /**
   * A drawable curve defined by an array of points.
   *
   * @param pts array of Point2D.Double
   * @param c Color
   * @param s Stroke
   */
  public Curve(Point2D.Double[] pts, Color c, BasicStroke s) {
    this.points = pts;
    this.color = c;
    this.stroke = s;
  }
  
  /**
   * Construct a new curve from the give curve.
   * 
 * @param curve given curve
   */
  public Curve(Curve curve) {
    this.points = curve.getPoints();
    this.color = curve.color;
    this.stroke = curve.stroke;
  }

  /**
   * A drawable curve with an empty array of points.
   *
   * @param c Color
   * @param s Stroke
   */
  public Curve(Color c, BasicStroke s) {
    this(new Point2D.Double[0], c, s);
  }

  /**
   * Paint the object.
   *
   * @param g2d Graphics2D
   */
  @Override
  public void paint(Graphics2D g2d) {
    if (points.length < 2) {
      return;
    }

    if (visible) {
      g2d.setColor(getColor());
      float scale = (float) g2d.getTransform().getScaleX();
      if (scale < 0.0) {
        return;		// not sure why this happens sometimes
      }
      float array[] = getStroke().getDashArray();
      if ((array == null) || (array.length == 0)) {
        g2d.setStroke(new BasicStroke(1.0f / scale));
      } else {
        for (int i = 0; i < array.length; i++) {
          array[i] = array[i] / scale;
        }
        g2d.setStroke(new BasicStroke(1.0f / scale, getStroke().getEndCap(), getStroke().getLineJoin(), 1.0f, array, getStroke().getDashPhase() / scale));
      }
      GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.length);
      polyline.moveTo(points[0].x, points[0].y);
      for (Point2D.Double pt : points) {
        polyline.lineTo(pt.x, pt.y);
      }
      g2d.draw(polyline);
    }
  }

  /**
   * Clear the points associated with this curve.
   */
  public void clear() {
    this.points = new Point2D.Double[0];
  }

  /**
   * Get the array of Point2D.Double that defines the curve.
   *
   * @return array of Point2D.Double
   */
  public Point2D.Double[] getPoints() {
    return points;
  }

  /**
   * Set the array of points defining the curve.
   *
   * @param pts array of Point2D.Double that defines the curve
   */
  public void setPoints(Point2D.Double[] pts) {
    this.points = pts;
  }

  /**
   * Get the number of points in the curve.
   *
   * @return the number of points
   */
  public int getSize() {
    return points.length;
  }

  /**
   * Get the top point of the curve (the last point).
   *
   * @return top point of the curve (last point).
   */
  public Point2D.Double getTopPoint() {
    return points[points.length - 1];
  }

  /**
   * Get the bottom point of the curve (the first point).
   *
   * @return bottom point of the curve (first point).
   */
  public Point2D.Double getBottomPoint() {
    return points[0];
  }

  /**
   * Get the bounding box for the points defining this shape.
   *
   * @return bounding box (which might not include 0.0, 0.0)
   */
  public BoundingBox getBoundingBox() {
    if (points.length == 0) {
      return new BoundingBox(0.0, 0.0, 0.0, 0.0);
    }
    Point2D.Double p0 = points[0];
    double minX = p0.x, maxX = p0.x, minY = p0.y, maxY = p0.y;
    for (Point2D.Double p : points) {
      if (p.y < minY) {
        minY = p.y;
      }
      if (p.y > maxY) {
        maxY = p.y;
      }
      if (p.x > maxX) {
        maxX = p.x;
      }
      if (p.x < minX) {
        minX = p.x;
      }
    }
    return new BoundingBox(minX, minY, maxX, maxY);
  }

  /**
   * Get the total length of the curve.
   *
   * @return total length
   */
  public double getLength() {
    if (points.length < 2) {
      return 0.0;
    }
    double length = 0.0;
    for (int i = 0; i < points.length - 1; i++) {
      length += points[i].distance(points[i + 1]);
    }
    return length;
  }

  /**
   * Make a subset of points on the curve.
   *
   * Include all points between p0 and p1 (inclusive). If points are off the
   * curve, use the nearest point.
   *
   * @param p0 first point
   * @param p1 second point
   * @return array of points between p0 and p1 (inclusive) (null if no points on
   * curve)
   */
  public Point2D.Double[] subsetPoints(Point2D.Double p0, Point2D.Double p1) {
    int i0 = indexOfNearestPoint(p0);
    int i1 = indexOfNearestPoint(p1);
    if ((i0 == -1) || (i1 == -1)) {
      return null;
    }
    Point2D.Double[] pts = new Point2D.Double[Math.abs(i1 - i0) + 1];
    int idx = 0;
    if (i0 < i1) {		// points are either bottom->up or top->down (depends on order of p0,p1)
      for (int i = i0; i <= i1; i++) {
        pts[idx] = points[i];
        idx++;
      }
    } else {
      for (int i = i0; i >= i1; i--) {
        pts[idx] = points[i];
        idx++;
      }
    }
    return pts;
  }

  /**
   * Find the nearest point on the curve to the given point.
   *
   * @param pt Given point
   * @return copy of the nearest point on the curve (null if no points)
   */
  public Point2D.Double nearestPoint(Point2D.Double pt) {
    if (points.length == 0) {
      return null;
    }
    if (points.length == 1) {
      return new Point2D.Double(points[0].x, points[0].y);
    }
    Point2D.Double closePt = points[0];
    double dist = closePt.distance(pt);
    for (Point2D.Double p : points) {
      if (p.distance(pt) < dist) {
        dist = p.distance(pt);
        closePt = p;
      }
    }
    return new Point2D.Double(closePt.x, closePt.y);	// return a copy, not the original
  }

  /**
   * Find the index of the nearest point on the curve to the given point.
   *
   * @param pt Given point
   * @return the index of the nearest point on the curve (-1 if no points)
   */
  public int indexOfNearestPoint(Point2D.Double pt) {
    if (points.length == 0) {
      return -1;
    }
    if (points.length == 1) {
      return 0;
    }
    int idx = 0;
    double dist = points[idx].distance(pt);
    for (int i = 1; i < points.length; i++) {
      if (points[i].distance(pt) < dist) {
        dist = points[i].distance(pt);
        idx = i;
      }
    }
    return idx;
  }

  /**
   * Find a point that is a given distance down (toward the beginning) from the
   * given point.
   *
   * A negative distance will find the distance up (toward the end). The given
   * point is assumed to be on the curve. The returned point will be
   * interpolated on a straight line between two points that are on the curve.
   * If d==0.0, then the given point is returned without any interpolation.
   *
   * @param pt Given point
   * @param d distance down (negative means up)
   * @return interpolated point (or null if off the curve)
   */
  public Point2D.Double interpolateDown(Point2D.Double pt, double d) {
    if (d == 0.0) {
      return pt;
    }
    if (points.length == 0) {
      return null;	// no points
    }
    if (points.length == 1) {
      return null;	// can't interpolate with only one point
    }
    int start = indexOfNearestPoint(pt);
    double length = 0.0, last;
    if (d >= 0) {		// go up
      if (start == (points.length - 1)) {
        return null;	// falls off the top
      }
      for (int i = start + 1; i < points.length; i++) {
        last = length;
        length += Math.hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y);
        if (length >= d) {
          return new Point2D.Double(points[i - 1].x + (points[i].x - points[i - 1].x) * (d - last) / (length - last),
              points[i - 1].y + (points[i].y - points[i - 1].y) * (d - last) / (length - last));
        }
      }
      return null;	// fell off the top
    } else {			// go down
      if (start == 0) {
        return null;	// falls off the bottom
      }
      for (int i = start - 1; i >= 0; i--) {
        last = length;
        length += Math.hypot(points[i + 1].x - points[i].x, points[i + 1].y - points[i].y);
        if (length >= Math.abs(d)) {
          return new Point2D.Double(points[i + 1].x - (points[i + 1].x - points[i].x) * (Math.abs(d) - last) / (length - last),
              points[i + 1].y - (points[i + 1].y - points[i].y) * (Math.abs(d) - last) / (length - last));
        }
      }
      return null;	// fell off the bottom
    }
  }

  /**
   * Flip the curve by changing sign of x-coordinates of all points.
   */
  public void flipX() {
    for (Point2D.Double point : points) {
      point.x = -point.x;   // change sign of x
    }
  }

  /**
   * Move the curve by moving all points parallel with the tangent at each
   * point.
   *
   * @param d offset amount
   */
  public void offsetPts(double d) {
    if (d == 0.0) {
      return;
    }
    setPoints(ptsOffset(d));
  }

  /**
   * Return an array of points which are offset in a perpendicular direction
   * from this curve by the specified amount.
   *
   * The direction of the offset is to the right of the direction of the line.
   *
   * @param d offset amount
   * @return new array of Point2D.Double representing the points on a new curve
   */
  public Point2D.Double[] ptsOffset(double d) {
    if (points.length <= 1) {	// no offset for one point
      return points;
    }

    Point2D.Double[] newPts = new Point2D.Double[points.length];
    for (int i = 0; i < points.length; i++) {
      int im1 = Math.max(i - 1, 0);
      int ip1 = Math.min(i + 1, points.length - 1);
      if (points[im1].x == points[ip1].x) {		// check for vertical
        newPts[i] = new Point2D.Double(points[i].x + d, points[i].y);	// if vertical, move x-direction only
//    } else if (points[im1].y == points[ip1].y) {	// check for horizontal
//	newPts[i] = new Point2D.Double(points[i].x, points[i].y - d);	// if horizontal, move y-direction only
      } else {
        double theta = Math.atan2(points[ip1].y - points[im1].y, points[ip1].x - points[im1].x);
        newPts[i] = new Point2D.Double(points[i].x + d * Math.sin(theta),
            points[i].y - d * Math.cos(theta));
      }
    }
    return filter(newPts);
  }

  /**
   * Filter the points to delete any "kinks".
   *
   * @param pts original points
   * @return points without "kinks"
   */
  private Point2D.Double[] filter(Point2D.Double pts[]) {
    if (pts.length <= 3) {	// don't bother if 3 points or less
      return pts;
    }

    ArrayList<Point2D.Double> list = new ArrayList<>();
    int ptsCtr = 0;
    list.add(pts[ptsCtr++]);		// first point
    list.add(pts[ptsCtr++]);		// second point
    int listCtr = 2;
    double lastAngle, nextAngle;
    do {
      lastAngle = angle(list.get(listCtr - 2), list.get(listCtr - 1));
      nextAngle = angle(list.get(listCtr - 1), pts[ptsCtr], lastAngle);
      if (Math.abs(lastAngle - nextAngle) <= FILTER_ANGLE) {
        list.add(pts[ptsCtr++]);	// add the point
        listCtr++;
      } else {
        list.remove(--listCtr);		// remove the last point
        ptsCtr++;			// and skip the next one too
        if (listCtr < 2) {
          return pts;		// too much deleted, go with the unfilterd data
        }
      }
    } while (ptsCtr < pts.length);

    return list.toArray(new Point2D.Double[list.size()]);
  }

  /**
   * Get the angle of the line between 2 points.
   *
   * @param p1 First point
   * @param p2 Second point
   * @return angle (radians) in the range -PI to +PI
   */
  private double angle(Point2D.Double p1, Point2D.Double p2) {
    return Math.atan2(p2.y - p1.y, p2.x - p1.x);
  }

  /**
   * Get the angle of the line between 2 points, but make sure that the returned
   * angle is in a quadrant such that the angle to the given angle is within +/-
   * PI
   *
   * @param p1 First point
   * @param p2 Second point
   * @param a
   * @return angle between the 2 points
   */
  private double angle(Point2D.Double p1, Point2D.Double p2, double a) {
    double ang = angle(p1, p2);
    if (Math.abs(ang - a) > Math.PI) {	// check if difference is > 180
      if (a > 0.0) {
        ang = ang + 2.0 * Math.PI;	// if first positive, then make 2nd positive
      } else {
        ang = ang - 2.0 * Math.PI;	// if first negative, then make 2nd negative
      }
    }
    return ang;
  }

  /**
   * Find the direction perpendicular to the curve at the point on the curve
   * closest to the given point.
   *
   * Note: A value for x or y less than 1E-12 will be returned as exactly 0.0
   *
   * @param p Given point in inches
   * @param dir Direction: true = FRONT_INSIDE or BACK_OUTSIDE, false =
   * FRONT_OUTSIDE or BACK_INSIDE
   * @return Vector2d (normalized), null if less than 2 points
   */
  public Vector2d perpendicular(Point2D.Double p, boolean dir) {
    if (points.length < 2) {
      return null;
    }
    int i = indexOfNearestPoint(p);
    if (i < 0) {
      return null;
    }
    Vector2d v;
    int im1 = Math.max(i - 1, 0);
    int ip1 = Math.min(i + 1, points.length - 1);
    double dx = points[ip1].x - points[im1].x;
    double dy = points[ip1].y - points[im1].y;
    if (dir) {
      v = new Vector2d(dy, -dx);
    } else {
      v = new Vector2d(-dy, dx);
    }
    v.normalize();
    if (Math.abs(v.x) < 1E-12) {
      v.x = 0.0;
    }		// to prevent -0.0
    if (Math.abs(v.y) < 1E-12) {
      v.y = 0.0;
    }		// to prevent -0.0
    return v;
  }

  /**
   * Re-sample the points to give a uniform spacing between points
   *
   * @param spacing new spacing
   */
  synchronized public void reSample(double spacing) {
    if (points.length < 2) {
      return;
    }
    double cum = 0.0;
    double[] dist = new double[points.length];	// the cumulative distance at each point
    dist[0] = 0.0;
    for (int i = 1; i < points.length; i++) {
      cum += points[i - 1].distance(points[i]);
      dist[i] = cum;
    }
    double total = cum;			// total length
    int n = (int) Math.round(total / spacing) + 1;	// number of new points (including the start and end point)
    Point2D.Double[] newPts = new Point2D.Double[n];
    newPts[0] = points[0];		// first point is the same
    int ptr = 1;			// pointer to a member of dist[]
    double ratio;
    for (int i = 1; i < (n - 1); i++) {
      cum = total * (double) i / (double) (n - 1);	// distance for the new point
      while (cum > dist[ptr]) {
        ptr++;	// bump up the pointer until we find the point whose distance is >= cum
      }
      ratio = (cum - dist[ptr - 1]) / (dist[ptr] - dist[ptr - 1]);
      newPts[i] = new Point2D.Double(points[ptr - 1].x + ratio * (points[ptr].x - points[ptr - 1].x),
          points[ptr - 1].y + ratio * (points[ptr].y - points[ptr - 1].y));
    }
    newPts[newPts.length - 1] = points[points.length - 1];    // last point should be the same too 
    this.points = newPts;
  }
}
