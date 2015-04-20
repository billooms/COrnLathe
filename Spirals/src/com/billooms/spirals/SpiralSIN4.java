package com.billooms.spirals;

import java.awt.geom.Point2D.Double;
import javafx.geometry.Point3D;
import org.openide.util.lookup.ServiceProvider;

/**
 * Spiral where the twist is proportional to distance along the outline *
 * quarter sine wave.
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
@ServiceProvider(service = SpiralStyle.class, position = 50)
public class SpiralSIN4 extends AbstractSpiral {

  /**
   * Create a new spiral based on a quarter sine wave.
   */
  public SpiralSIN4() {
    super("Sin4");
  }

  /**
   * This is where the main calculation is done. Each point is calculated then
   * stuffed into twistPts[].
   *
   * Note: the first point should be set to pts[0].x, pts[0].y, 0.0
   *
   * @param pts Points representing the outline of the portion of a shape
   * @param twist total twist in degrees
   * @param amp optional amplitude parameter (not used by all spirals)
   */
  @Override
  public void calculate(Double[] pts, double twist, double amp) {
    double tot = 0.0;
    for (int i = 1; i < pts.length; i++) {		// find the total length
      tot += pts[i].distance(pts[i - 1]);
    }

    twistPts[0] = new Point3D(pts[0].x, pts[0].y, 0.0);	// first point is 0.0
    double cum = 0.0;		// the cumulative length
    for (int i = 1; i < twistPts.length; i++) {
      cum += pts[i].distance(pts[i - 1]);
      twistPts[i] = new Point3D(pts[i].x, pts[i].y, twist * Math.sin((Math.PI / 2.0) * cum / tot));
    }
  }

}
