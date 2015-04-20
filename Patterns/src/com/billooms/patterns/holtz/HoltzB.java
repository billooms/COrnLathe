package com.billooms.patterns.holtz;

import com.billooms.patterns.BasicPattern;
import com.billooms.patterns.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * Holtzapffel B -- sine wave with every 3rd valley missing.
 *
 * Note: repeat is strictly the number of repeats of the base pattern. This is
 * NOT the same numbering that Holtzapffel uses! Holtz number is 3*repeat.
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
@ServiceProvider(service = Pattern.class, position = 120)
public class HoltzB extends BasicPattern {

  // this pattern uses the HoltzA pattern
  private final Pattern sine = new HoltzA();

  /**
   * Create a new rosette pattern
   */
  public HoltzB() {
    super("HoltzB");
  }

  /**
   * Get a normalized value (in the range of 0 to 1) for the given normalized
   * input (also in the range of 0 to 1).
   *
   * @param n given value in the range of 0.0 to 1.0
   * @return sine wave value in the range of 0.0 to 1.0
   */
  @Override
  public double getValue(double n) {
    // Make sure nn is in the range 0.0 to 1.0
    double nn = n;
    if ((nn > 1.0) || (nn < 0.0)) {
      nn = nn - Math.floor(nn);
    }
    // for n=2/3 to n=1.0 return zero (i.e. no deflection from max rosette radius)
    double z = 0.0;
    // 2 sine patterns then a skip
    if (nn < 2.0 / 3.0) {
      z = sine.getValue(nn * 3.0);
    }
    return z;
  }
}
