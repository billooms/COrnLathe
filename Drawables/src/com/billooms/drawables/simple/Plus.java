package com.billooms.drawables.simple;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * A drawable plus.
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
public class Plus extends AbstractDrawable {

  /** Construct a new Plus with defaults. */
  public Plus() {
    super();
  }

  /**
   * Construct a new Plus.
   *
   * @param x x-coordinate
   * @param y y-coordinate
   * @param c color
   */
  public Plus(double x, double y, Color c) {
    super(x, y, c, true);
  }

  /**
   * Construct a new Plus.
   *
   * @param pos position
   * @param c color
   */
  public Plus(Point2D.Double pos, Color c) {
    this(pos.x, pos.y, c);
  }

  @Override
  public void paint(Graphics2D g2d) {
    if (visible) {
      g2d.setColor(color);
      float scale = (float) g2d.getTransform().getScaleX();
      g2d.setStroke(new BasicStroke(1.0f / scale));

      double sizeX = getXPixSizeInch(g2d);
      double sizeY = -getYPixSizeInch(g2d);   // - because Y is positive down
      g2d.draw(new Line2D.Double(x, y + sizeY / 2.0, x, y - sizeY / 2.0));
      g2d.draw(new Line2D.Double(x + sizeX / 2.0, y, x - sizeX / 2.0, y));
    }
  }
}
