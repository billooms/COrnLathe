package com.billooms.drawables;

import com.billooms.drawables.simple.Square;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import org.w3c.dom.Element;

/**
 * An extension to Pt which draws dual square point points -- dual Y values for
 * each X.
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
public class SquarePt2 extends Pt {

  private final Square square = new Square();
  private final Square square2 = new Square();

  /**
   * A drawable square point defined by inch location and color. The second
   * point is at y2=0.
   *
   * @param pos square point location in inches
   * @param c Color
   * @param c2 2nd color
   */
  public SquarePt2(Point2D.Double pos, Color c, Color c2) {
    super(pos.x, pos.y, Style.XYY);
    square.setColor(c);
    square2.setColor(c2);
  }

  /**
   * A drawable square point defined by inch location and color. The second
   * point is at y2=0.
   *
   * @param x x-coordinate
   * @param y y-coordinate
   * @param y2 y2 coordinate
   * @param c Color
   * @param c2 2nd color
   */
  public SquarePt2(double x, double y, double y2, Color c, Color c2) {
    super(x, y, y2, Style.XYY);
    square.setColor(c);
    square2.setColor(c2);
  }

  /**
   * Construct a new SquarePt from the given DOM Element.
   *
   * @param element given DOM Element
   * @param c color
   * @param c2 2nd color
   */
  public SquarePt2(Element element, Color c, Color c2) {
    super(element, Style.XYY);
    square.setColor(c);
    square2.setColor(c2);
  }

  @Override
  public synchronized void setVisible(boolean v) {
    super.setVisible(v);
    square.setVisible(v);
    square2.setVisible(v);
  }

  @Override
  public synchronized void setColor(Color c) {
    super.setColor(c);
    square.setColor(c);
  }

  /**
   * Set the color of the 2nd square.
   *
   * @param c color
   */
  public synchronized void setColor2(Color c) {
    square2.setColor(c);
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
      square2.setXY(getX(), getY2());
      square2.paint(g2d);
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    throw new UnsupportedOperationException("SquarePt.propertyChange(): Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
