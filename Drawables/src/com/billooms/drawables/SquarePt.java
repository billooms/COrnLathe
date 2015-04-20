package com.billooms.drawables;

import com.billooms.drawables.simple.Square;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import org.w3c.dom.Element;

/**
 * An extension to Pt which draws a square point.
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
public class SquarePt extends Pt {

  private final Square square = new Square();

  /**
   * A drawable square point defined by inch location and color.
   *
   * @param pos square point location in inches
   * @param c Color
   */
  public SquarePt(Point2D.Double pos, Color c) {
    super(pos.x, pos.y, Style.XY);
    square.setColor(c);
  }

  /**
   * Construct a new SquarePt from the given DOM Element.
   *
   * @param element given DOM Element
   * @param c color
   */
  public SquarePt(Element element, Color c) {
    super(element, Style.XY);
    square.setColor(c);
  }

  @Override
  public synchronized void setVisible(boolean v) {
    super.setVisible(v);
    square.setVisible(v);
  }

  @Override
  public synchronized void setColor(Color c) {
    super.setColor(c);
    square.setColor(c);
  }

  /**
   * Paint the object
   *
   * @param g2d Graphics2D
   */
  @Override
  public void paint(Graphics2D g2d) {
    if (isVisible()) {
      square.setXY(getX(), getY());
      square.paint(g2d);
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    throw new UnsupportedOperationException("SquarePt.propertyChange(): Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
