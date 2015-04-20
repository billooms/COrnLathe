package com.billooms.profiles;

import com.billooms.drawables.simple.PolyLine;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Profile of a cutter for an ornamental lathe.
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
public interface Profile {

  /** Default width of the profile image (currently fixed at 100 pixels) */
  int DEFAULT_WIDTH = 100;
  /** Default height of the profile image (currently fixed at 33 pixels) */
  int DEFAULT_HEIGHT = 33;	// odd number so equal number either side of center

  /**
   * Paint the object
   *
   * @param g2d Graphics2D
   */
  void paint(Graphics2D g2d);

  /**
   * Get the name of this profile.
   *
   * @return name of this profile
   */
  String getName();

  /**
   * Get the display name of the style.
   *
   * @return display name
   */
  String getDisplayName();

  /**
   * Determine if the profile is built-in (can't be deleted).
   *
   * @return true=built-in
   */
  boolean isBuiltIn();

  /**
   * Get a BufferedImage of the profile. The image can be used like an icon. The
   * size is DEFAULT_WIDTH x DEFAULT_HEIGHT.
   *
   * @return image
   */
  BufferedImage getImage();

  /**
   * Make a Drawable PolyLine representing this profile up to the given depth.
   * The origin is at 0.0, 0.0 and the orientation is tip pointing downward.
   *
   * @param pos position
   * @param rodDiameter rodDiameter
   * @param angle angle of rotation -- plus is counter-clockwise
   * @param c Color
   * @param s BasicStroke
   * @return Drawable PolyLine
   */
  PolyLine getDrawable(Point2D.Double pos, double rodDiameter, double angle, Color c, BasicStroke s);

  /**
   * Calculate the profile of the cutter at a given distance from the center.
   * This is a positive number indicating the distance back from the tip of the
   * cutter, where the tip of the cutter is 0.0. Return -1.0 if the given
   * distance is greater then the radius of the cutter.
   *
   * @param d distance from the center of the cutter (allow for both positive
   * and negative)
   * @param rodRadius rodDiameter/2.0
   * @return profile at the given distance. Return -1.0 if beyond the diameter
   * of the cutter.
   */
  double profileAt(double d, double rodRadius);

  /**
   * Calculate the width of the cutter at the given depth.
   *
   * @param d depth
   * @param rodDiameter diameter of the cutter rod
   * @return width of the cutter at the given depth
   */
  double widthAtDepth(double d, double rodDiameter);

}
