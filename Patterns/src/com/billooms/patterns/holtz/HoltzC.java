package com.billooms.patterns.holtz;

import com.billooms.patterns.BasicPattern;
import com.billooms.patterns.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * Holtzapffel C rosette as I've implemented it is a regular polygon.
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
@ServiceProvider(service = Pattern.class, position = 130)
public class HoltzC extends BasicPattern {

  /**
   * Create a new rosette pattern
   */
  public HoltzC() {
    super("HoltzC");
    needsRepeat = true;			// the pattern shape is a function of the number of repeats
    minRepeat = 3;				// the math doesn't work for less than 3 sides
  }

  /**
   * Get a normalized value (in the range of 0 to 1) for the given normalized
   * input (also in the range of 0 to 1).
   *
   * @param n input value (in the range of 0.0 to 1.0)
   * @param r pattern repeat
   * @return normalized pattern value (in the range 0.0 to 1.0)
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
    // alpha (in radians) is 1/2 the angle spanned by the side of a polygon
    // r is the number of sides to the polygon
    double alphaRad = Math.PI / rr;
    // theta (in radians) goes from -alpha to +alpha as a function of the argument n
    // tanTheta is the tangent of theta
    double tanTheta = Math.tan((nn * 2.0 - 1.0) * alphaRad);
    // s is the sagitta (peak-to-peak of the polygon)
    // the radius is normalized to 1.0
    // note that this is a function of the number of sides of the polygon
    double s = (1.0 - Math.cos(alphaRad));
    // x is cos(alpha)
    double x = Math.cos(alphaRad);
    // y is tan(theta)
    double y = x * tanTheta;
    // z is the distance between the circle and the polygon 
    // along a line drawn from the center of the circle
    // at the angle theta
    // the radius is normalized to 1.0
    double z = 1.0 - Math.sqrt(x * x + y * y);
    // normalize z so its always range 0 to 1
    z = z / s;
    return z;
  }
}
