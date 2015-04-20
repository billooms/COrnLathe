package com.billooms.patterns;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Interface defining a pattern for a rosette.
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
public interface Pattern {

  /** Default width of the plot image (currently fixed at 100 pixels). */
  int DEFAULT_WIDTH = 100;
  /** Height of the plot image (currently fixed at 50 pixels). */
  int DEFAULT_HEIGHT = 50;

  /**
   * Paint the object.
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
   * Get the repeat value.
   *
   * @return repeat value
   */
  int getRepeat();

  /**
   * Find the minimum repeat allowed for this pattern.
   *
   * @return minimum repeat
   */
  int getMinRepeat();

  /**
   * Determine if the profile is built-in (can't be deleted).
   *
   * @return true=built-in
   */
  boolean isBuiltIn();

  /**
   * Determine if the pattern needs a repeat value (usually not the case).
   *
   * @return true=needs a repeat value for the pattern
   */
  boolean needsRepeat();

  /**
   * Determine if the pattern needs optional n2.
   *
   * @return true=needs optional n2 parameter
   */
  boolean needsN2();

  /**
   * Determine if the pattern needs optional parameter amp2.
   *
   * @return true=needs optional amp2 parameter
   */
  boolean needsAmp2();

  /**
   * Determine if the pattern needs optional parameter (n2 and/or amp2).
   *
   * @return true=needs one or more optional parameters
   */
  boolean needsOptions();

  /**
   * Get a BufferedImage of the profile. The image can be used like an icon. The
   * size is DEFAULT_WIDTH x DEFAULT_HEIGHT.
   *
   * @return image
   */
  BufferedImage getImage();

  /**
   * Get a BufferedImage of a plot of the pattern.
   *
   * The image can be used like an icon. The size is DEFAULT_WIDTH x
   * DEFAULT_HEIGHT. This should only be used for patterns that need to know the
   * repeat. For all others, use getImage(n).
   *
   * @param r pattern repeat
   * @return image
   */
  BufferedImage getImage(int r);

  /**
   * Get a BufferedImage of a plot of the pattern.
   *
   * The image can be used like an icon. The size is DEFAULT_WIDTH x
   * DEFAULT_HEIGHT. This should only be used for patterns that need optional
   * parameters. For all others, use getImage(n).
   *
   * @param r pattern repeat
   * @param n optional 2nd integer parameter
   * @param a2 optional 2nd amplitude parameter
   * @return image
   */
  BufferedImage getImage(int r, int n, double a2);

  /**
   * Get a normalized value (in the range of 0 to 1) for the given normalized
   * input (also in the range of 0 to 1).
   *
   * @param n input value (in the range of 0.0 to 1.0)
   * @return normalized pattern value (in the range 0.0 to 1.0)
   */
  default double getValue(double n) {
    return 0.0;
  }

  /**
   * Get a normalized value (in the range of 0 to 1) for the given normalized
   * input (also in the range of 0 to 1).
   *
   * This should only be used for patterns that need to know the repeat. For all
   * others, use getValue(n).
   *
   * @param n input value (in the range of 0.0 to 1.0)
   * @param r pattern repeat
   * @return normalized pattern value (in the range 0.0 to 1.0)
   */
  default double getValue(double n, int r) {
    return 0.0;
  }

  /**
   * Get a normalized value (in the range of 0 to 1) for the given normalized
   * input (also in the range of 0 to 1).
   *
   * This should only be used for patterns that need optional parameters. For
   * all others, use getValue(n).
   *
   * @param n input value (in the range of 0.0 to 1.0)
   * @param r pattern repeat
   * @param n2 optional 2nd integer parameter
   * @param a2 optional 2nd amplitude parameter
   * @return normalized pattern value (in the range 0.0 to 1.0)
   */
  default double getValue(double n, int r, int n2, double a2) {
    return 0.0;
  }
}
