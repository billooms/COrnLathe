package com.billooms.patterns.holtz;

import com.billooms.patterns.BasicPattern;
import com.billooms.patterns.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * Holtzapffel L -- alternates small and large (2X) bumps.
 *
 * Note: repeat is strictly the number of repeats of the base pattern. This is
 * NOT the same numbering that Holtzapffel uses! Holtz number is 2*repeat.
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
@ServiceProvider(service = Pattern.class, position = 220)
public class HoltzL extends BasicPattern {

  // number of small bumps per repeat
  private final static int TOTAL = 3;
  // number of small bumps
  private final static int NSMALL = 1;
  // size of big bumps
  private final static int BIG = TOTAL - NSMALL;
  // dividing point
  private final static double DIVISION = (double) NSMALL / (double) TOTAL;
  // scale factor for n 
  private final static double SCALE = 1.0 / (1.0 - DIVISION);
  // this pattern uses the HoltzD pattern
  private final Pattern bump = new HoltzD();

  /**
   * Create a new rosette pattern
   */
  public HoltzL() {
    super("HoltzL");
    needsRepeat = true;
    minRepeat = 2;
  }

  /**
   * Get a normalized value (in the range of 0 to 1) for the given normalized
   * input (also in the range of 0 to 1).
   *
   * @param n given value in the range of 0.0 to 1.0
   * @param r pattern repeat
   * @return sine wave value in the range of 0.0 to 1.0
   */
  @Override
  public double getValue(double n, int r) {
    // Make sure nn is in the range 0.0 to 1.0
    double nn = n;
    if ((nn > 1.0) || (nn < 0.0)) {
      nn = nn - Math.floor(nn);
    }
    // Make sure r is at least the minimum
    int rr = Math.max(r, minRepeat);
    double z;
    if (nn < DIVISION) {
      z = bump.getValue(nn * (double) TOTAL, rr * TOTAL);		// small bump
    } else {
      z = bump.getValue((nn - DIVISION) * SCALE, rr * TOTAL / BIG);	// big bump
    }
    return z;
  }
}
