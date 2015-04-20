package com.billooms.drawables.simple;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A drawable square.
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
public class Square extends AbstractDrawable {

  /** Construct a new Square with defaults. */
  public Square() {
    super();
  }

  /**
   * Construct a new Square.
   *
   * @param x x-coordinate
   * @param y y-coordinate
   * @param c color
   * @param fill fill flag
   */
  public Square(double x, double y, Color c, boolean fill) {
    super(x, y, c, fill);
  }

  /**
   * Construct a new Square.
   *
   * @param pos position
   * @param c color
   * @param fill fill flag
   */
  public Square(Point2D.Double pos, Color c, boolean fill) {
    this(pos.x, pos.y, c, fill);
  }

  @Override
  public void paint(Graphics2D g2d) {
    if (visible) {
      g2d.setColor(color);
      float scale = (float) g2d.getTransform().getScaleX();
      g2d.setStroke(new BasicStroke(1.0f / scale));

      double sizeX = getXPixSizeInch(g2d);
      double sizeY = -getYPixSizeInch(g2d);   // - because Y is positive down
      if (isFilled()) {
        g2d.fill(new Rectangle2D.Double(x - sizeX / 2.0, y - sizeY / 2.0, sizeX, sizeY));
      } else {
        g2d.draw(new Rectangle2D.Double(x - sizeX / 2.0, y - sizeY / 2.0, sizeX, sizeY));
      }
    }
  }
}
