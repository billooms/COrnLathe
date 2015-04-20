package com.billooms.patterns.holtz;

import com.billooms.patterns.BasicPattern;
import com.billooms.patterns.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * Holtzapffel Q -- D pattern on top of a puffy polygon.
 *
 * Note: repeat is strictly the number of repeats of the base pattern. This is
 * NOT the same numbering that Holtzapffel uses! The optional 2nd integer
 * parameter is the number of small bumps within a repeat. The optional 2nd
 * amplitude is the amplitude of the small bump as a fraction of the total
 * amplitude. Holtz number is repeat*n2.
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
@ServiceProvider(service = Pattern.class, position = 270)
public class HoltzQ extends BasicPattern {

  // minimum number of small bumps per repeat
  private final static int MIN_N2 = 3;
  // default amp2
  private final static double DEFAULT_AMP2 = 0.1;
  // this pattern uses the HoltzD pattern
  private final Pattern flower = new HoltzD();
  // this pattern uses the HoltzC pattern
  private final Pattern poly = new HoltzC();

  /**
   * Create a new rosette pattern
   */
  public HoltzQ() {
    super("HoltzQ");
    needsN2 = true;
    needsAmp2 = true;
    minRepeat = 3;				// the math doesn't work for less than 3 sides
  }

  /**
   * Get a normalized value (in the range of 0 to 1) for the given normalized
   * input (also in the range of 0 to 1). This should only be used for patterns
   * that need optional parameters. For all others, use getValue(n).
   *
   * @param n input value (in the range of 0.0 to 1.0)
   * @param r pattern repeat
   * @param n2 optional 2nd integer parameter
   * @param a2 optional 2nd amplitude parameter
   * @return normalized pattern value (in the range 0.0 to 1.0)
   */
  @Override
  public double getValue(double n, int r, int n2, double a2) {
    // Make sure nn is in the range 0.0 to 1.0
    double nn = n;
    if ((nn > 1.0) || (nn < 0.0)) {
      nn = nn - Math.floor(nn);
    }
    // Make sure r is at least the minimum
    int rr = Math.max(r, minRepeat);
    int nn2 = Math.max(n2, MIN_N2);
    double ampl2 = a2;
    if ((ampl2 < 0.0) || (ampl2 > 1.0)) {
      ampl2 = DEFAULT_AMP2;
    }
    double f = ampl2 * flower.getValue(nn * nn2, rr * nn2);
    double outline = (1.0 - ampl2) * poly.getValue(nn, rr);
    return outline + f;
  }
}
