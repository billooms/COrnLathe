package com.billooms.patterns.builtin;

import com.billooms.patterns.BasicPattern;
import com.billooms.patterns.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * Half Sine wave pattern.
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
@ServiceProvider(service = Pattern.class, position = 50)
public class PatternHALFSINE extends BasicPattern {

  /**
   * Create a new pattern with a half sine wave (actually a cosine)
   */
  public PatternHALFSINE() {
    super("HalfSine");
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
		// No checking for n in range 0.0 to 1.0 because sine works OK. 
    // Top half of a sine wave
    return Math.sin(n * Math.PI);
  }
}
