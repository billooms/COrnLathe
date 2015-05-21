package com.billooms.rosette;

import java.util.Hashtable;
import javax.swing.JLabel;

/**
 * Create labels for sliders
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
public class SliderLabels extends Hashtable {

  /**
   * Create custom labels for a slider.
   *
   * @param sliderMin minimum slider value
   * @param sliderMax maximum slider value
   * @param sliderDelta delta for putting labels (in slider units)
   * @param labelMin starting label value
   * @param labelDelta increment for label values
   */
  public SliderLabels(int sliderMin, int sliderMax, int sliderDelta, int labelMin, int labelDelta) {
    int i, n;
    for (i = sliderMin, n = labelMin; i <= sliderMax; i = i + sliderDelta, n = n + labelDelta) {
      put(i, new JLabel(Integer.toString(n)));
    }
  }

  /**
   * Create custom labels for a slider.
   *
   * @param sliderMin minimum slider value
   * @param sliderMax maximum slider value
   * @param sliderDelta delta for putting labels (in slider units)
   * @param labelMin starting label value
   * @param labelDelta increment for label values
   */
  public SliderLabels(int sliderMin, int sliderMax, int sliderDelta, double labelMin, double labelDelta) {
    int i;
    double n;
    for (i = sliderMin, n = labelMin; i <= sliderMax; i = i + sliderDelta, n = n + labelDelta) {
      put(i, new JLabel(Double.toString(n)));
    }
  }

}
