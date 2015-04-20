package com.billooms.cutpoints.surface;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point3D;

/**
 * An array of 3D points representing a line in 3D lathe space.
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
public class Line3D {

  /** The list of 3D points (lathe coordinates). */
  public final ArrayList<Point3D> pts = new ArrayList<>();

  /**
   * Construct a new empty line.
   */
  public Line3D() {
    // do nothing
  }

  /**
   * Construct a new Line3D from the given list of points.
   *
   * @param pts list of points
   */
  public Line3D(List<Point3D> pts) {
    this.pts.addAll(pts);
  }

  /**
   * Construct a straight line between the two given points.
   *
   * @param p1 first point
   * @param p2 second point
   */
  public Line3D(Point3D p1, Point3D p2) {
    this.pts.add(p1);
    this.pts.add(p2);
  }

  /**
   * Get the number of points in the line.
   *
   * @return number of points
   */
  public int size() {
    return pts.size();
  }

  /**
   * Determine if the line is empty (no points).
   *
   * @return true=no points
   */
  public boolean isEmpty() {
    return pts.isEmpty();
  }

  /**
   * Add the given point to the end of the line.
   *
   * @param pt point
   * @return point was added
   */
  public boolean add(Point3D pt) {
    return pts.add(pt);
  }

  /**
   * Get the points.
   *
   * @return points
   */
  public List<Point3D> getPoints() {
    return pts;
  }

  /**
   * Rotate around the given axis by the given number of degrees.
   *
   * @param axis Axis
   * @param angle angle in degrees
   */
  public void rotate(RotMatrix.Axis axis, double angle) {
    RotMatrix mat = new RotMatrix(axis, angle);
    ArrayList<Point3D> newPts = new ArrayList<>();
    pts.stream().forEach((pt) -> {
      newPts.add(mat.apply(pt));
    });
    pts.clear();
    pts.addAll(newPts);
  }

  /**
   * Translate the line by adding the given offset to each of the points.
   *
   * @param x x offset
   * @param y y offset
   * @param z z offset
   */
  public void translate(double x, double y, double z) {
    ArrayList<Point3D> newPts = new ArrayList<>();
    pts.stream().forEach((pt) -> {
      newPts.add(new Point3D(pt.getX() + x, pt.getY() + y, pt.getZ() + z));
    });
    pts.clear();
    pts.addAll(newPts);
  }

}
