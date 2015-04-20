package com.billooms.patterns.holtz;

import com.billooms.patterns.BasicPattern;
import com.billooms.patterns.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * Holtzapffel F rosette is a heart shape.
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
@ServiceProvider(service = Pattern.class, position = 160)
public class HoltzF extends BasicPattern {

  /**
   * Create a new rosette pattern
   */
  public HoltzF() {
    super("HoltzF");
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
    // make a symmetrical pattern by only defining half of it
    nn = 2.0 * nn;
    // mirror the other half around the center point
    if (nn > 1.0) {
      nn = 2.0 - nn;
    }
    double z = Math.sin(nn * 2 * Math.PI);	// for 0.0 to 0.25
    if (nn >= 0.75) {
      z = z + 1.0;				// for 0.75 to 1.0
    } else if (nn > 0.25) {          // for 0.25 to 0.75
      z = z + (1.0 - Math.sin(nn * 2 * Math.PI)) / 2.0;
    }
    return z;
  }
}
