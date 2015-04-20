package com.billooms.profiles.builtins;

import com.billooms.profiles.BasicProfile;
import com.billooms.profiles.Profile;
import org.openide.util.lookup.ServiceProvider;

/**
 * Ideal cutter tip is just a very sharp point. Imagine this to be like a very
 * strong needle.
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
@ServiceProvider(service = Profile.class, position = 0)
public class ProfileIDEAL extends BasicProfile {

  /**
   * Create a new ideal profile (a very sharp point).
   */
  public ProfileIDEAL() {
    super("Ideal");
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
    if (Math.abs(d) <= 0.001) {		// works better than (d == 0.0)
      return 0.0;
    } else {
      return -1.0;
    }
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
    return 0.0;
  }
}
