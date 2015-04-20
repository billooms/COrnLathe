package com.billooms.drawables.simple;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * A Drawable straight line.
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
public class Line extends AbstractDrawable {

  /** Width and height from the starting point. */
  private double w, h;

  /**
   * A drawable line.
   *
   * @param pos Point2D.Double defining the beginning of the line
   * @param w width of the line in inches
   * @param h height of the line in inches
   * @param c Color
   */
  public Line(Point2D.Double pos, double w, double h, Color c) {
    super(pos, c, SOLID_LINE);
    this.w = w;
    this.h = h;
  }

  /**
   * Get the width (x-direction) in inches.
   *
   * @return width (x-direction) in inches
   */
  public double getW() {
    return w;
  }

  /**
   * Set the width (x-direction) in inches.
   *
   * @param w new width (x-direction) in inches
   */
  public void setW(double w) {
    this.w = w;
  }

  /**
   * Get the height (y-direction) in inches.
   *
   * @return height (y-direction) in inches
   */
  public double getH() {
    return h;
  }

  /**
   * Set the height (y-direction) in inches.
   *
   * @param h new height (y-direction) in inches
   */
  public void setH(double h) {
    this.h = h;
  }

  /**
   * Paint the object
   *
   * @param g2d Graphics2D
   */
  @Override
  public void paint(Graphics2D g2d) {
    if (visible) {
      g2d.setColor(getColor());
      float scale = (float) g2d.getTransform().getScaleX();
      if (scale < 0.0) {
        return;		// not sure why this happens sometimes
      }
      float array[] = getStroke().getDashArray();
      if ((array == null) || (array.length == 0)) {
        g2d.setStroke(new BasicStroke(1.0f / scale));
      } else {
        for (int i = 0; i < array.length; i++) {
          array[i] = array[i] / scale;
        }
        g2d.setStroke(new BasicStroke(1.0f / scale, getStroke().getEndCap(), getStroke().getLineJoin(), 1.0f, array, getStroke().getDashPhase() / scale));
      }
      g2d.draw(new Line2D.Double(x, y, x + w, y + h));
    }
  }
}
