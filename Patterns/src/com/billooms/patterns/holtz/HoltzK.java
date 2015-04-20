package com.billooms.patterns.holtz;

import com.billooms.patterns.BasicPattern;
import com.billooms.patterns.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * Holtzapffel K -- 2 sine waves (1/4 of pattern) and a heart (3/4 of pattern.
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
@ServiceProvider(service = Pattern.class, position = 210)
public class HoltzK extends BasicPattern {

  // dividing point
  private final static double DIVISION = 0.25;
  // scale factor for n 
  private final static double SCALE = 1.0 / (1.0 - DIVISION);
  // this pattern uses the HoltzA pattern
  private final Pattern sine = new HoltzA();
  // and the HoltzF pattern
  private final Pattern heart = new HoltzF();

  /**
   * Create a new rosette pattern
   */
  public HoltzK() {
    super("HoltzK");
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
    double z;
    if (nn < DIVISION) {
      z = sine.getValue(nn * 8.0);	// 2 full sine waves
    } else {
      z = heart.getValue((nn - DIVISION) * SCALE);	// big bump
    }
    return z;
  }
}
