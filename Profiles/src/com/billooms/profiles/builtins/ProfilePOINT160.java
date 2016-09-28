package com.billooms.profiles.builtins;

import com.billooms.profiles.BasicProfile;
import com.billooms.profiles.Profile;
import org.openide.util.lookup.ServiceProvider;

/**
 * A Profile that had a 150 degree point at the tip.
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
@ServiceProvider(service = Profile.class, position = 50)
public class ProfilePOINT160 extends BasicProfile {

  private final static double TAN80 = Math.tan(Math.toRadians(80.0));

  /**
   * Create a new profile with a 150 degree point.
   */
  public ProfilePOINT160() {
    super("Point160");
  }

  /**
   * Calculate the profile of the cutter at a given distance from the center.
   *
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
  @Override
  public double profileAt(double d, double rodRadius) {
    if (Math.abs(d) > rodRadius) {
      return -1.0;
    }
    return Math.abs(d) / TAN80;		// symmetrical around center
  }

  /**
   * Calculate the width of the cutter at the given depth.
   *
   * @param d depth
   * @param rodDiameter diameter of the cutter rod
   * @return
   */
  @Override
  public double widthAtDepth(double d, double rodDiameter) {
    if (d <= 0.0) {
      return 0.0;
    } else {
      return Math.min(rodDiameter, 2.0 * d * TAN80);
    }
  }
}
