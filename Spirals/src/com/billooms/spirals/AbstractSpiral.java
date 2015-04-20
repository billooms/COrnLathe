package com.billooms.spirals;

import java.awt.geom.Point2D;
import javafx.geometry.Point3D;

/**
 * This is a basic spiral that can be extended without having to implement all
 * the methods.
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
public abstract class AbstractSpiral implements SpiralStyle {

  /** The name of this spiral (upper case letters only). */
  protected String name = "";
  /** The display name of this spiral. */
  protected String displayName = "";
  /** Flag indicating if this needs the optional amplitude parameter. */
  protected boolean needsAmplitude = false;

  /**
   * An array of Point3d with x,y from the original curve points and z=twist in
   * degrees. Note: In lathe coordinates, this is actually x, z, c(degrees).
   * This is a temporary array for the user to put data into.
   */
  protected Point3D[] twistPts;

  /**
   * Create a new spiral with the given display name.
   *
   * The name will be a modified version of the display name: Convert everything
   * to uppercase, delete anything after the first blank, and remove anything
   * that is not an uppercase.
   *
   * @param displayName display name of the new style
   */
  public AbstractSpiral(String displayName) {
    this.displayName = displayName;
    this.name = filterName(displayName);
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Get the name of this pattern.
   *
   * @return name of this pattern
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Get the display name of the style.
   *
   * @return display name
   */
  @Override
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Determine if the spiral needs the optional amplitude parameter (usually not
   * the case).
   *
   * @return true=needs an amplitude parameter for the spiral
   */
  @Override
  public boolean needsAmplitude() {
    return needsAmplitude;
  }

  /**
   * Make an array of twists (in degrees) for the given array of points.
   *
   * Note: The first point twist should always be 0.0, so this is relative twist.
   * You need to add the starting phase of the spiral to these numbers to get
   * the total twist.
   *
   * @param pts Points representing the outline of the portion of a shape
   * @param twist total twist in degrees
   * @param amp optional amplitude parameter (not used by all spirals)
   * @return an array of Point3D with x,y from the original pts and z=twist in
   * degrees (or null if there are no points given). Note: In lathe coordinates,
   * this is actually x, z, c(degrees).
   */
  @Override
  public Point3D[] makeSpiral(Point2D.Double[] pts, double twist, double amp) {
    if (pts.length == 0) {
      return null;
    }

    if (pts.length == 1) {
      twistPts = new Point3D[2];		// must have at least a start and end
      twistPts[0] = new Point3D(pts[0].x, pts[0].y, 0.0);	// first point is 0.0
      twistPts[1] = new Point3D(pts[0].x, pts[0].y, twist);	// and the second is the total twist
    } else {
      twistPts = new Point3D[pts.length];
      twistPts[0] = new Point3D(pts[0].x, pts[0].y, 0.0);	// first point is 0.0

      calculate(pts, twist, amp);		// This is where the main calculation is done.
    }
    return twistPts;
  }

  /**
   * This is where the main calculation is done. Each point is calculated then
   * stuffed into twistPts[].
   *
   * Note: the first point should be set to pts[0].x, pts[0].y, 0.0
   *
   * @param pts Points representing the outline of the portion of a shape
   * @param twist total twist in degrees
   * @param amp optional amplitude parameter (not used by all spirals)
   */
  abstract void calculate(Point2D.Double[] pts, double twist, double amp);

  /**
   * Filter the display name to produce a pattern name. Convert everything to
   * uppercase, delete anything after the first blank, and remove anything that
   * is not an uppercase alpha-numeric.
   *
   * @param n display name
   * @return pattern name
   */
  protected final String filterName(String n) {
    String str = n.toUpperCase();   // convert to upper case
    if (str.contains(" ")) {	    // only go up to first blank
      str = str.substring(0, str.indexOf(" "));
    }
    for (int i = 0; i < str.length(); i++) {    // look for anything besides uppercase letters
      if (((str.charAt(i) >= 'A') && (str.charAt(i) <= 'Z')) || // keep alpha-numerics
          ((str.charAt(i) >= '0') && (str.charAt(i) <= '9'))) {
      } else {
        str = str.replace(str.charAt(i), '-');
      }
    }
    str = str.replace("-", "");     // and strip them out
    if ("".equals(str)) {
      str = "NEW";
    }
    return str;
  }
}
