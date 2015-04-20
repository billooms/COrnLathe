package com.billooms.patterns.builtin;

import com.billooms.patterns.BasicPattern;
import com.billooms.patterns.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * Sine wave pattern.
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
@ServiceProvider(service = Pattern.class, position = 40)
public class PatternSINE extends BasicPattern {

  /**
   * Create a new pattern with a sine wave (actually a cosine)
   */
  public PatternSINE() {
    super("Sine");
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
    // No checking for n in range 0.0 to 1.0 because cosine works OK. 
    // cosine wave centered around the middle
    return 0.5 + 0.5 * Math.cos(n * 2.0 * Math.PI);
  }
}
