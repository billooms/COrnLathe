package com.billooms.spirals;

import java.awt.geom.Point2D;
import javafx.geometry.Point3D;

/**
 * Interface for a spiral that can be utilized for certain CutPoints.
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
public interface SpiralStyle {

  /** Default twist in degrees */
  public double DEFAULT_TWIST = 90.0;
  /** Default amplitude parameter (not used by all styles) */
  public double DEFAULT_AMP = 0.0;

  /**
   * Get the name of this spiral.
   *
   * @return name of this spiral
   */
  String getName();

  /**
   * Get the display name of the spiral.
   *
   * @return display name
   */
  String getDisplayName();

  /**
   * Determine if the spiral needs the optional amplitude parameter (usually not
   * the case).
   *
   * @return true=needs an amplitude parameter for the spiral
   */
  boolean needsAmplitude();

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
	public Point3D[] makeSpiral(Point2D.Double[] pts, double twist, double amp);
}
