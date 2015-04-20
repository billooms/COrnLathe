package com.billooms.drawables.simple;

import com.billooms.drawables.vecmath.Vector2d;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * A Drawable shape made of multiple lines defined by an ArrayList of
 * coordinates.
 *
 * In addition to color, visible flag, and stroke, a PolyLine has a list of
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
public class PolyLine extends AbstractDrawable {

  /** The list of points */
  private ArrayList<Point2D.Double> ptList = new ArrayList<>();

  /**
   * Create a new PolyLine with the given color and stroke.
   *
   * @param c Color
   * @param s Stroke
   */
  public PolyLine(Color c, BasicStroke s) {
    this.color = c;
    this.stroke = s;
  }

  /**
   * Paint the object.
   *
   * @param g2d Graphics2D
   */
  @Override
  public void paint(Graphics2D g2d) {
    if (ptList.size() < 2) {	// no line for only one point
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
      Point2D.Double p, pm1;
      for (int i = 1; i < ptList.size(); i++) {
        p = ptList.get(i - 1);
        pm1 = ptList.get(i);
        g2d.draw(new Line2D.Double(pm1.x, pm1.y, p.x, p.y));
      }
    }
  }

  /**
   * Clear all points.
   */
  public synchronized void clear() {
    ptList.clear();
  }

  /**
   * Get all the points.
   *
   * @return all points
   */
  public ArrayList<Point2D.Double> getPoints() {
    return ptList;
  }

  /**
   * Set the points to the new list.
   *
   * @param list new points
   */
  public synchronized void setPoints(ArrayList<Point2D.Double> list) {
    ptList = list;
  }

  /**
   * Get the number of points.
   *
   * @return number of points
   */
  public int size() {
    return ptList.size();
  }

  /**
   * Get the point of the given index.
   *
   * @param i index
   * @return point at that index
   */
  public Point2D.Double get(int i) {
    return ptList.get(i);
  }

  /**
   * Add the given point to the end of the list.
   *
   * @param p point to add
   */
  public synchronized void add(Point2D.Double p) {
    ptList.add(p);
  }

  /**
   * Remove the given point.
   *
   * @param p point to remove
   */
  public synchronized void remove(Point2D.Double p) {
    ptList.remove(p);
  }

  /**
   * Rotate the shape by a given angle around the given pivot. Positive angle is
   * counter clockwise.
   *
   * @param ang angle in degrees
   * @param pivot pivot point for rotation
   */
  public synchronized void rotate(double ang, Point2D.Double pivot) {
    ArrayList<Point2D.Double> newList = new ArrayList<>();
    for (Point2D.Double pt : ptList) {
      Vector2d v = new Vector2d(pt.x - pivot.x, pt.y - pivot.y);
      Vector2d vp = v.rotate(ang);
      newList.add(new Point2D.Double(pivot.x + vp.x, pivot.y + vp.y));
    }
    ptList = newList;
  }

}
