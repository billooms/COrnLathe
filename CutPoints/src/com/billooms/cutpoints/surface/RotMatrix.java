package com.billooms.cutpoints.surface;

import javafx.geometry.Point3D;

/**
 * Rotation Matrix.
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
public class RotMatrix {

  /** Options for rotation: around X, Y, or Z axis. */
  public static enum Axis {

    /** Used to indicate rotation around the X-axis. */
    X,
    /** Used to indicate rotation around the Y-axis. */
    Y,
    /** Used to indicate rotation around the Z-axis. */
    Z
  }

  private double[][] m;

  /**
   * Rotation Matrix.
   *
   * @param axis X, Y, or Z
   * @param angle angle in degrees
   */
  public RotMatrix(Axis axis, double angle) {
    m = new double[3][3];	// all values are 0.0 initially
    double sinA = Math.sin(Math.toRadians(angle));
    double cosA = Math.cos(Math.toRadians(angle));
    switch (axis) {
      case X:
        m[0][0] = 1.0;
                        m[1][1] = cosA;   m[1][2] = -sinA;
                        m[2][1] = sinA;   m[2][2] = cosA;
        break;
      case Y:
        m[0][0] = cosA;                   m[0][2] = sinA;
                        m[1][1] = 1.0;
        m[2][0] = -sinA;                  m[2][2] = cosA;
        break;
      case Z:
        m[0][0] = cosA; m[0][1] = -sinA;
        m[1][0] = sinA; m[1][1] = cosA;
                                          m[2][2] = 1.0;
        break;
    }
  }

  /**
   * Apply this rotation matrix to the given point.
   *
   * @param p Point
   * @return Rotated point
   */
  public Point3D apply(Point3D p) {
    final double x = p.getX();
    final double y = p.getY();
    final double z = p.getZ();
    return new Point3D(x * m[0][0] + y * m[0][1] + z * m[0][2],
                       x * m[1][0] + y * m[1][1] + z * m[1][2],
                       x * m[2][0] + y * m[2][1] + z * m[2][2]);
  }
}
