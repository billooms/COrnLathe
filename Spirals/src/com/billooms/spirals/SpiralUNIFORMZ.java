package com.billooms.spirals;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import javafx.geometry.Point3D;
import org.openide.util.lookup.ServiceProvider;

/**
 * Spiral where the twist is proportional to the height.
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
@ServiceProvider(service = SpiralStyle.class, position = 10)
public class SpiralUNIFORMZ extends AbstractSpiral {

  /**
   * Create a new spiral with the twist proportional to the height.
   */
  public SpiralUNIFORMZ() {
    super("Uniform Z");
    this.name = "UNIFORMZ";
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
    double y0 = pts[0].y;
    double tot = pts[pts.length-1].y - y0;
    
    for (int i = 0; i < twistPts.length; i++) {
      twistPts[i] = new Point3D(pts[i].x, pts[i].y, twist * (pts[i].y - y0) / tot);
    }
  }

}
